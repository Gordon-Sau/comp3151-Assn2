package org.example2;

import java.util.HashMap;
import java.util.Map;

import org.example2.BufferActor.BufferCommand;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

/*
 * Poll-based algorithm: Buffer request from the producer. We need to register 
 * producers to the buffer so the buffer can request the producer.
 */
public class ProducerConsumer extends AbstractBehavior<ProducerConsumer.Command> {
    public interface Command {}
    public static enum RegisterProducer implements Command {
        INSTANCE
    }

    public static enum RegisterConsumer implements Command {
        INSTANCE
    }


    private ActorRef<BufferCommand> buffer;
    private Map<Long, ActorRef<ProducerActor.Command>> producers = new HashMap<>();
    private Map<Long, ActorRef<ConsumerActor.Msg>> consumers = new HashMap<>();

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
            buffer.tell(new BufferActor.RegisterProducer(newProducer));
        }

        for (long i = 0; i < nConsumers; i++) {
            ActorRef<ConsumerActor.Msg> newConsumer = getContext().spawn(ConsumerActor.create(buffer), "consumer-" + i);
            consumers.put(i, newConsumer);
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(RegisterProducer.class, this::registerProducer)
        .onMessage(RegisterConsumer.class, this::registerConsumer)
        .build();
    }

    private Behavior<Command> registerProducer(RegisterProducer request) {
        long i = producers.size()-1;
        ActorRef<ProducerActor.Command> newProducer = getContext().spawn(ProducerActor.create(buffer), "producer-" + i);
        producers.put(i, newProducer);
        buffer.tell(new BufferActor.RegisterProducer(newProducer));
        return this;
    }

    private Behavior<Command> registerConsumer(RegisterConsumer request) {
        long i = consumers.size()-1;
        ActorRef<ConsumerActor.Msg> newConsumer = getContext().spawn(ConsumerActor.create(buffer), "consumer-" + i);
        consumers.put(i, newConsumer);
        return this;
    }
}
