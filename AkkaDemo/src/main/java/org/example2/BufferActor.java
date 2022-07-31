package org.example2;

import java.util.ArrayDeque;
import java.util.Queue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.*;

public abstract class BufferActor extends AbstractBehavior<BufferActor.BufferCommand> {
    public static interface BufferCommand {}

    public static class Consume implements BufferCommand {
        public ActorRef<ConsumerActor.Msg> consumer;
        public Consume(ActorRef<ConsumerActor.Msg> consumer) {
            this.consumer = consumer;
        }
    }

    public static class Produce implements BufferCommand {
        public ActorRef<ProducerActor.Command> producer;
        public long requestId;
        public long data;
        public Produce(ActorRef<ProducerActor.Command> producer, long requestId, long data) {
            this.producer = producer;
            this.requestId = requestId;
            this.data = data;
        }
    }

    public static class RegisterProducer implements BufferCommand {
        public ActorRef<ProducerActor.Command> producer;
        public RegisterProducer(ActorRef<ProducerActor.Command> producer) {
            this.producer = producer;
        }
    }

    protected Queue<ActorRef<ProducerActor.Command>> producers = new ArrayDeque<>();

    protected BufferActor(akka.actor.typed.javadsl.ActorContext<BufferCommand> context) {
        super(context);
    }

    @Override
    public Receive<BufferCommand> createReceive() {
        return newReceiveBuilder()
        .onMessage(RegisterProducer.class, this::onRegisterProducer)
        .onMessage(Consume.class, this::onConsume)
        .onMessage(Produce.class, this::onProduce)
        .build();
    }

    protected abstract Behavior<BufferCommand> onConsume(Consume request);
    protected abstract Behavior<BufferCommand> onProduce(Produce request);
    protected Behavior<BufferCommand> onRegisterProducer(RegisterProducer request) {
        producers.add(request.producer);
        return this;
    }
}
