package org.example2;

import java.util.ArrayDeque;
import java.util.Queue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class BoundedBuffer extends BufferActor {

    private final Queue<String> buffer = new ArrayDeque<>();
    private final Queue<ActorRef<ConsumerActor.Msg>> consumersQueue = new ArrayDeque<>();
    private final Queue<ActorRef<ProducerActor.Command>> producersQueue = new ArrayDeque<>();
    private final long maxSize;
    private long outDeficit = 0; // number of ProduceRequest that have sent out but have not received a response

    public static Behavior<BufferActor.BufferCommand> create(long bufferSize) {
        return Behaviors.setup(context -> new BoundedBuffer(context, bufferSize));
    }

    private BoundedBuffer(ActorContext<BufferCommand> context, long bufferSize) {
        super(context);
        this.maxSize = bufferSize;
    }

    @Override
    protected Behavior<BufferCommand> onConsume(Consume request) {
        // same as unbounded buffer
        if (buffer.isEmpty()) {
            consumersQueue.add(request.consumer);
        } else {
            request.consumer.tell(new ConsumerActor.DataMsg(buffer.poll()));
        }

        // TODO: request a producer

        return this;
    }

    @Override
    protected Behavior<BufferCommand> onProduce(Produce request) {
        producersQueue.add(request.producer);
        if (consumersQueue.isEmpty()) {
            buffer.add(request.data);
        } else {
            consumersQueue.poll().tell(new ConsumerActor.DataMsg(request.data));
        }
        return this;
    }
    
    private boolean isFull() {
        return buffer.size() + outDeficit >= maxSize;
    }

    @Override
    protected Behavior<BufferCommand> onRegisterProducer(RegisterProducer request) {
        // request the producer
        return this;
    }

    @Override
    protected Behavior<BufferCommand> onFinish(Finish request) {
        // do not request the producer anymore
        return this;
    }
}
