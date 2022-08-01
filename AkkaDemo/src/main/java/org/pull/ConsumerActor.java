package org.pull;

import java.util.Random;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ConsumerActor extends AbstractBehavior<ConsumerActor.Msg> {
    public static interface Msg {}
    
    public static class DataMsg implements Msg {
        public final String data;
        public DataMsg(String data) {
            this.data = data;
        }
    }

    private final ActorRef<BufferActor.BufferCommand> buffer;

    private ConsumerActor(ActorContext<Msg> context, ActorRef<BufferActor.BufferCommand> buffer) {
        super(context);
        this.buffer = buffer;
        requestConsume();
    }

    @Override
    public Receive<Msg> createReceive() {
        return newReceiveBuilder()
          .onMessage(DataMsg.class, this::receiveMsg)
          .build();
    }

    private Behavior<Msg> receiveMsg(DataMsg msg) {
        proceess(msg.data);
        requestConsume();
        return this;
    }

    private void requestConsume() {
        buffer.tell(new BufferActor.Consume(getContext().getSelf()));
    }

    private void proceess(String data) {
        // some complex calculations
        Random rand =  new Random();
        long val = 2;
        for (long i = 0; i < Math.abs(rand.nextLong()); i++) {
            val = val*val;
        }
        getContext().getLog().info("Consumer {} consumes the data from {}.", getContext().getSelf().path(), data);
    }

    public static Behavior<ConsumerActor.Msg> create(ActorRef<BufferActor.BufferCommand> buffer) {
        return Behaviors.setup(context -> new ConsumerActor(context, buffer));
    }

}
