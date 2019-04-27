package model;

public class Nexus {
	
	Attack currentAttack;

	public Attack getCurrentAttack() {
		return currentAttack;
	}

	public void setCurrentAttack(Attack currentAttack) {
		this.currentAttack = currentAttack;
	}
	@Override
	public String toString() {
		return "Nexus";
	}
	

}
