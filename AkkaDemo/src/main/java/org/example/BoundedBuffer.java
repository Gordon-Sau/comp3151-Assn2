package org.example;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class BoundedBuffer extends BufferActor {

    private final Queue<Long> buffer = new ArrayDeque<>();
    private final Queue<ActorRef<ConsumerActor.Msg>> consumersQueue = new ArrayDeque<>();
    private final Set<ActorRef<ProducerActor.Command>> producers;
    private final long maxSize;

    public static Behavior<BufferActor.BufferCommand> create(Set<ActorRef<ProducerActor.Command>> producers, long bufferSize) {
        return Behaviors.setup(context -> new BoundedBuffer(context, producers, bufferSize));
    }

    private BoundedBuffer(ActorContext<BufferCommand> context, Set<ActorRef<ProducerActor.Command>> producers, long bufferSize) {
        super(context);
        this.maxSize = bufferSize;
        this.producers = new HashSet<>(producers);
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
