package org.example;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class ConsumerActor extends AbstractBehavior<ConsumerActor.Msg> {
    public static class Msg {}

    private ConsumerActor(ActorContext<Msg> context) {
        super(context);
        //TODO Auto-generated constructor stub
    }

    @Override
    public Receive<Msg> createReceive() {
        return newReceiveBuilder()
          .onMessage(Msg.class, this::receiveMsg)
          .build();
    }

    private Behavior<Msg> receiveMsg(Msg msg) {

        return this;
    }

}
