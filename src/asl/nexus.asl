// Agent nexus in project mobasimulator

/* Initial beliefs and rules */

hitPoints(500).

/* Initial goals */
+damage(X,Y) <- true; !receiveDamage.

/* Plans */

+!start : true <- .print("hello world.").
+!receiveDamage : damage(X,Y) <- .print("received damage from ",Y).
