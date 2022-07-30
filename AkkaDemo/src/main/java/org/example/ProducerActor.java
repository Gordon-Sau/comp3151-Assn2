package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ProducerActor extends AbstractBehavior<ProducerActor.Command> {
    public static interface Command {}

    int count = 1;
    static Behavior<Command> create() {
        return Behaviors.setup(ProducerActor::new);
    }

    private ProducerActor(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(Command.class, this::produce)
        .build();
    }

    private Behavior<Command> produce(Command request) {
        System.out.println("Producing");
        return this;
    }
}