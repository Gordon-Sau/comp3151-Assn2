package org.example2;

import org.example2.BufferActor.BufferCommand;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class ProducerActor extends AbstractBehavior<ProducerActor.Command> {
    public static interface Command {}

    public static enum RequestProduce implements Command {
        INSTANCE
    }

    private final ActorRef<BufferCommand> buffer;
    private long requestId = 0;
    private final long nData;

    public static Behavior<Command> create(ActorRef<BufferCommand> buffer) {
        return Behaviors.setup(context -> new ProducerActor(context, buffer));
    }

    private ProducerActor(ActorContext<Command> context, ActorRef<BufferCommand> buffer) {
        super(context);
        this.buffer = buffer;
        this.nData = 0; // 0 means infinite stream
    }

    private ProducerActor(ActorContext<Command> context, ActorRef<BufferCommand> buffer, long nData) {
        super(context);
        this.buffer = buffer;
        this.nData = Math.abs(nData);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
        .onMessage(RequestProduce.class, context -> this.requestProduce())
        .build();
    }

    private Behavior<Command> requestProduce() {
        if (nData == 0 || requestId < nData) {
            // generate data
            String data = generateData();
            // insert to the buffer
            buffer.tell(new BufferActor.Produce(getContext().getSelf(), requestId, data));
            // update request id
            requestId++;
        } else {
            // signal the buffer that the producer has finished producing
            buffer.tell(BufferActor.Finish.INSTANCE);
        }
        return this;
    }

    private String generateData() {
        String data = getContext().getSelf().path().name() + ' ' + requestId;
        getContext().getLog().info("Producer {} produced {}", getContext().getSelf().path(), data);
        return data;
    }
}