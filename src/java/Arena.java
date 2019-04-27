
// Environment code for project mobasimulator

import jason.asSyntax.*;
import jason.environment.*;
import model.Attack;
import model.Champion;
import model.Minion;
import model.Nexus;
import model.Role;
import model.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.*;

public class Arena extends Environment {

	public static final Literal prepareArena = Literal.parseLiteral("prepareArena");
	public static final String selectNextEnemy = "selectNextEnemy";
	public static final String updateKill = "updateKill";
	public static final String turn = "turn";
	public static final String createMinions = "createMinions";
	public Map<Team, List<Minion>> teams = new HashMap<Team, List<Minion>>();
	private static final int numberOfMeleeMinions = 2;
	private static final int numberOfMinions = 6;
	private Logger log = Logger.getLogger("mobasimulator." + Arena.class.getName());
	private Champion bluTeamChampion;
	private Champion redTeamChampion;
	private Nexus redTeamNexus;
	private Nexus blueTeamNexus;
	private final static String selectTarget = "selectTarget";

	/** Called before the MAS execution with the args informed in .mas2j */
	@Override
	public void init(String[] args) {

	}

	@Override
	/* agName nome dell'agente, action l'azione */
	public boolean executeAction(String agName, Structure action) {
		// log.info(agName+" "+action.getFunctor());
		boolean execute = false;
		if (action.equals(prepareArena)) {
			// log.info("create arena");
			this.createArena();
			System.out.println(agName);
			for (final Team team : Team.values()) {
				List<Minion> minions = teams.get(team);
				minions.stream().forEach(new Consumer<Minion>() {
					public void accept(Minion minion) {

						log.info("Spawning minion");
						addPercept(team.toString() + minion.toString(), Literal.parseLiteral("spawn"));
					}
				});

			}

			execute = true;

		}
		if (action.getFunctor().contentEquals(turn)) {

			Team startingTeam = Team.valueOf(action.getTerm(0).toString());
			Team otherTeam = Team.getOtherTeam(startingTeam);
			List<Minion> minions = this.teams.get(startingTeam);
			List<Minion> otherMinions = this.teams.get(otherTeam);
			/*
			 * It is to ensure there is always one and only damage percept for other team
			 * and one and only one attack in this turn team
			 */
			this.updatePerceptsForTeamsElements(startingTeam, otherTeam);

			int bound = minions.size();

			Champion thisTurnCurrentChampion = startingTeam == Team.blueTeam ? this.bluTeamChampion
					: this.redTeamChampion;
			if (bound > 0) {
				int index = new Random().nextInt(bound);
				Minion minionAttacking = minions.get(index);
				log.info("adding percept ATTACK to " + startingTeam.toString() + minionAttacking.toString());
				addPercept(startingTeam.toString() + minionAttacking.toString(), Literal.parseLiteral("attack"));
				execute = true;

			} else if (thisTurnCurrentChampion != null) {
				addPercept(startingTeam.toString() + thisTurnCurrentChampion.toString(),
						Literal.parseLiteral("commenceAttack"));
			}
			/* No element in current turn team, other team turn! */
			else {
				log.info("no element in current team");
				bound = otherMinions.size();
				int index = new Random().nextInt(bound);
				Minion minionAttacking = otherMinions.get(index);
				log.info("adding percept to " + otherTeam.toString() + minionAttacking.toString() + " since "
						+ otherTeam.toString() + " minions all died!");
				addPercept(otherTeam.toString() + minionAttacking.toString(), Literal.parseLiteral("attackAgain"));
				execute = false;

			}
		}

		if (action.getFunctor().equals(selectNextEnemy)) {

			Team team = agName.contains("red") ? Team.blueTeam : Team.redTeam;

			List<Minion> teamMinions = teams.get(team);

			Role minionRole = Role.valueOf(action.getTerm(0).toString().toUpperCase());

			Champion enemyChampion = agName.contains("red") ? this.bluTeamChampion : this.redTeamChampion;

			switch (minionRole) {
			/*
			 * if Role is Melee, I cannot hit distance first, all enemy melee minions must
			 * be dead
			 */
			case MELEE:

				List<Minion> teamMinionsOnlyMelee = new ArrayList<Minion>();
				List<Minion> teamMinionsOnlyDistance = new ArrayList<Minion>();

				for (Minion minion : teamMinions) {
					if (minion.getRole() == Role.MELEE) {
						teamMinionsOnlyMelee.add(minion);
					} else {
						teamMinionsOnlyDistance.add(minion);
					}

				}
				int boundMelee = teamMinionsOnlyMelee.size();
				int boundDistance = teamMinionsOnlyDistance.size();
				if (boundMelee > 0) {
					log.info(agName + " focusing melee first!");
					int indexMelee = new Random().nextInt(boundMelee);
					Minion minionReceivingDamage = teamMinionsOnlyMelee.get(indexMelee);

					int attackDamage = 50;
					log.info("adding damage to " + team.toString() + minionReceivingDamage.toString());
					addPercept(team.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;

				} else if (boundDistance > 0) {
					log.info(agName + "no more melee enemies, focus distance now!");

					int indexDistance = new Random().nextInt(boundDistance);
					Minion minionReceivingDamage = teamMinionsOnlyDistance.get(indexDistance);

					int attackDamage = 50;
					log.info("adding damage to " + team.toString() + minionReceivingDamage.toString());
					addPercept(team.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;
				} else {
					/* melee focus champion or turret */
					log.info("adding percept damage to " + team.toString() + enemyChampion.toString());
					addPercept(team.toString() + enemyChampion.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(50) + "," + agName + ")"));
					enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
					return true;
				}

			case DISTANCE:
				int bound = teamMinions.size();

				if (bound > 0) {

					int index = new Random().nextInt(bound);
					Minion minionReceivingDamage = teamMinions.get(index);

					int attackDamage = 50;
					log.info("adding damage to " + team.toString() + minionReceivingDamage.toString());
					addPercept(team.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;
				}

				else {
					/* distance focus champion or turret */
					log.info("adding percept damage to " + team.toString() + enemyChampion.toString());
					addPercept(team.toString() + enemyChampion.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(50) + "," + agName + ")"));
					enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
					return true;
				}

			}

		}
		if (action.getFunctor().equals(updateKill)) {
			log.info(agName + " is dead");
			final String self = action.getTerm(0).toString();
			final Team team = Team.valueOf(action.getTerm(1).toString());
			List<Minion> minions = this.teams.get(team);
			Optional<Minion> optionalMinion = minions.stream().filter(new Predicate<Minion>() {
				public boolean test(Minion minion) {
					String minionName = team.toString() + minion.toString();
					return minionName.equalsIgnoreCase(self);

				}
			}).findAny();
			if (optionalMinion.isPresent()) {
				minions.remove(optionalMinion.get());
				this.teams.put(team, minions);

			}
			log.info("TEAM " + team.toString() + " HAS " + Integer.toString(minions.size()) + " MINIONS!");
			execute = true;

		}
		if (action.getFunctor().contentEquals(createMinions)) {
			log.info("New minions!");
			int number = Integer.parseInt(action.getTerm(0).toString());
			this.addNewMinions(number);
			execute = true;
		}
		if (action.getFunctor().contentEquals(selectTarget)) {

			Team enemyTeam = Team.valueOf(action.getTerm(0).toString());
			Champion enemyChampion = enemyTeam == Team.redTeam ? this.redTeamChampion : this.bluTeamChampion;
			int attackDamage = enemyChampion.getAttackDamage();
			Nexus enemyNexus = enemyTeam == Team.redTeam ? this.redTeamNexus : this.blueTeamNexus;
			double randomChoosing = Double.parseDouble(action.getTerm(1).toString());
			List<Minion> minions = this.teams.get(enemyTeam);

			if (randomChoosing > 0.5 && enemyChampion != null) {
				log.info("adding percept damageFromEnemy to " + enemyTeam.toString() + enemyChampion.toString());
				addPercept(enemyTeam.toString() + enemyChampion.toString(),
						Literal.parseLiteral("damageFromEnemy(" + Integer.toString(50) + "," + agName + ")"));
				enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
				execute = true;
			} else if (randomChoosing < 0.5 && !minions.isEmpty()) {
				Team championTeam = Team.getOtherTeam(enemyTeam);
				this.updatePerceptsForTeamsElements(enemyTeam, championTeam);

				int bound = minions.size();
				int index = new Random().nextInt(bound);
				Minion minionReceivingDamage = minions.get(index);
				addPercept(enemyTeam.toString() + minionReceivingDamage.toString(), Literal.parseLiteral(
						"damageFromEnemyChampion(" + Integer.toString(attackDamage) + "," + agName + ")"));
				minionReceivingDamage.setAttackFromChampion(new Attack(attackDamage, 0, agName));
				execute = true;

			} else if (enemyChampion != null) {
				addPercept(enemyTeam.toString() + enemyChampion.toString(),
						Literal.parseLiteral("damage(" + Integer.toString(50) + "," + agName + ")"));
				enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
				execute = true;
			} else {
				log.info(agName + " is attacking nexus!");
				addPercept(enemyTeam.toString() + enemyNexus.toString(),
						Literal.parseLiteral("damage(" + attackDamage + "," + agName + ")"));
				enemyNexus.setCurrentAttack(new Attack(attackDamage, 0, agName));
				execute = true;
			}

			// log.info("this is champion team " +championTeam);

		}
		return execute;
	}

	private void updatePerceptsForTeamsElements(Team startingTeam, Team otherTeam) {
		List<Minion> minions = this.teams.get(startingTeam);
		for (Minion minion : minions) {
			Attack currentAttack = minion.getCurrentAttack();
			Attack attackFromChampion = minion.getAttackFromChampion();
			boolean removeDamage = false;
			boolean removeDamageFromChampion = false;
			if (currentAttack != null) {
				removeDamage = removePercept(startingTeam.toString() + minion.toString(), Literal.parseLiteral("damage("
						+ Integer.toString(currentAttack.getAttackDamage()) + "," + currentAttack.getFrom() + ")"));
			}
			if (attackFromChampion != null) {
				removePercept(startingTeam.toString() + minion.toString(),
						Literal.parseLiteral(
								"damageFromEnemyChampion(" + Integer.toString(attackFromChampion.getAttackDamage())
										+ "," + attackFromChampion.getFrom() + ")"));
			}

			if (removeDamage) {
				log.info("removing damage from " + startingTeam.toString() + minion.toString() + " is "
						+ Boolean.toString(removeDamage));
			}
			if (removeDamageFromChampion) {
				log.info("removing damagefromEnemyChampion from " + startingTeam.toString() + minion.toString() + " is "
						+ Boolean.toString(removeDamage));
			}

			removePercept(startingTeam.toString() + minion.toString(), Literal.parseLiteral("spawn"));

			boolean removeAttack = removePercept(startingTeam.toString() + minion.toString(),
					Literal.parseLiteral("attack"));
			if (removeAttack)
				log.info("removing attack from " + startingTeam.toString() + minion.toString() + " is "
						+ Boolean.toString(removeDamageFromChampion));
			minion.setCurrentAttack(null);
			minion.setAttackFromChampion(null);
		}

		List<Minion> otherTeamMinions = this.teams.get(otherTeam);
		for (Minion otherMinion : otherTeamMinions) {
			Attack currentAttack = otherMinion.getCurrentAttack();
			Attack attackFromChampion = otherMinion.getAttackFromChampion();
			boolean removeDamage = false;
			boolean removeDamageFromChampion = false;
			if (currentAttack != null) {
				removeDamage = removePercept(otherTeam.toString() + otherMinion.toString(),
						Literal.parseLiteral("damage(" + Integer.toString(currentAttack.getAttackDamage()) + ","
								+ currentAttack.getFrom() + ")"));
			}
			if (attackFromChampion != null) {
				removePercept(otherTeam.toString() + otherMinion.toString(),
						Literal.parseLiteral(
								"damageFromEnemyChampion(" + Integer.toString(attackFromChampion.getAttackDamage())
										+ "," + attackFromChampion.getFrom() + ")"));
			}

			if (removeDamage) {
				log.info("removing damage from " + otherTeam.toString() + otherMinion.toString() + " is "
						+ Boolean.toString(removeDamage));
			}
			if (removeDamageFromChampion) {
				log.info("removing damagefromEnemyChampion from " + otherTeam.toString() + otherMinion.toString()
						+ " is " + Boolean.toString(removeDamageFromChampion));
			}

			removePercept(otherTeam.toString() + otherMinion.toString(), Literal.parseLiteral("spawn"));

			boolean removeAttack = removePercept(otherTeam.toString() + otherMinion.toString(),
					Literal.parseLiteral("attack"));
			if (removeAttack)
				log.info("removing attack from " + otherTeam.toString() + otherMinion.toString() + " is "
						+ Boolean.toString(removeAttack));
			otherMinion.setCurrentAttack(null);
			otherMinion.setAttackFromChampion(null);

			

		}
		/* remove attack percept from champions */
		removePercept("blueTeamGaren", Literal.parseLiteral("commenceAttack"));
		removePercept("redTeamRiven", Literal.parseLiteral("commenceAttack"));
		Attack championBlueAttack = this.bluTeamChampion.getCurrentAttack();
		boolean removeDamagefromBlueChampion = false;
		if (championBlueAttack != null) {
			removeDamagefromBlueChampion = removePercept("blueTeamGaren",
					Literal.parseLiteral("damageFromEnemy(" + Integer.toString(championBlueAttack.getAttackDamage())
							+ "," + championBlueAttack.getFrom() + ")"));
			this.bluTeamChampion.setCurrentAttack(null);
		}
		if (removeDamagefromBlueChampion) {
			log.info("remove percept damage from redTeamGaren");
		}
		Attack championRedAttack = this.redTeamChampion.getCurrentAttack();
		boolean removeDamageFromRedChampion = false;
		if (championRedAttack != null) {
			removeDamageFromRedChampion = removePercept("redTeamRiven",
					Literal.parseLiteral("damageFromEnemy(" + Integer.toString(championRedAttack.getAttackDamage())
							+ "," + championRedAttack.getFrom() + ")"));
			this.redTeamChampion.setCurrentAttack(null);
		}
		if (removeDamageFromRedChampion) {
			log.info("remove percept damage from redTeamRiven");
		}
		/*remove percept from nexus*/
		Attack blueTeamNexusAttack = this.blueTeamNexus.getCurrentAttack();
		if(blueTeamNexusAttack != null) {
			removePercept("blueTeamNexus",
					Literal.parseLiteral("damage(" + Integer.toString(blueTeamNexusAttack.getAttackDamage())
							+ "," + blueTeamNexusAttack.getFrom() + ")"));
		}
		Attack redTeamNexusAttack = this.redTeamNexus.getCurrentAttack();
		if(redTeamNexusAttack != null) {
			removePercept("redTeamNexus",
					Literal.parseLiteral("damage(" + Integer.toString(redTeamNexusAttack.getAttackDamage())
							+ "," + redTeamNexusAttack.getFrom() + ")"));
		}

	}

	private void addNewMinions(int number) {
		for (Team t : Team.values()) {
			List<Minion> minions = this.teams.get(t);

			Minion minion = new Minion(10, number, Role.MELEE);
			addPercept(t.toString() + minion.toString(), Literal.parseLiteral("team(" + t.toString() + ")"));
			addPercept(t.toString() + minion.toString(),
					Literal.parseLiteral("role(" + minion.getRole().toString().toLowerCase() + ")"));
			addPercept(t.toString() + minion.toString(),
					Literal.parseLiteral("self(" + t.toString() + minion.toString() + ")"));

			minions.add(minion);

			teams.put(t, minions);

		}
	}

	private void createArena() {

		for (Team t : Team.values()) {

			List<Minion> elements = new ArrayList<Minion>();
			for (int i = 1; i <= numberOfMeleeMinions; i++) {

				Minion minion = new Minion(10, i, Role.MELEE);
				log.info(t.toString() + minion.toString());
				addPercept(t.toString() + minion.toString(), Literal.parseLiteral("team(" + t.toString() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("role(" + minion.getRole().toString().toLowerCase() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("self(" + t.toString() + minion.toString() + ")"));

				elements.add(minion);

			}
			int distanceMinionStartingNumber = numberOfMeleeMinions + 1;
			for (int i = distanceMinionStartingNumber; i <= numberOfMinions; i++) {

				Minion minion = new Minion(10, i, Role.DISTANCE);
				log.info(t.toString() + minion.toString());
				addPercept(t.toString() + minion.toString(), Literal.parseLiteral("team(" + t.toString() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("role(" + minion.getRole().toString().toLowerCase() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("self(" + t.toString() + minion.toString() + ")"));

				elements.add(minion);

			}

			teams.put(t, elements);

		}
		this.bluTeamChampion = new Champion(40, 0, "Garen");
		addPercept(Team.blueTeam.toString() + this.bluTeamChampion.toString(),
				Literal.parseLiteral("team(" + Team.blueTeam.toString() + ")"));
		addPercept(Team.blueTeam.toString() + this.bluTeamChampion.toString(),
				Literal.parseLiteral("self(blueTeamGaren)"));
		this.redTeamChampion = new Champion(40, 0, "Riven");
		addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
				Literal.parseLiteral("team(" + Team.redTeam.toString() + ")"));
		addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
				Literal.parseLiteral("self(redTeamRiven)"));

	}

	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
}
