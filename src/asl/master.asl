// Agent master in project mobasimulator

/* Initial beliefs and rules */
currentChampionKillRedTeam(0).
currentChampionKillBlueTeam(0).

numberOfMinions(1).





/* Initial goals */
!start.
!prepareArena.



+!swapTurn(redTeam) : true <-.wait(3000);
					 .print("")
					 .print("It is redTeam turn!");
					 .print("");
					 turn(redTeam).
+!swapTurn(blueTeam) : true <-.wait(3000);
							 .print("")
							 .print("It is blueTeam turn!");
							 .print("");
							  turn(blueTeam).
-!swapTurn(blueTeam): true <- .print("Blue team minions all died!");!spawn(blueTeam).
-!swapTurn(redTeam): true <- .print("Red team minions all died");!spawn(redTeam).
/* Plans */

+!start : true <- .print("Getting the game ready").

+!prepareArena: true <- prepareArena
						?startTurn;.
						

+?startTurn <- .random(R);if(R > 0.01){
							turn(blueTeam);			
						}
						else{
							turn(redTeam);
						}.
//+!spawn(blueTeam) : minionId(X) <- MinionId= X+1;-+minionId(X+1);.concat("redTeamMinion",MinionId,MinionNameRed);
//						.print("CREATING MINION ",MinionNameRed);
//						.create_agent(MinionNameRed,"minion.asl");
//						.concat("blueTeamMinion",MinionId,MinionNameBlue);
//						.print("CREATING MINION ",MinionNameBlue);
//						 .create_agent(MinionNameBlue,"minion.asl");
//						 createMinions(X);.
						 
						 //turn(blueTeam).
//+!spawn(redTeam) : minionId(X) <- MinionId= X+1;-+minionId(X+1);.concat("redTeamMinion",MinionId,MinionNameRed);
//						.print("CREATING MINION ",MinionNameRed);
//						.create_agent(MinionNameRed,"minion.asl");
//						.concat("blueTeamMinion",MinionId,MinionNameBlue);
//						.print("CREATING MINION ",MinionNameBlue);
//						 .create_agent(MinionNameBlue,"minion.asl");
//						 createMinions(X);.
//						 //turn(blueTeam).

+!spawn(blueTeam) <- 
					while(numberOfMinions(X) & X < 7){
						 .concat("blueTeamMinion",X,MinionNameBlue);
						 .create_agent(MinionNameBlue,"minion.asl");
						 -+numberOfMinions(X+1);
						 .print("inside loop")
						 };
						 -+numberOfMinions(1);
						 createMinions(blueTeam).
+!spawn(redTeam)  <- 
					while(numberOfMinions(X) & X < 7){
						 .concat("redTeamMinion",X,MinionNameRed);
						 .create_agent(MinionNameRed,"minion.asl");
						 -+numberOfMinions(X+1);
						 .print("inside loop")
						 };
						 -+numberOfMinions(1);
						 createMinions(redTeam).
				  
+!updateChampionKill(redTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y)<- -+currentChampionKillBlueTeam(Y + 1);
																.print("Score is: ", Y + 1, " Blue Team to ", X, " Red Team");
																//update and respawn champion
																turn(redTeam).
+!updateChampionKill(blueTeam) : currentChampionKillRedTeam(X) 
							& currentChampionKillBlueTeam(Y)<- -+currentChampionKillRedTeam(X + 1);
																.print("Score is: ", X + 1, " Red Team to ", X, " Blue Team");
																turn(blueTeam).
																

					 
						 					
