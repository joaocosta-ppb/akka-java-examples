package io.github.jlmc;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableStatesTest {

    @Test
    void ex1() {

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        CompletionStage<Bet> betCompletableFuture = CompletableFuture.supplyAsync(() -> generateBet(), executorService);
        CompletionStage<TOperation> tOperationCompletionStage = betCompletableFuture.thenApplyAsync(bet -> processBet(bet), executorService);

        TOperation join = tOperationCompletionStage.toCompletableFuture().join();
        System.out.println(join);
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static AtomicInteger count = new AtomicInteger(0);

    private static Bet generateBet() {
        int value = ThreadLocalRandom.current().nextInt(1, 100);
        sleep(2);
        return new Bet("Foo-" + count.incrementAndGet(), value);
    }

    private static TOperation processBet(Bet bet) {
        sleep(3);
        String hash = "%s#%s".formatted(bet.name(), bet.value()).toLowerCase();
        return new TOperation(bet, hash);
    }
}

record Bet(String name, int value) {
}

record TOperation(Bet bet, String hash) {}


