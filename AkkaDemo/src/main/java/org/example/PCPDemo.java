package org.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;

public class PCPDemo {
    public static void main(String[] args) {
        ActorRef<String> buffer = ActorSystem.create(BufferActor.create(), "buffer");
        buffer.tell("start");
    }
}
