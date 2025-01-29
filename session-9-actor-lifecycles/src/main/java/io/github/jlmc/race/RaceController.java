package io.github.jlmc.race;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    private Map<ActorRef<Runner.Command>, Integer> currentPositions;
    private static final String TIMER_KEY = "timer-key";
    private long start;

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartRaceCommand.class, this::onStartRaceCommand)
                .onMessage(RunnerUpdateCommand.class, this::onRunnerUpdateCommand)
                .onMessage(GetPositionsCommand.class, this::onGetPositionsCommand)
                .build();
    }

    private Behavior<Command> onGetPositionsCommand(GetPositionsCommand command) {
        if (currentPositions == null) {
            return Behaviors.same();
        }

        currentPositions.keySet()
                .forEach(runner -> {
                    runner.tell(new Runner.WhereAreYouQueryCommand(getContext().getSelf()));
                    displayRace();
                });

        return Behaviors.same();
    }

    private void displayRace() {
        int displayLength = 100;
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
        System.out.println("    " + new String (new char[displayLength]).replace('\0', '='));
        int i = 0;
        for (ActorRef<Runner.Command> racer : currentPositions.keySet()) {
            System.out.println(i + " : "  + new String (new char[currentPositions.get(racer) * displayLength / 100]).replace('\0', '*'));
            i++;
        }
    }

    private Behavior<Command> onRunnerUpdateCommand(RunnerUpdateCommand command) {
        currentPositions.put(command.runner(), command.position());
        return this;
    }

    private Behavior<Command> onStartRaceCommand(StartRaceCommand command) {
        ActorContext<Command> context = getContext();

        currentPositions = new HashMap<>();
        start = System.currentTimeMillis();

        //List<ActorRef<Runner.Command>> actorRefs = new ArrayList<>();
        for (int i = 0; i < command.numberOfRunners; i++) {
            Behavior<Runner.Command> runner = Runner.createRacer();

            ActorRef<Runner.Command> runnerActor = context.spawn(runner, Runner.class.getSimpleName() + "_" + i);

            currentPositions.put(runnerActor, 0);

            //actorRefs.add(runnerActor);
        }

        currentPositions.keySet()
                .forEach(actorRef -> {
                    actorRef.tell(new Runner.StartRunningCommand(
                            context.getSelf(),
                            command.raceLength(),
                            command.defaultAvgSpeed()
                            ));
                });

        return Behaviors.withTimers(timer -> {
            timer.startTimerAtFixedRate(TIMER_KEY, new GetPositionsCommand(), Duration.ofSeconds(1));
            return this;
        });

        //return this;
    }


    public interface Command extends Serializable {
    }

    public record StartRaceCommand(int numberOfRunners, int raceLength, double defaultAvgSpeed) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    record RunnerUpdateCommand(ActorRef<Runner.Command> runner, int position) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    static class GetPositionsCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
