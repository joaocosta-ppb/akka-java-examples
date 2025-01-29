package io.github.jlmc.race;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serial;
import java.io.Serializable;

public class Runner extends AbstractBehavior<Runner.Command> {

    private RunnerData data;

    private Runner(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> createRacer() {
        return Behaviors.setup(Runner::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartRunningCommand.class, this::onStartRunningCommand)
                .onMessage(WhereAreYouQueryCommand.class, this::onWhereAreYouQueryCommand)
                .build();
    }

    private Behavior<Command> onWhereAreYouQueryCommand(WhereAreYouQueryCommand command) {

        if (data != null) {
            this.data = this.data.next();
        }

        command.sender().tell(new RaceController.RunnerUpdateCommand(getContext().getSelf(), this.data.currentPosition()));

        return this;
    }

    private Behavior<Command> onStartRunningCommand(StartRunningCommand command) {
        this.data = RunnerData.start(command.raceLength(), command.defaultAvgSpeed());


        return this;
    }

    public interface Command extends Serializable {
    }

    record StartRunningCommand(ActorRef<RaceController.Command> sender,
                               int raceLength,
                               double defaultAvgSpeed) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    record WhereAreYouQueryCommand(ActorRef<RaceController.Command> sender) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}



