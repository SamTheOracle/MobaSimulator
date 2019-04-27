// Agent nexus in project mobasimulator

/* Initial beliefs and rules */

hitPoints(500).

/* Initial goals */
+damage(X,Y) <- true; !receiveDamage.

/* Plans */

+!start : true <- .print("hello world.").
+!receiveDamage : damage(X,Y) & team(T) & hitPoints(Z)<- .print("received damage from ",Y)
								 ?amIAlive;
								-+hitPoints(Z - X);
								//dopo che il minion ha ricevuto il danno,
																	//tocca al campione nemico di attaccare
								 .send(gameMaster,achieve,swapTurn(T));.kill_agent(X);.	
								 
-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED");.								
										//.
								
									
				   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 
