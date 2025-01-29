package io.github.jlmc.race;

import java.util.concurrent.ThreadLocalRandom;

record RunnerData(int raceLength, double defaultAvgSpeed, int avgSpeedAdjustmentFactor, int currentPosition, double currentSpeed) {

    public static RunnerData start(int raceLength, double defaultAvgSpeed) {
        return new RunnerData(raceLength, defaultAvgSpeed, generateRandomAvgSpeedAdjustmentFactor(), 0, 0);
    }

    public RunnerData next() {
        double currentSpeed = determineNextSpeed();

        var nextPosition = currentPosition + getDistanceMovedPerSecond();
        if (nextPosition > raceLength) {
            nextPosition = raceLength;
        }

        return new RunnerData(raceLength, defaultAvgSpeed, avgSpeedAdjustmentFactor, (int) nextPosition, currentSpeed);
    }

    private double getMaxSpeed() {
        return defaultAvgSpeed * (1 + ((double) avgSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return determineNextSpeed() * 1000 / 3600;
    }

    private double determineNextSpeed() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double nextSpeed = currentSpeed;
        if (currentPosition < (raceLength / 4.0)) {
            nextSpeed = nextSpeed + (((getMaxSpeed() - nextSpeed) / 10) * random.nextDouble());
        } else {
            nextSpeed = nextSpeed * (0.5 + random.nextDouble());
        }

        if (nextSpeed > getMaxSpeed()) {
            nextSpeed = getMaxSpeed();
        }

        if (nextSpeed < 5)
            nextSpeed = 5;

        if (currentPosition > (raceLength / 2.0) && nextSpeed < getMaxSpeed() / 2) {
            nextSpeed = getMaxSpeed() / 2;
        }

        return nextSpeed;
    }

    private static int generateRandomAvgSpeedAdjustmentFactor() {
        return ThreadLocalRandom.current().nextInt(30) - 10;
    }

}