package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.*;

public class BufferActor extends AbstractBehavior<BufferActor.BufferCommand> {
    public static interface BufferCommand {}

    public static class Consume implements BufferCommand {
    }

    public static class Produce implements BufferCommand {
    }

    static Behavior<BufferCommand> create() {
        return Behaviors.setup(BufferActor::new);
    }


    private BufferActor(akka.actor.typed.javadsl.ActorContext<BufferCommand> context) {
        super(context);
    }

    @Override
    public Receive<BufferCommand> createReceive() {
        return newReceiveBuilder()
          .build();
    }

    private Behavior<String> addProduction() {
        System.out.println("Adding production");
        return Behaviors.same();
    }
}
