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

    //private RunnerData data;

    private Runner(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> createRacer() {
        return Behaviors.setup(Runner::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }

    private Receive<Command> notYetStarted() {
        return newReceiveBuilder()
                .onMessage(StartRunningCommand.class, this::onStartRunningCommand)
                //.onMessage(WhereAreYouQueryCommand.class, this::onWhereAreYouQueryCommand)
                .build();
    }

    private Receive<Command> running(RunnerData runnerData) {
        return newReceiveBuilder()
                //.onMessage(StartRunningCommand.class, this::onStartRunningCommand)
                .onMessage(WhereAreYouQueryCommand.class, command -> onWhereAreYouQueryCommand(command, runnerData))
                .build();
    }

    private Receive<Command> completed(RunnerData position) {
        return newReceiveBuilder()
                //.onMessage(StartRunningCommand.class, this::onStartRunningCommand)
                .onMessage(WhereAreYouQueryCommand.class, command -> {
                    command.sender().tell(new RaceController.RunnerUpdateCommand(getContext().getSelf(), position.currentPosition()));
                    return Behaviors.same();
                })
                .build();
    }


    private Receive<Command> onWhereAreYouQueryCommand(WhereAreYouQueryCommand command, RunnerData data) {
        RunnerData next = data.next();


        command.sender().tell(new RaceController.RunnerUpdateCommand(getContext().getSelf(), next.currentPosition()));

        if (next.isFinished()) {
            return completed(next);
        }

        return running(next);
    }

    private Behavior<Command> onStartRunningCommand(StartRunningCommand command) {
        RunnerData data = RunnerData.start(command.raceLength(), command.defaultAvgSpeed());

        return running(data);
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



