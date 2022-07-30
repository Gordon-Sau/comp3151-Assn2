package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ProducerConsumer extends AbstractBehavior<ProducerConsumer.Command> {
    public interface Command {}

    public static enum StartProducerConsumerReqeust implements Command {
        INSTANCE
    }


    public static Behavior<Command> create(long nProducers, long nConsumers, long bufferSize) {
        return Behaviors.setup(context -> new ProducerConsumer(context, nProducers, nConsumers, bufferSize));
    }

    private ProducerConsumer(ActorContext<Command> context, long nProducers, long nConsumers, long bufferSize) {
        super(context);
        nProducers = Math.max(1, nProducers);
        nConsumers = Math.max(1, nConsumers);
        bufferSize = Math.max(0, bufferSize);

        // TODO: spawn producer, consumer and buffer actors
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(StartProducerConsumerReqeust.class, this::start)
        .build();
    }

    private ProducerConsumer start(StartProducerConsumerReqeust reqeust) {
        // TODO: tell the actors to start producing and consuming

        return this;
    }
}
