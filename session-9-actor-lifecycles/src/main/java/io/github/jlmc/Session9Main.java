package io.github.jlmc;

import akka.actor.typed.ActorSystem;
import io.github.jlmc.race.RaceController;


public class Session9Main {
    public static void main(String[] args) {
        ActorSystem<RaceController.Command> raceController = ActorSystem.create(RaceController.create(), "RaceSimulation");
        raceController.tell(new RaceController.StartRaceCommand(10, 100, 48.2));
    }
}
