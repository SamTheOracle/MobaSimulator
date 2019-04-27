package model;

public class Nexus {
	
	private Attack currentAttack;
	private Attack currentChampionAttack;
	
	

	public Attack getCurrentChampionAttack() {
		return currentChampionAttack;
	}

	public void setCurrentChampionAttack(Attack currentChampionAttack) {
		this.currentChampionAttack = currentChampionAttack;
	}

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
