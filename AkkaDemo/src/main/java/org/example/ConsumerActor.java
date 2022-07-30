package org.example;

import org.example.ProducerConsumer.StartProducerConsumerReqeust;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ConsumerActor extends AbstractBehavior<ConsumerActor.Msg> {
    public static interface Msg {}

    private final ActorRef<BufferActor.BufferCommand> buffer;

    private ConsumerActor(ActorContext<Msg> context, ActorRef<BufferActor.BufferCommand> buffer) {
        super(context);
        this.buffer = buffer;
    }

    @Override
    public Receive<Msg> createReceive() {
        return newReceiveBuilder()
          .onMessage(Msg.class, this::receiveMsg)
          .onMessage(StartProducerConsumerReqeust.class, reqeust -> requestConsume())
          .build();
    }

    private Behavior<Msg> receiveMsg(Msg msg) {
        // TODO
        return requestConsume();
    }

    private Behavior<Msg> requestConsume() {
        buffer.tell(new BufferActor.Consume(getContext().getSelf()));
        return this;
    }

    public static Behavior<ConsumerActor.Msg> create(ActorRef<BufferActor.BufferCommand> buffer) {
        return Behaviors.setup(context -> new ConsumerActor(context, buffer));
    }

}
