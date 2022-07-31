package org.example2;

import java.util.ArrayDeque;
import java.util.Queue;

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
        // if the buffer is empty put the consumer in the waiting queue
        if (buffer.isEmpty()) {
            consumersQueue.add(request.consumer);
        } else {
            request.consumer.tell(new ConsumerActor.DataMsg(buffer.poll()));
        }
        return this;
    }

    @Override
    protected Behavior<BufferCommand> onProduce(Produce request) {
        // Since the buffer is unbounded,
        // just put the data in the queue/ send the data to a consumer
        // and request the next output from the producer
        if (consumersQueue.isEmpty()) {
            buffer.add(request.data);
        } else {
            consumersQueue.poll().tell(new ConsumerActor.DataMsg(request.data));
        }
        request.producer.tell(ProducerActor.RequestProduce.INSTANCE);
        return this;
    }
    
}