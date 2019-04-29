// Agent champion in project mobasimulator

/* Initial beliefs and rules */
hitPoints(200).

mana(100).



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


+!start : true <- .print("hello world.").
+!fight : true <- ?chooseEnemy;.
+!attack(redTeam) <- .print("my turn");.random(R);selectTarget(redTeam,0.5).
					
+!attack(blueTeam) <- .print("my turn");.random(R);selectTarget(blueTeam,R).

+!receiveDamage : team(T) & damageFromEnemy(X,Y) & hitPoints(Z)<-?amIAlive;
										 -+hitPoints(Z - X);
										  if(T == redTeam){
										  	.send(gameMaster,achieve,swapTurn(redTeam));
										  }
										  else{
										  	.send(gameMaster,achieve,swapTurn(blueTeam));
										  }.

//+?tryHitEnemyChampion : mana(X) <- 

-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED ", X, " team ", Y);updateKill(X,Y);.print("I am killing myself ",X);
								  .send(gameMaster,achieve,updateChampionKill(Y));.kill_agent(X);.										
										//.
								
									
				   						    
+?amIAlive : damageFromEnemy(X,_) & hitPoints(Y) <- Y > X. 