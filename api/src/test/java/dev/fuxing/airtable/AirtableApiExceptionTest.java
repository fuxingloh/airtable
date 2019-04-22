package dev.fuxing.airtable;

import dev.fuxing.airtable.exceptions.AirtableApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:14
 */
class AirtableApiExceptionTest {

    AirtableApi api;

    @BeforeEach
    void setUp() {
        this.api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"));
    }

    @Test
    @DisplayName("Unauthorized, invalid apiKey")
    void unauthorized() {
        AirtableApi api = new AirtableApi("TEST");

        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("Test Table");

        AirtableApiException exception = assertThrows(AirtableApiException.class, table::list);
        assertEquals(exception.getCode(), 401);
    }

    @Test
    @DisplayName("Application not found, no key")
    void notFound1() {
        AirtableApi api = new AirtableApi("TEST");

        AirtableApi.Table table = api.app("app").table("Opportunities");
        AirtableApiException exception = assertThrows(AirtableApiException.class, table::list);
        assertEquals(exception.getCode(), 404);
    }

    @Test
    @DisplayName("Application not found, with key")
    void notFound2() {
        AirtableApi.Table table = api.app("app").table("Opportunities");
        AirtableApiException exception = assertThrows(AirtableApiException.class, table::list);
        assertEquals(exception.getCode(), 404);
    }

    @Test
    @DisplayName("Table not found")
    void notFound3() {
        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("afdfsd");
        AirtableApiException exception = assertThrows(AirtableApiException.class, table::list);
        assertEquals(exception.getCode(), 404);
    }

    @Test
    @DisplayName("Querystring error")
    void invalidRequest1() {
        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
        AirtableApiException exception = assertThrows(AirtableApiException.class, () -> {
            table.list(querySpec -> {
                querySpec.fields("n");
            });
        });
        assertEquals(exception.getCode(), 422);
    }

    @Test
    @DisplayName("Json body, unknown field name")
    void invalidRequest2() {
        AirtableApi.Table table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
        AirtableApiException exception = assertThrows(AirtableApiException.class, () -> {
            AirtableRecord record = new AirtableRecord();
            record.putField("abdfdsf", "");
            table.post(record);
        });
        assertEquals(exception.getCode(), 422);
    }

    // Unable to test: 400, 402, 403, 413, 500, 502, 503
}
