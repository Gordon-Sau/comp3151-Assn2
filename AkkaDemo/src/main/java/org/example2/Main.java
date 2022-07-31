package org.example2;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class Main {
    public static void main(String[] args) {
        ActorRef<ProducerConsumer.Command> producerConsumerActor = ActorSystem.create(ProducerConsumer.create(1,1, 10), "producer-consumer");
    }
}
