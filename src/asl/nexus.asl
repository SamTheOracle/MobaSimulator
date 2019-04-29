// Agent nexus in project mobasimulator

/* Initial beliefs and rules */

hitPoints(150).

/* Initial goals */
+damage(X,Y) <- true; !receiveDamage.
+damageFromChampion(X,Y) <- true; !receiveDamageFromChampion.

/* Plans */

+!start : true <- .print("hello world.").
+!receiveDamage : damage(X,Y) & team(T) & hitPoints(Z)<- .print("received damage from ",Y)
								 ?amIAlive;
								-+hitPoints(Z - X);
								
								if(T == redTeam){
									.send(blueTeamGaren,achieve,attack(redTeam));
								}
								else{
									.send(redTeamRiven,achieve,attack(blueTeam));
								}.
+!receiveDamageFromChampion :damageFromChampion(X,Y) & team(T) & hitPoints(Z) <- .print("received damage from ",Y);
																			      ?amIAliveFromChampionHit;
																			      -+hitPoints(Z - X);
																			      .print("HitPoints ", Z - X);
																				 .send(gameMaster,achieve,spawn(T));.									 
		
-?amIAliveFromChampionHit : self(X) & team(Y) <- .print("I WAS KILLED");.send(gameMaster,achieve,endGame(Y));.kill_agent(X).											   						    
+?amIAliveFromChampionHit : damageFromChampion(X,_) & hitPoints(Y) <- Y > X. 			
			 
-?amIAlive : team(Y) <- .print("I WAS KILLED");.send(gameMaster,achieve,endGame(Y)).											   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 
