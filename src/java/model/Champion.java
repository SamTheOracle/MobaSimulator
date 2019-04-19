package model;

public class Champion {
	
	private int attackDamage,abilityPower;
	private String name;
	private Attack currentAttack,championAttack;
	
	private Ability ability;

	public Champion(int attackDamage, int abilityPower, String name) {
		this.attackDamage = attackDamage;
		this.abilityPower = abilityPower;
		this.name = name;
		this.ability = new Ability();
	}
	
	

	public Attack getChampionAttack() {
		return championAttack;
	}



	public void setChampionAttack(Attack championAttack) {
		this.championAttack = championAttack;
	}



	public Ability getAbility() {
		return ability;
	}



	public void setAbility(Ability ability) {
		this.ability = ability;
	}



	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getAttackDamage() {
		return attackDamage;
	}

	public void setAttackDamage(int attackDamage) {
		this.attackDamage = attackDamage;
	}

	public int getAbilityPower() {
		return abilityPower;
	}

	public void setAbilityPower(int abilityPower) {
		this.abilityPower = abilityPower;
	}

	public Attack getCurrentAttack() {
		return currentAttack;
	}

	public void setCurrentAttack(Attack currentAttack) {
		this.currentAttack = currentAttack;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	

}
