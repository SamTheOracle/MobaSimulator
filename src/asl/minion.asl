// Agent minion in project mobasimulator

/* Initial beliefs and rules */




/*when I receive "spawn" percept, I execute plan !***    */
+spawn <- .print("I'm going forward").
+role(melee)  <- -+hitPoints(150).
+role(distance) <- -+hitPoints(100).
					
+damage(X,Y) : true <- .print("receive damage ",X," from ",Y);!receiveDamage.


+damageFromEnemyChampion(X,Y): true <- .print("receive damage ",X," from ",Y);!receiveDamageFromChampion.
+attackTurretOrChampion : true <- .print("attack!");!fight.
+attack :true<- .print("attack!");!fight.

+!fight : role(X)<- .print("I'll focus the enemy ", X);selectNextEnemy(X);.

-!fight : team(Y) <- .print("ciao").
+!receiveDamageFromChampion : damageFromEnemyChampion(X,Z) & hitPoints(Y) & team(T)<- 
										.print(Z, " just hit me wtf!");	
										?amIAliveWhenChampionHit;
										-+hitPoints(Y - X);
										.print("updating hi points when champion hit");	
										 .send(gameMaster,achieve,swapTurn(T)).
//+!receiveDamageFromTurret : damageFromTurret(X,Z) & hitPoints(Y) & team(T) <-
//																			.print(Z, " just hit me wtf!");
//																			
+!receiveDamage : damage(X,Z) & hitPoints(Y) & team(T)<- 
										.print("Ouch, that hurt!");	
										?amIAlive;		
										-+hitPoints(Y - X);
										//dopo che il minion ha ricevuto il danno,
																	//tocca al campione nemico di attaccare
										if(T == redTeam){
											.send(blueTeamGaren,achieve,attack(T));
										}else{
											.send(redTeamRiven,achieve,attack(T));
										}.
//enemy champion is dead
-!receiveDamage : team(T) <- .print("receive damage failed").send(gameMaster,achieve,swapTurn(T)).
										
-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED");updateKill(X,Y);.print("I am killing myself ",X);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.										
										//.
								
									
				   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 

+?amIAliveWhenChampionHit : damageFromEnemyChampion(X,_) & hitPoints(Y) <- Y > X.
-?amIAliveWhenChampionHit :self(X) & team(Y) <- .print("I WAS KILLED");updateKill(X,Y);.print("I am killing myself ",X);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.			
