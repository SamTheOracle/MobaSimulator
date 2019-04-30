
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
	private Champion blueTeamChampion;
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
	

		if (action.equals(prepareArena)) {
			this.createArena();

			return true;

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
			this.updatePerceptsForTeamsElements();

			int bound = minions.size();

			Champion thisTurnCurrentChampion = startingTeam == Team.blueTeam ? this.blueTeamChampion
					: this.redTeamChampion;
			Champion otherTeamChampion = startingTeam == Team.blueTeam ? this.redTeamChampion : this.blueTeamChampion;

			if (bound > 0) {
				int index = new Random().nextInt(bound);
				Minion minionAttacking = minions.get(index);
		
				addPercept(startingTeam.toString() + minionAttacking.toString(), Literal.parseLiteral("attack"));
				return true;

			} else if (thisTurnCurrentChampion != null) {
				addPercept(startingTeam.toString() + thisTurnCurrentChampion.toString(),
						Literal.parseLiteral("commenceAttack"));
				return true;
			}
			/* No element in current turn team, other team attack! */
			else {
				log.info("no element in current team, " + otherTeam.toString() + " will attack nexus!");

	
				bound = otherMinions.size();
				if (bound > 0) {
					int index = new Random().nextInt(bound);
					Minion minionAttacking = otherMinions.get(index);

					addPercept(otherTeam.toString() + minionAttacking.toString(), Literal.parseLiteral("attackNexus"));
				} else if (otherTeamChampion != null) {

					addPercept(otherTeam.toString() + otherTeamChampion.toString(),
							Literal.parseLiteral("attackNexus"));
				}

				return true;

			}
		}

		if (action.getFunctor().equals(selectNextEnemy)) {

			this.updatePerceptsForTeamsElements();
			final String agentName = agName;
			final Team enemyTeam = agName.contains("red") ? Team.blueTeam : Team.redTeam;
			final Team currentTeam = Team.getOtherTeam(enemyTeam);

			this.updatePerceptsForTeamsElements();

			Nexus enemyNexus = agName.contains("red") ? this.blueTeamNexus : this.redTeamNexus;
			List<Minion> enemyTeamMinions = teams.get(enemyTeam);
			List<Minion> currentTeamMinions = teams.get(currentTeam);

			Role minionRole = Role.valueOf(action.getTerm(0).toString().toUpperCase());

			Champion enemyChampion = agName.contains("red") ? this.blueTeamChampion : this.redTeamChampion;

			Minion minionDoingDamage = currentTeamMinions.stream().filter(new Predicate<Minion>() {

				public boolean test(Minion t) {
					// TODO Auto-generated method stub
					String currentMinionName = currentTeam.toString() + t.toString();
					return currentMinionName.equalsIgnoreCase(agentName);
				}
			}).findAny().get();

			int attackDamage = minionDoingDamage.getAttackDamage();

			switch (minionRole) {
			/*
			 * if Role is Melee, I cannot hit distance first, all enemy melee minions must
			 * be dead
			 */
			case MELEE:

				List<Minion> teamMinionsOnlyMelee = new ArrayList<Minion>();
				List<Minion> teamMinionsOnlyDistance = new ArrayList<Minion>();

				for (Minion minion : enemyTeamMinions) {
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
					addPercept(enemyTeam.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;

				} else if (boundDistance > 0) {
					log.info(agName + "no more melee enemies, focus distance now!");

					int indexDistance = new Random().nextInt(boundDistance);
					Minion minionReceivingDamage = teamMinionsOnlyDistance.get(indexDistance);


					addPercept(enemyTeam.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;
				} else if (enemyChampion != null) {
					/* melee focus champion or turret */

					addPercept(enemyTeam.toString() + enemyChampion.toString(),
							Literal.parseLiteral("damageFromEnemy(" + Integer.toString(50) + "," + agName + ")"));
					enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
					return true;
				} else {
					addPercept(enemyTeam.toString() + enemyNexus.toString(),
							Literal.parseLiteral("damage(" + attackDamage + "," + agName + ")"));
					enemyNexus.setCurrentAttack(new Attack(attackDamage, 0, agName));

					return true;
				}

			case DISTANCE:
				int bound = enemyTeamMinions.size();

				if (bound > 0) {

					int index = new Random().nextInt(bound);
					Minion minionReceivingDamage = enemyTeamMinions.get(index);

		
					addPercept(enemyTeam.toString() + minionReceivingDamage.toString(),
							Literal.parseLiteral("damage(" + Integer.toString(attackDamage) + "," + agName + ")"));
					minionReceivingDamage.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;
				}

				else if (enemyChampion != null) {


					addPercept(enemyTeam.toString() + enemyChampion.toString(),
							Literal.parseLiteral("damageFromEnemy(" + Integer.toString(50) + "," + agName + ")"));
					enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
					return true;
				} else {
					log.info(agName + " is attacking nexus!");
					addPercept(enemyTeam.toString() + enemyNexus.toString(),
							Literal.parseLiteral("damage(" + attackDamage + "," + agName + ")"));
					enemyNexus.setCurrentAttack(new Attack(attackDamage, 0, agName));
					return true;
				}

			}

		}
		if (action.getFunctor().equals(updateKill)) {
			this.updatePerceptsForTeamsElements();
			log.info(agName + " is dead");
			if (agName.contains("Garen")) {
				this.blueTeamChampion = null;
				return true;
			}
			if (agName.contains("Riven")) {
				this.redTeamChampion = null;
				return true;
			}

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
			return true;

		}
		if (action.getFunctor().contentEquals(createMinions)) {
			this.updatePerceptsForTeamsElements();

			final Team currentTeam = Team.valueOf(action.getTerm(0).toString());
			log.info("New minions and champion for " + currentTeam.toString());

			List<Minion> elements = new ArrayList<Minion>();
			for (int i = 1; i <= numberOfMeleeMinions; i++) {

				Minion minion = new Minion(10, i, Role.MELEE);
				removePercept(currentTeam.toString() + minion.toString(),
						Literal.parseLiteral("random(" + currentTeam.toString() + ")"));


				elements.add(minion);

			}
			int distanceMinionStartingNumber = numberOfMeleeMinions + 1;
			for (int i = distanceMinionStartingNumber; i <= numberOfMinions; i++) {
				
				Minion minion = new Minion(20, i, Role.DISTANCE);
				removePercept(currentTeam.toString() + minion.toString(),
						Literal.parseLiteral("random(" + currentTeam.toString() + ")"));
				elements.add(minion);

			}

			teams.put(currentTeam, elements);

			if (currentTeam == Team.redTeam) {
				this.redTeamChampion = new Champion(50, 0, "Riven");
				removePercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
						Literal.parseLiteral("random(" + currentTeam.toString() + ")"));
				addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
						Literal.parseLiteral("team(" + Team.redTeam.toString() + ")"));
				addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
						Literal.parseLiteral("self(redTeamRiven)"));
			} else {
				this.blueTeamChampion = new Champion(50, 0, "Garen");
				removePercept(Team.blueTeam.toString() + this.blueTeamChampion.toString(),
						Literal.parseLiteral("random(" + currentTeam.toString() + ")"));
				addPercept(Team.blueTeam.toString() + this.blueTeamChampion.toString(),
						Literal.parseLiteral("team(" + Team.blueTeam.toString() + ")"));
				addPercept(Team.blueTeam.toString() + this.blueTeamChampion.toString(),
						Literal.parseLiteral("self(blueTeamGaren)"));
			}

			return true;
		}
		if (action.getFunctor().contentEquals(selectTarget)) {

			this.updatePerceptsForTeamsElements();
			log.info(agName + " is choosing target");
			Team enemyTeam = Team.valueOf(action.getTerm(0).toString());
			Champion enemyChampion = enemyTeam == Team.redTeam ? this.redTeamChampion : this.blueTeamChampion;
			float attackDamageDouble = Float.parseFloat(action.getTerm(1).toString());
			int attackDamage = Math.round(attackDamageDouble);
			Nexus enemyNexus = enemyTeam == Team.redTeam ? this.redTeamNexus : this.blueTeamNexus;
			List<Minion> minions = this.teams.get(enemyTeam);
	
			if (!minions.isEmpty()) {

				int bound = minions.size();
				int index = new Random().nextInt(bound);
				Minion minionReceivingDamage = minions.get(index);

				addPercept(enemyTeam.toString() + minionReceivingDamage.toString(), Literal.parseLiteral(
						"damageFromEnemyChampion(" + Integer.toString(attackDamage) + "," + agName + ")"));
				minionReceivingDamage.setAttackFromChampion(new Attack(attackDamage, 0, agName));
				return true;

			} else if (enemyChampion != null) {

				addPercept(enemyTeam.toString() + enemyChampion.toString(),
						Literal.parseLiteral("damageFromEnemy(" + Integer.toString(50) + "," + agName + ")"));
				enemyChampion.setCurrentAttack(new Attack(50, 0, agName));
				return true;
			} else {
				log.info(agName + " is attacking nexus!");
				addPercept(enemyTeam.toString() + enemyNexus.toString(),
						Literal.parseLiteral("damageFromChampion(" + attackDamage + "," + agName + ")"));
				enemyNexus.setCurrentChampionAttack(new Attack(attackDamage, 0, agName));
				return true;
			}


		}
		return true;
	}

	private void updatePerceptsForTeamsElements() {
		List<Minion> minions = this.teams.get(Team.redTeam);
		for (Minion minion : minions) {
			Attack currentAttack = minion.getCurrentAttack();
			Attack attackFromChampion = minion.getAttackFromChampion();
			if (currentAttack != null) {
				removePercept(Team.redTeam.toString() + minion.toString(), Literal.parseLiteral("damage("
						+ Integer.toString(currentAttack.getAttackDamage()) + "," + currentAttack.getFrom() + ")"));
			}
			if (attackFromChampion != null) {
				removePercept(Team.redTeam.toString() + minion.toString(),
						Literal.parseLiteral(
								"damageFromEnemyChampion(" + Integer.toString(attackFromChampion.getAttackDamage())
										+ "," + attackFromChampion.getFrom() + ")"));
			}

		
			removePercept(Team.redTeam.toString() + minion.toString(), Literal.parseLiteral("spawn"));

			removePercept(Team.redTeam.toString() + minion.toString(),
					Literal.parseLiteral("attack"));
			
			removePercept(Team.redTeam.toString() + minion.toString(), Literal.parseLiteral("attackNexus"));
			minion.setCurrentAttack(null);
			minion.setAttackFromChampion(null);
		}

		List<Minion> otherTeamMinions = this.teams.get(Team.blueTeam);
		for (Minion otherMinion : otherTeamMinions) {
			Attack currentAttack = otherMinion.getCurrentAttack();
			Attack attackFromChampion = otherMinion.getAttackFromChampion();
			if (currentAttack != null) {
				removePercept(Team.blueTeam.toString() + otherMinion.toString(),
						Literal.parseLiteral("damage(" + Integer.toString(currentAttack.getAttackDamage()) + ","
								+ currentAttack.getFrom() + ")"));
			}
			if (attackFromChampion != null) {
								removePercept(Team.blueTeam.toString() + otherMinion.toString(),
						Literal.parseLiteral(
								"damageFromEnemyChampion(" + Integer.toString(attackFromChampion.getAttackDamage())
										+ "," + attackFromChampion.getFrom() + ")"));
			}

	

			removePercept(Team.blueTeam.toString() + otherMinion.toString(), Literal.parseLiteral("spawn"));

			boolean removeAttack = removePercept(Team.blueTeam.toString() + otherMinion.toString(),
					Literal.parseLiteral("attack"));
			if (removeAttack)
			
			removePercept(Team.blueTeam.toString() + otherMinion.toString(), Literal.parseLiteral("attackNexus"));
			otherMinion.setCurrentAttack(null);
			otherMinion.setAttackFromChampion(null);

		}
		/* remove attack percept from champions */
		if (this.blueTeamChampion != null) {
			removePercept("blueTeamGaren", Literal.parseLiteral("attackNexus"));
			removePercept("blueTeamGaren", Literal.parseLiteral("commenceAttack"));
			Attack championBlueAttack = this.blueTeamChampion.getCurrentAttack();
			if (championBlueAttack != null) {
				removePercept("blueTeamGaren",
						Literal.parseLiteral("damageFromEnemy(" + Integer.toString(championBlueAttack.getAttackDamage())
								+ "," + championBlueAttack.getFrom() + ")"));
				this.blueTeamChampion.setCurrentAttack(null);
			}
		
		}
		if (this.redTeamChampion != null) {
			removePercept("redTeamRiven", Literal.parseLiteral("attackNexus"));
			removePercept("redTeamRiven", Literal.parseLiteral("commenceAttack"));

			Attack championRedAttack = this.redTeamChampion.getCurrentAttack();
			if (championRedAttack != null) {
				removePercept("redTeamRiven",
						Literal.parseLiteral("damageFromEnemy(" + Integer.toString(championRedAttack.getAttackDamage())
								+ "," + championRedAttack.getFrom() + ")"));
				this.redTeamChampion.setCurrentAttack(null);
			}
			
		}

		/* remove percept from nexus */
		Attack blueTeamNexusAttack = this.blueTeamNexus.getCurrentAttack();
		if (blueTeamNexusAttack != null) {
			removePercept("blueTeamNexus",
					Literal.parseLiteral("damage(" + Integer.toString(blueTeamNexusAttack.getAttackDamage()) + ","
							+ blueTeamNexusAttack.getFrom() + ")"));
		}
		Attack blueTeamNexusChampionAttack = this.blueTeamNexus.getCurrentChampionAttack();
		if (blueTeamNexusChampionAttack != null) {
			removePercept("blueTeamNexus",
					Literal.parseLiteral(
							"damageFromChampion(" + Integer.toString(blueTeamNexusChampionAttack.getAttackDamage())
									+ "," + blueTeamNexusChampionAttack.getFrom() + ")"));
		}
		Attack redTeamNexusAttack = this.redTeamNexus.getCurrentAttack();
		if (redTeamNexusAttack != null) {
			removePercept("redTeamNexus",
					Literal.parseLiteral("damage(" + Integer.toString(redTeamNexusAttack.getAttackDamage()) + ","
							+ redTeamNexusAttack.getFrom() + ")"));
		}
		Attack redTeamNexusAttackChampionAttack = this.redTeamNexus.getCurrentChampionAttack();
		if (redTeamNexusAttackChampionAttack != null) {
			removePercept("redTeamNexus",
					Literal.parseLiteral(
							"damageFromChampion(" + Integer.toString(redTeamNexusAttackChampionAttack.getAttackDamage())
									+ "," + redTeamNexusAttackChampionAttack.getFrom() + ")"));
		}
		this.blueTeamNexus.setCurrentAttack(null);
		this.blueTeamNexus.setCurrentChampionAttack(null);
		this.redTeamNexus.setCurrentAttack(null);
		this.redTeamNexus.setCurrentChampionAttack(null);

	}

	private void createArena() {

		for (Team t : Team.values()) {

			List<Minion> elements = new ArrayList<Minion>();
			for (int i = 1; i <= numberOfMeleeMinions; i++) {

				Minion minion = new Minion(25, i, Role.MELEE);
				addPercept(t.toString() + minion.toString(), Literal.parseLiteral("team(" + t.toString() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("role(" + minion.getRole().toString().toLowerCase() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("self(" + t.toString() + minion.toString() + ")"));

				elements.add(minion);

			}
			int distanceMinionStartingNumber = numberOfMeleeMinions + 1;
			for (int i = distanceMinionStartingNumber; i <= numberOfMinions; i++) {

				Minion minion = new Minion(50, i, Role.DISTANCE);
				addPercept(t.toString() + minion.toString(), Literal.parseLiteral("team(" + t.toString() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("role(" + minion.getRole().toString().toLowerCase() + ")"));
				addPercept(t.toString() + minion.toString(),
						Literal.parseLiteral("self(" + t.toString() + minion.toString() + ")"));

				elements.add(minion);

			}

			teams.put(t, elements);

		}
		this.blueTeamChampion = new Champion(40, 0, "Garen");
		addPercept(Team.blueTeam.toString() + this.blueTeamChampion.toString(),
				Literal.parseLiteral("team(" + Team.blueTeam.toString() + ")"));
		addPercept(Team.blueTeam.toString() + this.blueTeamChampion.toString(),
				Literal.parseLiteral("self(blueTeamGaren)"));
		this.redTeamChampion = new Champion(40, 0, "Riven");
		addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
				Literal.parseLiteral("team(" + Team.redTeam.toString() + ")"));
		addPercept(Team.redTeam.toString() + this.redTeamChampion.toString(),
				Literal.parseLiteral("self(redTeamRiven)"));
		this.blueTeamNexus = new Nexus();
		addPercept(Team.blueTeam.toString() + this.blueTeamNexus.toString(),
				Literal.parseLiteral("team(" + Team.blueTeam.toString() + ")"));
		addPercept(Team.blueTeam.toString() + this.blueTeamNexus.toString(),
				Literal.parseLiteral("self(blueTeamNexus)"));
		this.redTeamNexus = new Nexus();
		addPercept(Team.redTeam.toString() + this.redTeamNexus.toString(),
				Literal.parseLiteral("team(" + Team.redTeam.toString() + ")"));
		addPercept(Team.redTeam.toString() + this.redTeamNexus.toString(), Literal.parseLiteral("self(redTeamNexus)"));
	}

	/** Called before the end of MAS execution */
	@Override
	public void stop() {
		super.stop();
	}
}
