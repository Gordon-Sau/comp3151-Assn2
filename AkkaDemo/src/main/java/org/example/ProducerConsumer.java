package org.example;

import java.util.Map;

import org.example.BufferActor.BufferCommand;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ProducerConsumer extends AbstractBehavior<ProducerConsumer.Command> {
    public interface Command {}

    private ActorRef<BufferCommand> buffer;
    private Map<Long, ActorRef<ProducerActor.Command>> producers;
    private Map<Long, ActorRef<ConsumerActor.Msg>> consumers;

    public static Behavior<Command> create(long nProducers, long nConsumers, long bufferSize) {
        return Behaviors.setup(context -> new ProducerConsumer(context, nProducers, nConsumers, bufferSize));
    }

    private ProducerConsumer(ActorContext<Command> context, long nProducers, long nConsumers, long bufferSize) {
        super(context);
        nProducers = Math.max(1, nProducers);
        nConsumers = Math.max(1, nConsumers);
        bufferSize = Math.max(0, bufferSize);
        
        // spawn producer, consumer and buffer actors

        if (bufferSize == 0) {
            // spawn unbounded buffer when buffersize is 0
            buffer = getContext().spawn(UnboundedBuffer.create(), "unbounded-buffer");
        } else {
            buffer = getContext().spawn(BoundedBuffer.create(bufferSize), "bounded-buffer");
        }

        for (long i = 0; i < nProducers; i++) {
            ActorRef<ProducerActor.Command> newProducer = getContext().spawn(ProducerActor.create(buffer), "producer-" + i);
            producers.put(i, newProducer);
        }

        for (long i = 0; i < nConsumers; i++) {
            ActorRef<ConsumerActor.Msg> newConsumer = getContext().spawn(ConsumerActor.create(buffer), "consumer-" + i);
            consumers.put(i, newConsumer);
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .build();
    }

}
