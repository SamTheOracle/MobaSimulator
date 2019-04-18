package model;

public class Attack {
	
	private int attackDamage;
	
	private int abilityPower;
	
	private  String from;
	
	public Attack(int attackDamage, int abilityPower, String agName) {
		this.attackDamage = attackDamage;
		this.abilityPower = abilityPower;
		this.from = agName;
	}

	public int getAttackDamage() {
		return attackDamage;
	}

	public String getFrom() {
		return from;
	}

	public void String(String from) {
		this.from = from;
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
	

}
