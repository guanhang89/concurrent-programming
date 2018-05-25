package chapter6;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ComposeCalc {
    public static Integer calc(Integer para) {
        return para / 2;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> calc(50))
                .thenCompose((i) -> CompletableFuture.supplyAsync(() -> calc(i)))
                .thenApply((str) -> "str" + str)
                .thenAccept(System.out::println);
        future.get();
    }
}
