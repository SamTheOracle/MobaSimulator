// Agent minion in project mobasimulator

/* Initial beliefs and rules */




+role(melee)  <- -+hitPoints(150).
+role(distance) <- -+hitPoints(100).
					
+damage(X,Y) : true <- .print("receive damage ",X," from ",Y);!receiveDamage.


+damageFromEnemyChampion(X,Y): true <- .print("receive damage ",X," from ",Y);!receiveDamageFromChampion.
+attackNexus : true <- .print("Attack enemy Nexus");!fight.
+attack :true<- .print("attack!");!fight.

+!fight : role(X)<- .print("I'll focus the enemy ", X);selectNextEnemy(X);.

+!receiveDamageFromChampion : damageFromEnemyChampion(X,Z) & hitPoints(Y) & team(T)<- 
										.print(Z, " just hit me!");	
										?amIAliveWhenChampionHit;
										-+hitPoints(Y - X);
										 .send(gameMaster,achieve,swapTurn(T)).

																		
+!receiveDamage : damage(X,Z) & hitPoints(Y) & team(T)<- 
										.print("Ouch, that hurt!");	
										?amIAlive;		
										-+hitPoints(Y - X);
										if(T == redTeam){
											.send(blueTeamGaren,achieve,attack(T));
										}else{
											.send(redTeamRiven,achieve,attack(T));
										}.
-!receiveDamage : team(T) <- .send(gameMaster,achieve,swapTurn(T)).
										
-?amIAlive :self(X) & team(Y) <- .print("I was killed!");updateKill(X,Y);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.										
								
									
				   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 

+?amIAliveWhenChampionHit : damageFromEnemyChampion(X,_) & hitPoints(Y) <- Y > X.
-?amIAliveWhenChampionHit :self(X) & team(Y) <- .print("I WAS KILLED");updateKill(X,Y);.print("I am killing myself ",X);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.			
