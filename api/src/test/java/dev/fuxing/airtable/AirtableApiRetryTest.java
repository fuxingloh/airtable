package dev.fuxing.airtable;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.http.client.fluent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
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
        Executor executor = AirtableExecutor.newInstance(true, 3);

        this.api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"), executor);
    }


    /**
     * Send all 1000 request in async, validate all request succeeded to test 429 error.
     * <p>
     * This is using default CONNECTION_MANAGER of 8 request per second.
     * <pre>
     * CONNECTION_MANAGER.setMaxTotal(8);
     * CONNECTION_MANAGER.setDefaultMaxPerRoute(8);
     * CONNECTION_MANAGER.setValidateAfterInactivity(1000);
     * </pre>
     */
    @Test
    @Disabled
    void tooManyRequest() {
        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
        AtomicInteger counter = new AtomicInteger(0);

        Runnable runnable = () -> {
            table.list(querySpec -> {
                querySpec.maxRecords(1);
                querySpec.pageSize(1);
            });
            System.out.println(counter.incrementAndGet());
        };

        // All request will be complete
        CompletableFuture[] futures = IntStream.range(0, 1000).mapToObj(value -> {
            return CompletableFuture.runAsync(runnable);
        }).toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

}
