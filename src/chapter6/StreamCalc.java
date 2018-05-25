package chapter6;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StreamCalc {
    public static Integer calc(Integer value) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value * value;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> fu = CompletableFuture.supplyAsync(() -> calc(50))
                .thenAccept((i) -> Integer.toString(i))
                .thenApply((str) -> "\"" + str + "\"")
                .thenAccept(System.out::println);
        fu.get();
    }
}
