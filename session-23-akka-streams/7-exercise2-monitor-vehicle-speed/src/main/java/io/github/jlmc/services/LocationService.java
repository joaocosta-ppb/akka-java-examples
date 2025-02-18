package io.github.jlmc.services;

import io.github.jlmc.model.VehiclePositionMessage;
import io.github.jlmc.model.VehicleSpeed;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class LocationService {

    public VehiclePositionMessage getVehiclePosition(String vehicleId) {
        //simulate some time to get a response from the vehicle

        ThreadLocalRandom r = ThreadLocalRandom.current();
        try {
            Thread.sleep(1000 * r.nextInt(5));
        } catch (InterruptedException ignored) {
        }

        return new VehiclePositionMessage(vehicleId, Instant.now(), r.nextInt(100), r.nextInt(100));
    }

    public VehicleSpeed calculateSpeed(VehiclePositionMessage position1, VehiclePositionMessage position2) {
        double longDistance = Math.abs(position1.longitude() - position2.longitude());
        double latDistance = Math.abs(position1.latitude() - position2.latitude());
        double distanceTravelled = Math.pow((Math.pow(longDistance, 2) + Math.pow(latDistance, 2)), 0.5);
        long time = Math.max(1, Math.abs(position1.time().toEpochMilli() - position2.time().toEpochMilli()) / 1000);
        double speed = distanceTravelled * 10 / time;
        if (position2.longitude() == 0 && position2.latitude() == 0) speed = 0;
        if (speed > 120) speed = 50;
        return new VehicleSpeed(position1.vehicleId(), speed);
    }

}
