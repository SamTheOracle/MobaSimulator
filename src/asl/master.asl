// Agent master in project mobasimulator

/* Initial beliefs and rules */
currentChampionKillRedTeam(0).
currentChampionKillBlueTeam(0).

minionId(6).





/* Initial goals */
!start.
!prepareArena.



+!swapTurn(redTeam) : true <-//.wait(4000);
					 .print("")
					 .print("It is redTeam turn!");
					 .print("");
					 turn(redTeam).
+!swapTurn(blueTeam) : true <-//.wait(4000);
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
+!spawn(blueTeam) : minionId(X) <- .wait(5000);MinionId= X+1;-+minionId(X+1);.concat("redTeamMinion",MinionId,MinionNameRed);
						.print("CREATING MINION ",MinionNameRed);
						.create_agent(MinionNameRed,"minion.asl");
						.concat("blueTeamMinion",MinionId,MinionNameBlue);
						.print("CREATING MINION ",MinionNameBlue);
						 .create_agent(MinionNameBlue,"minion.asl");
						 createMinions(X);.
						 //turn(blueTeam).
+!spawn(redTeam) : minionId(X) <- .wait(5000);MinionId= X+1;-+minionId(X+1);.concat("redTeamMinion",MinionId,MinionNameRed);
						.print("CREATING MINION ",MinionNameRed);
						.create_agent(MinionNameRed,"minion.asl");
						.concat("blueTeamMinion",MinionId,MinionNameBlue);
						.print("CREATING MINION ",MinionNameBlue);
						 .create_agent(MinionNameBlue,"minion.asl");
						 createMinions(X);.
						 //turn(blueTeam).
						 
						 					
