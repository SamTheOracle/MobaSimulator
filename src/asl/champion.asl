// Agent champion in project mobasimulator

/* Initial beliefs and rules */
hitPoints(200).
/* Initial goals */

!start.

/* Plans */


+!start : true <- .print("hello world.").
+!fight : true <- ?chooseEnemy;.
+!attack(redTeam) <- .print("my turn");selectTarget(redTeam).
					 //.send(gameMaster,achieve,swapTurn(redTeam)).
+!attack(blueTeam) <- .print("my turn");selectTarget(blueTeam).//"selectTarget".send(gameMaster,achieve,swapTurn(blueTeam)).