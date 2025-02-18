package io.github.jlmc.model;

import java.time.Instant;

public record VehiclePositionMessage(String vehicleId, Instant time, int latitude, int longitude) {
}
