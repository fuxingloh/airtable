package dev.fuxing.airtable.mirror;

import dev.fuxing.airtable.AirtableApi;
import dev.fuxing.airtable.AirtableRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.field;

/**
 * Created by: Fuxing
 * Date: 2019-04-23
 * Time: 19:06
 */
class AirtableMirrorTest {

    AirtableApi api;
    AirtableApi.Table table;

    @BeforeEach
    void setUp() {
        this.api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"));
        this.table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
    }

    @Test
    void mirror() {
        Database database = new Database();

        AirtableMirror mirror = new AirtableMirror(table, field("Name")) {
            @Override
            protected Iterator<AirtableRecord> iterator() {
                Iterator<Database.Data> iterator = database.iterator();

                return new Iterator<AirtableRecord>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public AirtableRecord next() {
                        Database.Data data = iterator.next();

                        AirtableRecord record = new AirtableRecord();
                        record.putField("Name", data.name);
                        record.putField("Checkbox", data.checkbox);
                        record.putField("Long Text", data.longText);
                        record.putField("Double", data.aDouble);
                        record.putField("Integer", data.aInteger);
                        return record;
                    }
                };
            }

            @Override
            protected boolean same(AirtableRecord fromIterator, AirtableRecord fromAirtable) {
                // You might want to add a timestamp to simplify checking
                String left = fromIterator.getFieldString("Name");
                String right = fromAirtable.getFieldString("Name");
                return Objects.equals(left, right);
            }

            @Override
            protected boolean has(String fieldValue) {
                return database.get(fieldValue) != null;
            }
        };

        // Run to mirror over.
        mirror.run();

        // Example of how to async run it every 6 hours.
//        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
//        ses.scheduleAtFixedRate(mirror, 0, 6, TimeUnit.HOURS);
    }

    // An example database
    public static class Database {
        public Map<String, Data> DATA = new HashMap<>();

        public Database() {
            DATA.put("Name 1", new Data("Name 1", true, "Line 1\nLine 2", 42.9, -42));
            DATA.put("Name 2", new Data("Name 2", true, null, null, null));
            DATA.put("Name 3", new Data("Name 3", true, null, null, null));
        }

        public Iterator<Data> iterator() {
            return DATA.values().iterator();
        }

        @Nullable
        public Data get(String key) {
            return DATA.get(key);
        }

        public static class Data {
            private String name;
            private Boolean checkbox;
            private String longText;
            private Double aDouble;
            private Integer aInteger;

            public Data(String name, Boolean checkbox, String longText, Double aDouble, Integer aInteger) {
                this.name = name;
                this.checkbox = checkbox;
                this.longText = longText;
                this.aDouble = aDouble;
                this.aInteger = aInteger;
            }
        }
    }
}