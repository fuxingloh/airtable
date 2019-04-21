package dev.fuxing.airtable;

import dev.fuxing.airtable.exceptions.AirtableApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:14
 */
class AirtableApiExceptionTest {

    @Test
    void unauthorized() {
        AirtableApi api = new AirtableApi("TEST");

        AirtableApi.Table table = api.base("appKZCZBCeAouRBgW").table("Opportunities");

        AirtableApiException exception = assertThrows(AirtableApiException.class, table::list);
        assertEquals(exception.getCode(), 401);
    }

    // TODO(fuxing): exception testing
}
