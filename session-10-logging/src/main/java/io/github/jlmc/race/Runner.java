package io.github.jlmc.race;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
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
                .onMessage(WhereAreYouQueryCommand.class, command -> onWhereAreYouQueryCommand(command, runnerData))
                .build();
    }

    private Receive<Command> completed(RunnerData data) {
        return newReceiveBuilder()
                .onMessage(WhereAreYouQueryCommand.class, command -> {
                    command.sender().tell(new RaceController.RunnerUpdateCommand(getContext().getSelf(), data.currentPosition(), data.runnerId()));
                    command.sender().tell(new RaceController.RunnerFinishedCommand(getContext().getSelf(), data.runnerId()));
                    //return Behaviors.ignore(); // This actor will ignore any future message
                    return waitingToStop(data);
                })
                .build();
    }

    private Receive<Command> waitingToStop(RunnerData data) {
        return newReceiveBuilder()
                .onAnyMessage(message -> Behaviors.same())
                .onSignal(PostStop.class, signal -> {
                    if (getContext().getLog().isInfoEnabled()) {
                        getContext().getLog().info("{} - I'm about to terminate!", data.runnerId());
                    }
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> onWhereAreYouQueryCommand(WhereAreYouQueryCommand command, RunnerData data) {
        RunnerData next = data.next();


        command.sender().tell(new RaceController.RunnerUpdateCommand(getContext().getSelf(), next.currentPosition(), data.runnerId()));

        if (next.isFinished()) {
            return completed(next);
        }

        return running(next);
    }

    private Behavior<Command> onStartRunningCommand(StartRunningCommand command) {
        RunnerData data = RunnerData.start(command.raceLength(), command.defaultAvgSpeed(), command.runnerId());

        return running(data);
    }

    public interface Command extends Serializable {
    }

    record StartRunningCommand(ActorRef<RaceController.Command> sender,
                               int raceLength,
                               double defaultAvgSpeed,
                               String runnerId) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    record WhereAreYouQueryCommand(ActorRef<RaceController.Command> sender) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}



