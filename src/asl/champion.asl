// Agent champion in project mobasimulator

/* Initial beliefs and rules */
hitPoints(200).

mana(100).



/* Initial goals */
!start.
+damage(X,Y) : true <- !receiveDamage.
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
+!attack(redTeam) <- .print("my turn");selectTarget(redTeam).
					 //.send(gameMaster,achieve,swapTurn(redTeam)).
+!attack(blueTeam) <- .print("my turn");selectTarget(blueTeam).//"selectTarget".send(gameMaster,achieve,swapTurn(blueTeam)).

+!receiveDamage : team(X) & damage(X,Y)<- .print("Ouch! I was hit by ", Y, " for ",X," damage").
//+?tryHitEnemyChampion : mana(X) <- 

-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED");updateKill(X,Y);.print("I am killing myself ",X);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.										
										//.
								
									
				   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 