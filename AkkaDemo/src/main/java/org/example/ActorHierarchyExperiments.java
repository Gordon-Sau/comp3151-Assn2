package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class ActorHierarchyExperiments {
    public static void main(String[] args) {
        ActorRef<String> testSystem = ActorSystem.create(Main.create(), "testSystem");
        testSystem.tell("start");
    }
}