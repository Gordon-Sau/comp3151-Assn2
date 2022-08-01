package org.pull;

import java.util.ArrayDeque;
import java.util.Queue;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class BoundedBuffer extends BufferActor {

    // stores the actual data
    private final Queue<String> buffer = new ArrayDeque<>();
    // keep track of the consumers that are ready for receiving data
    private final Queue<ActorRef<ConsumerActor.Msg>> consumersQueue = new ArrayDeque<>();
    // keep track of the producers it can request
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

        // request a producer if a consumer consumes from the buffer (2nd case)
        requestProducersUntilFull();

        return this;
    }

    @Override
    protected Behavior<BufferCommand> onProduce(Produce request) {
        outDeficit--;
        if (consumersQueue.isEmpty()) {
            buffer.add(request.data);
        } else {
            consumersQueue.poll().tell(new ConsumerActor.DataMsg(request.data));
        }
        // Note: 
        // the loop will only run once if a consumer consumes data (2nd case)
        requestProducersUntilFull();
        return this;
    }
    
    private boolean isFull() {
        return buffer.size() + outDeficit >= maxSize;
    }

    @Override
    protected Behavior<BufferCommand> onRegisterProducer(RegisterProducer request) {
        producersQueue.add(request.producer);
        // Note: the loop will only run for the first producer
        // to fill up the buffer
        requestProducersUntilFull();
        return this;
    }

    @Override
    protected Behavior<BufferCommand> onFinish(Finish request) {
        // do not request the producer anymore
        outDeficit--; // receive response
        producersQueue.remove(request.producer);
        return this;
    }

    private void requestOneProducer() {
        if (producersQueue.isEmpty()) {
            return;
        }
        // Use queue to ensure that everyone has the chance to produce 
        // (eventual entry if every producer response to its request eventually)

        // Drawbacks: It is possible that we are waiting for some producers for 
        // too long, and the parallelism is lost as other producers needs to wait 
        // for these producers to produce first before receiving a RequestProduce
        // and have the chance to produce.
        ActorRef<ProducerActor.Command> producer = producersQueue.poll();
        producer.tell(ProducerActor.RequestProduce.INSTANCE);
        // put the producer back to the end of the queue
        producersQueue.add(producer);
        outDeficit++; // create request

        // One may use a different scheduling strategy for better parallelism
        // e.g. prioirity based on previous throughput,
        // partition the buffer so that every producer contains a fixed amount of
        // isolated buffer
    }

    private void requestProducersUntilFull() {
        if (producersQueue.isEmpty()) {
            return;
        }
        while (!isFull()) {
            requestOneProducer();
        }
    }
}
