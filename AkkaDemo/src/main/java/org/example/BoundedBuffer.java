package org.example;

import java.util.ArrayDeque;
import java.util.Queue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class BoundedBuffer extends BufferActor {

    private final Queue<Long> buffer = new ArrayDeque<>();
    private final Queue<ActorRef<ConsumerActor.Msg>> consumersQueue = new ArrayDeque<>();
    private final long maxSize;

    public static Behavior<BufferActor.BufferCommand> create(long bufferSize) {
        return Behaviors.setup(context -> new BoundedBuffer(context, bufferSize));
    }

    private BoundedBuffer(ActorContext<BufferCommand> context, long bufferSize) {
        super(context);
        this.maxSize = bufferSize;
    }

    @Override
    protected Behavior<BufferCommand> onConsume(Consume request) {
        // if (buffer.isEmpty()) {
        //     // cannot send to any consumer at the moment
        //     consumersQueue.add(request.consumer);
        // } else {
        //     // get data from the buffer and send to the consumer
        //     ActorRef<ConsumerActor.Msg> consumer = consumersQueue.poll();
        //     consumer.tell(new ConsumerActor.DataMsg(buffer.poll()));
        // }

        return this;
    }

    @Override
    protected Behavior<BufferCommand> onProduce(Produce request) {
        // if (consumersQueue.isEmpty()) {
        //     // put in the buffer first
        //     buffer.add(request.data);
        // } else {
        //     // just send to a consumer
        //     ActorRef<ConsumerActor.Msg> consumer = consumersQueue.poll();
        //     consumer.tell(new ConsumerActor.DataMsg(request.data));
        // }
        return this;
    }
    
    private boolean isFull() {
        return buffer.size() >= maxSize;
    }
}
