package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.ConfigException;

public class BufferActor extends AbstractBehavior {

    static Behavior<String> create() {
        return Behaviors.setup(BufferActor::new);
    }

    private BufferActor(akka.actor.typed.javadsl.ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
          .onMessageEquals("start", this::start)
          .onMessageEquals("addProduction", this::addProduction)
          .build();
    }

    private Behavior<String> start() {
        ActorRef<String> producer = getContext().spawn(ProducerActor.create(), "producer");

        System.out.println("Producer: " + producer);
        producer.tell("produce");
        return Behaviors.same();
    }

    private Behavior<String> addProduction() {
        System.out.println("Adding production");
        return Behaviors.same();
    }
}
