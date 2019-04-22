package dev.fuxing.airtable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:13
 */
class AirtableApiRetryTest {
    AirtableApi api;

    @BeforeEach
    void setUp() {
        this.api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"));
    }


    @Test
    void tooManyRequest() {
        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("Test Table");

        Runnable runnable = () -> {
            table.list(querySpec -> {
                querySpec.maxRecords(1);
                querySpec.pageSize(1);
            });
        };

        // All request will be complete
        CompletableFuture[] futures = IntStream.range(0, 100).mapToObj(value -> {
            return CompletableFuture.runAsync(runnable);
        }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

}