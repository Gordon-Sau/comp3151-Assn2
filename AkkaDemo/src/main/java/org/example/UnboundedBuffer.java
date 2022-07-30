package org.example;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class UnboundedBuffer extends BufferActor {

    private final Queue<Long> buffer = new ArrayDeque<>();
    private final Queue<ActorRef<ConsumerActor.Msg>> consumersQueue = new ArrayDeque<>();

    public static Behavior<BufferActor.BufferCommand> create() {
        return Behaviors.setup(context -> new UnboundedBuffer(context));
    }

    private UnboundedBuffer(ActorContext<BufferCommand> context) {
        super(context);
    }

    @Override
    protected Behavior<BufferCommand> onConsume(Consume request) {
        return null;
    }

    @Override
    protected Behavior<BufferCommand> onProduce(Produce request) {
        return null;
    }
    
}
