package org.pull;

import org.pull.BufferActor.BufferCommand;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
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
    private long nextProducerId = 0;
    private long nextConsumerId = 0;

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

        for (; nextProducerId < nProducers; nextProducerId++) {
            registerProducer();
        }

        for (; nextConsumerId < nConsumers; nextConsumerId++) {
            registerConsumer();
        }
    }

    private ProducerConsumer(ActorContext<Command> context, long bufferSize) {
        this(context, 0, 0, bufferSize);
    }

    /* pattern matching on request and call the corresponding handler function */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(RegisterProducer.class, __ -> this.registerProducer())
        .onMessage(RegisterConsumer.class, __ -> this.registerConsumer())
        .onSignal(Terminated.class, this::onTerminated)
        .build();
    }

    /* create producer and register it to the buffer */
    private Behavior<Command> registerProducer() {
        ActorRef<ProducerActor.Command> newProducer = getContext().spawn(ProducerActor.create(buffer), "producer-" + nextProducerId);
        buffer.tell(new BufferActor.RegisterProducer(newProducer));
        getContext().watch(newProducer);
        nextProducerId++;
        return this;
    }

    /* create a consumer, which consumes from the buffer */
    private Behavior<Command> registerConsumer() {
        ActorRef<ConsumerActor.Msg> newConsumer = getContext().spawn(ConsumerActor.create(buffer), "consumer-" + nextConsumerId);
        getContext().watch(newConsumer);
        nextConsumerId++;
        return this;
    }

    /* onTerminated */
    private Behavior<Command> onTerminated(Terminated request) {
        ActorRef<Void> actor = request.getRef();
        getContext().getLog().info("{} terminated!", actor.path().name());
        return this;
    }
}
