// Agent champion in project mobasimulator

/* Initial beliefs and rules */
hitPoints(200).
level(1).



/* Initial goals */
!start.
+damageFromEnemy(X,Y) : true <- .print("Ouch! I was hit by ", Y, " for ",X," damage");!receiveDamage.

+commenceAttack : team(X) <-.print("Attack!");if(X == redTeam){
						!attack(blueTeam)
						}
						else{
							!attack(redTeam)
							}.
+attackNexus: team(X) <-.print("Attack enemy nexus!");if(X == redTeam){
						!attack(blueTeam)
						}
						else{
							!attack(redTeam)
							}.							
/* Plans */


+!start : true <- .print("Ready to fight!").
+!attack(redTeam) : level(X) <- .print("my turn");.random(R);if(R > 0.5){
										.print("I will struck you with my powerful ability!")
										DamageModifier = (50 * X * 10)/100;
										selectTarget(redTeam, 50 + DamageModifier );
										}
										else{
											.print("I will hit you with my sword!")
											selectTarget(redTeam,50);
										}.
					
+!attack(blueTeam) : level(X) <- .print("my turn");.random(R);if(R > 0.5){
										.print("I will struck you with my powerful ability!")
										DamageModifier = (50 * X * 10)/100;
										selectTarget(blueTeam, 50 + DamageModifier );
										}
										else{
											.print("I will hit you with my sword!")
											selectTarget(blueTeam,50);
										}.

+!receiveDamage : team(T) & damageFromEnemy(X,Y) & hitPoints(Z)<-?amIAlive;
										 -+hitPoints(Z - X);
										  if(T == redTeam){
										  	.send(gameMaster,achieve,swapTurn(redTeam));
										  }
										  else{
										  	.send(gameMaster,achieve,swapTurn(blueTeam));
										  }.


-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED ", X, " team ", Y);updateKill(X,Y);.print("I am killing myself ",X);
								  
								  if(Y == redTeam){
								  	.send(blueTeamGaren,achieve,levelUp);								  	
								  }
								  else{
								  	.send(redTeamRiven,achieve,levelUp);
								  }
								  .kill_agent(X);.										
+?amIAlive : damageFromEnemy(X,_) & hitPoints(Y) <- Y > X. 
+!levelUp : level(X) & team(Y) <- .print("Enemy champion is dead, leveling up ", X +1);
								  -+level(X+1).send(gameMaster,achieve,updateChampionKill(Y));.

								  