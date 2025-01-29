package io.github.jlmc;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;

public class MainCh6 {
    public static void main(String[] args) {
        Behavior<ManagerBehavior.Command> mb = ManagerBehavior.createNewInstance(20);

        ActorSystem<ManagerBehavior.Command> actorSystem = ActorSystem.create(mb, "Manager");

        //actorSystem.tell(new ManagerBehavior.InstructionCommand("do some thing..."));
        actorSystem.tell(new ManagerBehavior.InstructionCommand(Messages.START));
    }
}
