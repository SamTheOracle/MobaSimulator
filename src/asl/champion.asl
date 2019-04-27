// Agent champion in project mobasimulator

/* Initial beliefs and rules */
hitPoints(200).

mana(100).



/* Initial goals */
!start.
+damageFromEnemy(X,Y) : true <- .print("Ouch! I was hit by ", Y, " for ",X," damage");!receiveDamage.
//+damageFromChampion(X,Y) : true
+commenceAttack : team(X) <- if(X == redTeam){
						!attack(redTeam)
						}
						else{
							!attack(blueTeam)
							}.
/* Plans */


+!start : true <- .print("hello world.").
+!fight : true <- ?chooseEnemy;.
+!attack(redTeam) <- .print("my turn");.random(R);selectTarget(redTeam,0.8).
					 //.send(gameMaster,achieve,swapTurn(redTeam)).
+!attack(blueTeam) <- .print("my turn");.random(R);selectTarget(blueTeam,0.8).//"selectTarget".send(gameMaster,achieve,swapTurn(blueTeam)).

+!receiveDamage : team(T) & damageFromEnemy(X,Y) & hitPoints(Z)<-?amIAlive;
										 -+hitPoints(Z - X);
										  if(T == redTeam){
										  	.send(gameMaster,achieve,swapTurn(redTeam));
										  }
										  else{
										  	.send(gameMaster,achieve,swapTurn(blueTeam));
										  }.

//+?tryHitEnemyChampion : mana(X) <- 

-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED");.//updateKill(X,Y);.print("I am killing myself ",X);
								// .send(gameMaster,achieve,respawnChampion(Y));/*.kill_agent(X);*/.										
										//.
								
									
				   						    
+?amIAlive : damageFromEnemy(X,_) & hitPoints(Y) <- Y > X. 