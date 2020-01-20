package dev.fuxing.airtable;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.fuxing.airtable.exceptions.AirtableClientException;
import dev.fuxing.airtable.fields.AttachmentField;
import dev.fuxing.airtable.fields.CollaboratorField;
import dev.fuxing.airtable.formula.LogicalOperator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.field;
import static dev.fuxing.airtable.formula.AirtableFormula.Object.value;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:13
 *
 * @since 0.3.0
 */
class AirtableApiBatchTest {
    static AirtableApi api;
    static AirtableApi.Table table;

    static final String TEST_NAME = "TEST_NAME";

    @BeforeAll
    static void beforeAll() {
        api = new AirtableApi(System.getenv("AIRTABLE_API_KEY"));
        table = api.app("app3h0gjxLX3Jomw8").table("Test Table");
    }

    @BeforeEach
    @AfterEach
    void cleanUp() {
        AirtableTable.PaginationList list = table.list(querySpec -> {
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value(TEST_NAME));
        });

        // Delete all record called TEST_NAME
        list.forEach(record -> {
            table.delete(record.getId());
        });
    }

    @Test
    void post() throws JsonProcessingException {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record.putField("Checkbox", false);
        record.putField("Long Text", "L1\nL2");
        record.putField("Double", 123.4);
        record.putField("Integer", -111);

        AttachmentField attachment = new AttachmentField();
        attachment.setFilename("Optional.png");
        attachment.setUrl("https://upload.wikimedia.org/wikipedia/commons/5/56/Wiki_Eagle_Public_Domain.png");

        record.putFieldAttachments("Attachments", Collections.singletonList(attachment));

        List<AirtableRecord> records = table.post(Arrays.asList(record, record, record, record, record));
        assertEquals(records.size(), 5);

        for (AirtableRecord posted : records) {
            assertNull(posted.getFieldBoolean("Checkbox"));

            assertEquals(TEST_NAME, posted.getFieldString("Name"));
            assertEquals("L1\nL2", posted.getFieldString("Long Text"));
            assertEquals(123.4, posted.getFieldDouble("Double"));
            assertEquals(-111, posted.getFieldInteger("Integer"));

            List<AttachmentField> attachments = posted.getFieldAttachmentList("Attachments");
            assertNotNull(attachments);
            assertEquals(1, attachments.size());
        }
    }

    @Test
    void patch() throws InterruptedException {
        AirtableRecord record1 = new AirtableRecord();
        record1.putField("Name", TEST_NAME);
        record1.putField("Double", -34.3);

        AirtableRecord record2 = new AirtableRecord();
        record2.putField("Name", TEST_NAME);
        record2.putField("Double", -34.3);


        List<AirtableRecord> records = table.post(Arrays.asList(record1, record2));
        List<AirtableRecord> putRecords = new ArrayList<>();

        records.forEach(airtableRecord -> {
            AirtableRecord record = new AirtableRecord(airtableRecord.getId());
            record.putField("Long Text", "PUT TEST");
            putRecords.add(record);
        });

        table.patch(putRecords);

        putRecords.forEach(record -> {
            AirtableRecord patched = table.get(record.getId());
            assertNotNull(patched);

            assertEquals(TEST_NAME, patched.getFieldString("Name"));
            assertEquals(-34.3, patched.getFieldDouble("Double"));

            assertEquals("PUT TEST", patched.getFieldString("Long Text"));
        });
    }

    @Test
    void put() throws InterruptedException {
        AirtableRecord record1 = new AirtableRecord();
        record1.putField("Name", TEST_NAME);
        record1.putField("Double", -34.3);

        AirtableRecord record2 = new AirtableRecord();
        record2.putField("Name", TEST_NAME);
        record2.putField("Integer", 12345);

        List<AirtableRecord> records = table.post(Arrays.asList(record1, record2));
        List<AirtableRecord> putRecords = new ArrayList<>();

        records.forEach(airtableRecord -> {
            AirtableRecord record = new AirtableRecord(airtableRecord.getId());
            record.putField("Name", TEST_NAME);
            record.putField("Long Text", "PUT TEST");
            putRecords.add(record);
        });

        table.put(putRecords);

        putRecords.forEach(record -> {
            AirtableRecord patched = table.get(record.getId());
            assertNotNull(patched);

            assertEquals(TEST_NAME, patched.getFieldString("Name"));
            assertNull(patched.getFieldDouble("Double"));
            assertNull(patched.getFieldInteger("Integer"));

            assertEquals("PUT TEST", patched.getFieldString("Long Text"));
        });
    }

    @Test
    void delete() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);

        List<AirtableRecord> records = table.post(Arrays.asList(record, record, record, record, record));
        assertEquals(records.size(), 5);

        List<String> recordIds = new ArrayList<>();
        records.forEach(airtableRecord -> recordIds.add(airtableRecord.getId()));

        List<String> deletedRecordIds = table.delete(recordIds);
        assertEquals(deletedRecordIds.size(), 5);


        AirtableTable.PaginationList list = table.list(querySpec -> {
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value(TEST_NAME));
        });
        assertEquals(list.size(), 0);
    }

    @Test
    void postMax10() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);

        assertThrows(AirtableClientException.class, () -> {
            table.post(Arrays.asList(
                    record, record, record, record, record,
                    record, record, record, record, record,
                    record
            ));
        });
    }

    @Test
    void putMax10() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);

        assertThrows(AirtableClientException.class, () -> {
            table.put(Arrays.asList(
                    record, record, record, record, record,
                    record, record, record, record, record,
                    record
            ));
        });
    }

    @Test
    void patchMax10() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);

        assertThrows(AirtableClientException.class, () -> {
            table.patch(Arrays.asList(
                    record, record, record, record, record,
                    record, record, record, record, record,
                    record
            ));
        });
    }

    @Test
    void deleteMax10() {
        assertThrows(AirtableClientException.class, () -> {
            table.delete(Arrays.asList(
                    "rec1", "rec2", "rec3", "rec4", "rec5",
                    "rec6", "rec7", "rec8", "rec9", "rec10",
                    "rec11"
            ));
        });
    }
}
