package io.github.jlmc;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.japi.function.Function;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.github.jlmc.model.VehiclePositionMessage;
import io.github.jlmc.model.VehicleSpeed;
import io.github.jlmc.services.LocationService;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class App {


    public static final String GO = "go";

    private static final List<String> IDS = List.of("1", "2", "3", "4", "5", "6", "7", "8");

    public static void main(String[] args) {

        final Map<String, VehiclePositionMessage> repository = new HashMap<>();

        for (String id : IDS) {
            repository.put(id, new VehiclePositionMessage(id, Instant.now(), 0, 0));
        }


        //source - repeat some value every 10 seconds.
        Source<String, NotUsed> source = Source.repeat(GO).throttle(1, Duration.ofSeconds(10));


        //flow 1 - transform into the ids of each van (ie 1..8) with mapConcat
        var generateIds = Flow.of(String.class)
                .mapConcat(v -> IDS);


        //flow 2 - get position for each van as a VPMs with a call to the lookup method (create a new instance of
        //utility functions each time). Note that this process isn't instant so should be run in parallel.
        Flow<String, VehiclePositionMessage, NotUsed> calculatePositionsWithGPS = Flow.of(String.class)
                .map(vehicleId -> {
                    System.out.println("Requesting Position for vehicle " + vehicleId);
                    LocationService locationService = new LocationService();
                    return locationService.getVehiclePosition(vehicleId);
                });

        Flow<String, VehiclePositionMessage, NotUsed> calculatePositionsWithGPSAsync = Flow.of(String.class)
                .mapAsync(4, new Function<String, CompletionStage<VehiclePositionMessage>>() {
                    @Override
                    public CompletionStage<VehiclePositionMessage> apply(String vehicleId) {
                        return CompletableFuture.supplyAsync(() -> {
                            System.out.println("Requesting Position for vehicle " + vehicleId);
                            LocationService locationService = new LocationService();
                            return locationService.getVehiclePosition(vehicleId);
                        });
                    }
                });


        //flow 3 - use previous position from the map to calculate the current speed of each vehicle. Replace the
        // position in the map with the newest position and pass the current speed downstream
        Flow<VehiclePositionMessage, VehicleSpeed, NotUsed> calculateSpeed = Flow.of(VehiclePositionMessage.class)
                .map(vehiclePositionMessage -> {
                    VehiclePositionMessage previousPositionMessage = repository.get(vehiclePositionMessage.vehicleId());

                    LocationService locationService = new LocationService();
                    VehicleSpeed speed = locationService.calculateSpeed(vehiclePositionMessage, previousPositionMessage);

                    System.out.println("Vehicle " + vehiclePositionMessage.vehicleId() + " is travelling at " + speed.speed());

                    repository.put(vehiclePositionMessage.vehicleId(), vehiclePositionMessage);

                    return speed;
                });


        //flow 4 - filter to only keep those values with a speed > 95
        Flow<VehicleSpeed, VehicleSpeed, NotUsed> speedFilter = Flow.of(VehicleSpeed.class)
                .filter(it -> it.speed() > 95);

        //sink - as soon as 1 value is received return it as a materialized value, and terminate the stream
        //Sink<VehicleSpeed, CompletionStage<Done>> skin = Sink.foreach(System.out::println);
        Sink<VehicleSpeed, CompletionStage<VehicleSpeed>> skin = Sink.head();


        ActorSystem<Object> system = ActorSystem.create(Behaviors.empty(), "system");

        var result = source
                .async()
                .via(generateIds)
                .async()
                .via(calculatePositionsWithGPSAsync)
                .via(calculateSpeed)
                .via(speedFilter)
                .toMat(skin, Keep.right())
                .run(system);

        result.whenComplete((value, throwable) -> {
            if (throwable != null) {
                System.out.println("Something went wrong " + throwable);
                //noinspection CallToPrintStackTrace
                throwable.printStackTrace();
            } else {
                System.out.println("Vehicle " + value.id() + " was going at a speed of " + value.speed());
            }
            system.terminate();
        });

    }
}
