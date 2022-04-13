package dev.fuxing.airtable;

import dev.fuxing.airtable.AirtableTable.QuerySpec;
import dev.fuxing.airtable.formula.LogicalFunction;
import dev.fuxing.airtable.formula.LogicalOperator;
import dev.fuxing.airtable.formula.NumericOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Consumer;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.*;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:13
 */
class AirtableApiQueryTest {
    AirtableApi api;
    AirtableApi.Table table;

    @BeforeEach
    void setUp() {
        this.api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"));
        this.table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
    }

    @Test
    void iterator() {
        table.iterator().forEachRemaining(record -> {
            String name = record.getFieldString("Name");
            Assertions.assertNotNull(name);

            Assertions.assertTrue(name.matches("Name [0-9]"));

            Boolean checkbox = record.getFieldBoolean("Checkbox");
            Assertions.assertNotNull(checkbox);
            Assertions.assertTrue(checkbox);
        });
    }

    @Test
    void list() {
        AirtableTable.PaginationList list = table.list();
        Assertions.assertEquals(3, list.size());

        for (int i = 0; i < list.size(); i++) {
            AirtableRecord record = list.get(i);

            Assertions.assertEquals("Name " + (i + 1), record.getFieldString("Name"));

            Boolean checkbox = record.getFieldBoolean("Checkbox");
            Assertions.assertNotNull(checkbox);
            Assertions.assertTrue(checkbox);
        }
    }

    @Test
    void paginationList() {
        AirtableTable.PaginationList list = table.list(querySpec -> querySpec.pageSize(2));
        Assertions.assertEquals(2, list.size());
        Assertions.assertNotNull(list.getOffset());

        AirtableTable.PaginationList nextList = table.list(querySpec -> {
            querySpec.offset(list.getOffset());
        });

        Assertions.assertEquals(1, nextList.size());
        Assertions.assertNull(nextList.getOffset());
    }

    @Test
    void paginationIterator() {
        Iterator<AirtableRecord> iterator = table.iterator(querySpec -> querySpec.pageSize(2));

        int size = 0;
        while (iterator.hasNext()) {
            AirtableRecord next = iterator.next();
            Boolean checkbox = next.getFieldBoolean("Checkbox");

            Assertions.assertNotNull(checkbox);
            Assertions.assertTrue(checkbox);
            size++;
        }

        Assertions.assertEquals(3, size);
    }

    @Test
    void listFormula() {
        AirtableTable.PaginationList list = table.list(querySpec -> {
            querySpec.fields("Name");
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value("Name 1"));
        });

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("Name 1", list.get(0).getFieldString("Name"));
    }

    @Test
    void querySpec() {
        // Cell Format
        assertEquals(query -> query.cellFormat("string"), "cellFormat=string");
        assertEquals(query -> query.cellFormat("json"), "cellFormat=json");

        // Time Zone
        assertEquals(query -> query.timeZone("Africa/Abidjan"), "timeZone=Africa%2FAbidjan");
        assertEquals(query -> query.timeZone("America/Monterrey"), "timeZone=America%2FMonterrey");
        assertEquals(query -> query.timeZone(ZoneId.of("Asia/Singapore")), "timeZone=Asia%2FSingapore");

        // User Locale
        assertEquals(query -> query.userLocale("af"), "userLocale=af");
        assertEquals(query -> query.userLocale("ar-sa"), "userLocale=ar-sa");
        assertEquals(query -> query.userLocale(Locale.US), "userLocale=en-us");
        assertEquals(query -> query.userLocale(Locale.UK), "userLocale=en-gb");

        // View
        assertEquals(query -> query.view("View"), "view=View");
        assertEquals(query -> query.view("View Name"), "view=View+Name");

        // Sort
        assertEquals(query -> query.sort("field-name"), "sort%5B0%5D%5Bfield%5D=field-name");
        assertEquals(query -> query.sort("field-name", "desc"),
                "sort%5B0%5D%5Bfield%5D=field-name&sort%5B0%5D%5Bdirection%5D=desc"
        );

        assertEquals(query -> {
            query.sort("f1");
            query.sort("f2");
        }, "sort%5B0%5D%5Bfield%5D=f1&sort%5B1%5D%5Bfield%5D=f2");

        assertEquals(query -> {
            query.sort("f1");
            query.sort("f2", "desc");
        }, "sort%5B0%5D%5Bfield%5D=f1&sort%5B1%5D%5Bfield%5D=f2&sort%5B1%5D%5Bdirection%5D=desc");

        // Page Size
        assertEquals(query -> query.pageSize(50), "pageSize=50");

        // Max Records
        assertEquals(query -> query.maxRecords(25), "maxRecords=25");

        // Filter By Formula, separated into another method

        // Fields
        assertEquals(query -> query.fields("a", "b"), "fields%5B%5D=a&fields%5B%5D=b");
        assertEquals(query -> query.fields(Collections.singletonList("Space Word")), "fields%5B%5D=Space+Word");

        // Offset
        assertEquals(query -> query.offset("recidexample"), "offset=recidexample");
    }

    @Test
    void querySpecFormula() {
        assertEquals(query -> {
            query.filterByFormula("NOT({F} = '')");
        }, "filterByFormula=NOT%28%7BF%7D+%3D+%27%27%29");

        assertEquals(query -> {
            query.filterByFormula(LogicalOperator.EQ, field("Field"), value(1));
        }, "filterByFormula=%7BField%7D%3D1", "filterByFormula={Field}=1");

        assertEquals(query -> {
            query.filterByFormula(NumericOperator.ADD, value(1), value(2));
        }, "filterByFormula=1%2B2", "filterByFormula=1+2");

        assertEquals(query -> {
            query.filterByFormula(LogicalFunction.AND, value(1), value(2));
        }, "filterByFormula=AND%281%2C2%29", "filterByFormula=AND(1,2)");

        assertEquals(query -> {
            query.filterByFormula(LogicalOperator.EQ, field("f1"), parentheses(LogicalFunction.AND, value(1), field("f2")));
        }, "filterByFormula=%7Bf1%7D%3D%28AND%281%2C%7Bf2%7D%29%29", "filterByFormula={f1}=(AND(1,{f2}))");
    }

    static void assertEquals(Consumer<QuerySpec> consumer, String expected) {
        QuerySpec querySpec = QuerySpec.create();
        consumer.accept(querySpec);
        URI uri = querySpec.build();

        // Raw Query assertion
        Assertions.assertEquals(expected, uri.getRawQuery());
    }

    static void assertEquals(Consumer<QuerySpec> consumer, String raw, String query) {
        QuerySpec querySpec = QuerySpec.create();
        consumer.accept(querySpec);
        URI uri = querySpec.build();

        Assertions.assertEquals(query, uri.getQuery());
        Assertions.assertEquals(raw, uri.getRawQuery());
    }
}