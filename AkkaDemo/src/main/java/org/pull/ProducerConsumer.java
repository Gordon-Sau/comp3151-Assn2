package org.pull;

import java.util.HashMap;
import java.util.Map;

import org.pull.BufferActor.BufferCommand;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

/*
 * Poll-based algorithm: Buffer request from the producer. We need to register 
 * producers to the buffer so the buffer can request the producer.
 */
public class ProducerConsumer extends AbstractBehavior<ProducerConsumer.Command> {
    /* communication protocols */
    public interface Command {}
    public static enum RegisterProducer implements Command {
        INSTANCE
    }

    public static enum RegisterConsumer implements Command {
        INSTANCE
    }

    /* local state */
    private ActorRef<BufferCommand> buffer;
    private Map<Long, ActorRef<ProducerActor.Command>> producers = new HashMap<>();
    private Map<Long, ActorRef<ConsumerActor.Msg>> consumers = new HashMap<>();

    /* constructor */
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

    /* pattern matching on request and call the corresponding handler function */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(RegisterProducer.class, this::registerProducer)
        .onMessage(RegisterConsumer.class, this::registerConsumer)
        .build();
    }

    /* create producer and register it to the buffer */
    private Behavior<Command> registerProducer(RegisterProducer request) {
        long i = producers.size()-1;
        ActorRef<ProducerActor.Command> newProducer = getContext().spawn(ProducerActor.create(buffer), "producer-" + i);
        producers.put(i, newProducer);
        buffer.tell(new BufferActor.RegisterProducer(newProducer));
        return this;
    }

    /* create a consumer, which consumes from the buffer */
    private Behavior<Command> registerConsumer(RegisterConsumer request) {
        long i = consumers.size()-1;
        ActorRef<ConsumerActor.Msg> newConsumer = getContext().spawn(ConsumerActor.create(buffer), "consumer-" + i);
        consumers.put(i, newConsumer);
        return this;
    }
}
