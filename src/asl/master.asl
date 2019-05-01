// Agent master in project mobasimulator

/* Initial beliefs and rules */
currentChampionKillRedTeam(0).
currentChampionKillBlueTeam(0).

numberOfMinions(1).





/* Initial goals */
!startGame.




/* Plans */
+!startGame: true <- prepareArena
						?startTurn;.
						

+?startTurn <- .random(R);if(R > 0.5){
							turn(blueTeam);			
						}
						else{
							turn(redTeam);
						}.

+!swapTurn(redTeam) : true <-.wait(2000);
					 turn(redTeam).
+!swapTurn(blueTeam) : true <-.wait(2000);
							  turn(blueTeam).
-!swapTurn(blueTeam): true <- .print("Blue team minions all died!");turn(blueTeam).
-!swapTurn(redTeam): true <- .print("Red team minions all died");turn(redTeam).

+!start : true <- .print("Getting the game ready").



+!spawn(blueTeam) <- 
					while(numberOfMinions(X) & X < 7){
						 .concat("blueTeamMinion",X,MinionNameBlue);
						 .create_agent(MinionNameBlue,"minion.asl");
						 -+numberOfMinions(X+1);
						 };
						 -+numberOfMinions(1);
						 .create_agent("blueTeamGaren","champion.asl");
						 createMinions(blueTeam);
						 turn(blueTeam).
+!spawn(redTeam)  <- 
					while(numberOfMinions(X) & X < 7){
						 .concat("redTeamMinion",X,MinionNameRed);
						 .create_agent(MinionNameRed,"minion.asl");
						 -+numberOfMinions(X+1);
						 };
						 -+numberOfMinions(1);
						 .create_agent("redTeamRiven","champion.asl");
						  createMinions(redTeam);
						 turn(blueTeam).
				  
+!updateChampionKill(redTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y)<- -+currentChampionKillBlueTeam(Y + 1);
																.print("Score is: ", Y + 1, " Blue Team to ", X, " Red Team");
																turn(redTeam).
+!updateChampionKill(blueTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y)<- -+currentChampionKillRedTeam(X + 1);
																.print("Score is: ", X + 1, " Red Team to ", X, " Blue Team");
																turn(blueTeam).
																
+!endGame(redTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y) <- .print("Blue Team destroyed Red Team Nexus! ", X, " to ",Y);.wait(2000000); .stopMAS.
+!endGame(blueTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y) <- .print("Red Team destroyed Blue Team Nexus! ", Y, " to ", X);.wait(2000000); .stopMAS.
																					  
					 
						 					
