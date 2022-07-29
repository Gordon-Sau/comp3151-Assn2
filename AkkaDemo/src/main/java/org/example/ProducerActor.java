package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ProducerActor extends AbstractBehavior<String> {

    int count = 1;
    static Behavior<String> create() {
        return Behaviors.setup(ProducerActor::new);
    }

    private ProducerActor(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("produce", this::produce).build();
    }

    private Behavior<String> produce() {
        System.out.println("Producing");
        return this;
    }
}