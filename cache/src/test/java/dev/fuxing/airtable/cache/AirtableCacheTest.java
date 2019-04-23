package dev.fuxing.airtable.cache;

import dev.fuxing.airtable.AirtableRecord;
import dev.fuxing.airtable.formula.LogicalOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.field;
import static dev.fuxing.airtable.formula.AirtableFormula.Object.value;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by: Fuxing
 * Date: 2019-04-23
 * Time: 19:07
 */
class AirtableCacheTest {

    AirtableCache cache;

    @BeforeEach
    void setUp() {
        cache = AirtableCache.create(builder -> builder
                .apiKey(System.getenv("AIRTABLE_API_KEY"))
                .app("app3h0gjxLX3Jomw8")
                .table("Test Table")
        );
    }

    @Test
    void get() {
        AirtableRecord record = cache.get("rec0W9eGVAFSy9Chb");
        assertNotNull(record);
    }

    @Test
    void query() {
        List<AirtableRecord> query = cache.query();
        assertEquals(3, query.size());

        query = cache.query(querySpec -> {
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value("Name 1"));
        });
        assertEquals(1, query.size());

        query = cache.query(querySpec -> {
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value("QUERY EMPTY"));
        });
        assertEquals(0, query.size());
    }
}