package model;

public enum Team {
	
	redTeam,blueTeam;
	
	public static Team getOtherTeam(Team currentTeam) {
		Team teamToReturn = null;
		switch(currentTeam) {
		case blueTeam:
			teamToReturn = Team.redTeam;
			break;
		case redTeam:
			teamToReturn = Team.blueTeam;
			break;
		}
		return teamToReturn;
		
		
		
	}

}
