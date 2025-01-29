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
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    private Map<ResultKey, Integer> currentPositions;
    private Map<ResultKey, Long> finishedTimes;

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
                .onMessage(RunnerFinishedCommand.class, this::onRunnerFinishedCommand)
                .build();
    }

    private Receive<Command> raceCompletedMessageHandler() {
        return newReceiveBuilder()
                .onMessage(GetPositionsCommand.class, command -> {

                    currentPositions.keySet()
                            .forEach(runner -> getContext().stop(runner.runner()));

                    displayRaceResult();

                    return Behaviors.withTimers(timers -> {
                        timers.cancelAll();
                        return Behaviors.stopped();
                    });
                    //return Behaviors.same();
                })
                .build();
    }

    private Behavior<Command> onRunnerFinishedCommand(RunnerFinishedCommand command) {
        finishedTimes.put(new ResultKey(command.runner(), command.runnerId()), System.currentTimeMillis());

        if (finishedTimes.size() == currentPositions.size()) {
            return raceCompletedMessageHandler();
        }

        return Behaviors.same();
    }

    private Behavior<Command> onGetPositionsCommand(GetPositionsCommand command) {
        if (currentPositions == null) {
            return Behaviors.same();
        }

        currentPositions.keySet()
                .forEach(runner -> {
                    runner.runner().tell(new Runner.WhereAreYouQueryCommand(getContext().getSelf()));
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
        for (var racer : currentPositions.keySet()) {
            System.out.println((racer.runnerId()) + "\t : "  + new String (new char[currentPositions.get(racer) * displayLength / 100]).replace('\0', '*'));
            i++;
        }
    }

    private void displayRaceResult() {
        System.out.println("Results");
        finishedTimes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {

                    String runner = entry.getKey().runnerId();

                    System.out.printf("Runner %s finished in %s seconds%n", runner, parseTime(entry.getValue()));

                });
    }

    private String parseTime(Long timestamp) {
        Duration duration = Duration.between(Instant.ofEpochMilli(start), Instant.ofEpochMilli(timestamp));
        return "%d.%03d".formatted(duration.getSeconds(), duration.toMillisPart());
    }

    private Behavior<Command> onRunnerUpdateCommand(RunnerUpdateCommand command) {
        currentPositions.put(new ResultKey(command.runner(), command.runnerId()), command.position());
        return this;
    }

    private Behavior<Command> onStartRaceCommand(StartRaceCommand command) {
        ActorContext<Command> context = getContext();

        currentPositions = new LinkedHashMap<>();
        finishedTimes = new LinkedHashMap<>();

        start = System.currentTimeMillis();

        for (int i = 0; i < command.numberOfRunners; i++) {
            Behavior<Runner.Command> runner = Runner.createRacer();

            ActorRef<Runner.Command> runnerActor = context.spawn(runner, Runner.class.getSimpleName() + "_" + i);

            String runnerNumber = "" + (i + 1);
            currentPositions.put(new ResultKey(runnerActor, runnerNumber ), 0);
        }

        for (Map.Entry<ResultKey, Integer> resultKeyIntegerEntry : currentPositions.entrySet()) {
            resultKeyIntegerEntry.getKey().runner()
                    .tell(new Runner.StartRunningCommand(
                            context.getSelf(),
                            command.raceLength(),
                            command.defaultAvgSpeed(),
                            resultKeyIntegerEntry.getKey().runnerId()
                    ));
        }

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

    record RunnerUpdateCommand(ActorRef<Runner.Command> runner, int position, String runnerId) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    static class GetPositionsCommand implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record RunnerFinishedCommand(ActorRef<Runner.Command> runner, String runnerId) implements Command {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    record ResultKey(ActorRef<Runner.Command> runner, String runnerId) {}


}
