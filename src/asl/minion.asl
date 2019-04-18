// Agent minion in project mobasimulator

/* Initial beliefs and rules */

hitPoints(100).


/*when I receive "spawn" percept, I execute plan !***    */
+spawn <- .print("I'm going forward").
+damage(X,Y) : true <- .print("receive damage ",X," from ",Y);!receiveDamage.

+attackAgain : true <- .print("attack!");!fight.
+attack :true<- .print("attack!");!fight.

+!fight : role(X)<- .print("I'll focus the enemy ", X);selectNextEnemy(X);.

-!fight <- .print("focus champion or turret?").

+!receiveDamage : damage(X,Z) & hitPoints(Y) & team(T)<- 
										.print("Ouch, that hurt!");	
										?amIAlive;		
										-+hitPoints(Y - X);
										//.abolish(damage(X,Z));
										.send(gameMaster,achieve,swapTurn(T)).
										
										
										



//-?amIAlive : self(X) & team(Y)  <- .print("I WAS KILLED BY ",N); updateKill(X,Y);.print("I am killing myself ",X);.kill_agent(X);.
										
-?amIAlive :self(X) & team(Y) <- .print("I WAS KILLED");updateKill(X,Y);.print("I am killing myself ",X);
								 .send(gameMaster,achieve,swapTurn(Y));.kill_agent(X);.										
										//.
								
									
				   						    
+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 



























































//+damage(_,_) : true <- !receiveDamage.
//+attack : true <- !fight.
////+keepFighting : true<- .print("keep fighting");.
///* Initial goals */
//
//+!start.
//
//
//
///* Plans */
//+!start <- .print("hello world").
//+!proceedForward <- .print("I'm marching forward, lets kill them!").
//
//+!fight : role(X)<- .print("Choosing the enemy");
//					//!wait_randomly;
//                     selectClosestEnemy(X)
//                     .abolish(attack).
//                               
//-!fight : team(X)<- .print("Team ", X, " has won the day").
//
//+!receiveDamage : damage(X,Z) & hitPoints(Y) <- 	
//										.print("I was hit by ", Z);
//                                        .print("total damage: ",X);
//										?amIAlive;
//										//false;
//										.print("Ouch, that hurt!");
//										-+hitPoints(Y - X);
//										.abolish(damage(X,Z)).
//										
//										
//										
//										
//
//-?amIAlive : self(X) & team(Y) & damage(M,N) <- .print("I WAS KILLED BY ",N); updateKill(X,Y);.print("I am killing myself ",X);.kill_agent(X);.
//										
//										
//										//.
//								
//									
//				   						    
//+?amIAlive : damage(X,_) & hitPoints(Y) <- Y > X. 
//
//+!wait_randomly <-
//	.random(R);
//	.wait(R * 5000).								
//						   
//				 								
//
