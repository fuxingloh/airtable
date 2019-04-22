package dev.fuxing.airtable;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.fuxing.airtable.fields.AttachmentField;
import dev.fuxing.airtable.fields.CollaboratorField;
import dev.fuxing.airtable.formula.LogicalOperator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.field;
import static dev.fuxing.airtable.formula.AirtableFormula.Object.value;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 18:13
 */
class AirtableApiTest {
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
    void get() {
        AirtableRecord record = table.get("rec0W9eGVAFSy9Chb");
        assertNotNull(record);

        assertEquals("Name 1", record.getFieldString("Name"));
        assertEquals(true, record.getFieldBoolean("Checkbox"));
        assertEquals("Line 1\nLine 2", record.getFieldString("Long Text"));
        assertEquals(42.9, record.getFieldDouble("Double"));
        assertEquals(-42, record.getFieldInteger("Integer"));

        // Attachments
        List<AttachmentField> attachments = record.getFieldAttachmentList("Attachments");
        assertNotNull(attachments);
        assertEquals(1, attachments.size());

        AttachmentField attachment = attachments.get(0);
        assertNotNull(attachment.getId());
        assertNotNull(attachment.getUrl());
        assertNotNull(attachment.getSize());

        assertFalse(attachment.getThumbnails().isEmpty());

        CollaboratorField collaborator = record.getFieldCollaborator("Collaborator");
        assertNotNull(collaborator);
        assertNotNull(collaborator.getId());
        assertNotNull(collaborator.getEmail());
        assertNotNull(collaborator.getName());
    }

    @Test
    void post() throws JsonProcessingException {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record.putField("Checkbox", false);
        record.putField("Long Text", "L1\nL2");
        record.putField("Double", 123.4);
        record.putField("Integer", -111);

        AttachmentField field = new AttachmentField();
        field.setFilename("Optional.png");
        field.setUrl("https://upload.wikimedia.org/wikipedia/commons/5/56/Wiki_Eagle_Public_Domain.png");

        record.putFieldAttachments("Attachments", Collections.singletonList(field));
        record = table.post(record);

        // If false, == null
        assertNull(record.getFieldBoolean("Checkbox"));

        assertEquals(TEST_NAME, record.getFieldString("Name"));
        assertEquals("L1\nL2", record.getFieldString("Long Text"));
        assertEquals(123.4, record.getFieldDouble("Double"));
        assertEquals(-111, record.getFieldInteger("Integer"));

        List<AttachmentField> attachments = record.getFieldAttachmentList("Attachments");
        assertNotNull(attachments);
        assertEquals(1, attachments.size());
    }

    @Test
    void postTypecast() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record.putField("Integer", "42");

        record = table.post(record, true);
        assertEquals(TEST_NAME, record.getFieldString("Name"));
        assertEquals(42, record.getFieldInteger("Integer"));
    }

    @Test
    void patch() throws InterruptedException {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record = table.post(record);

        record = new AirtableRecord(record.getId());
        record.putField("Double", -34.3);
        record.putField("Integer", 12345);
        table.patch(record);

        AirtableRecord patched = table.get(record.getId());
        assertNotNull(patched);
        assertEquals(TEST_NAME, patched.getFieldString("Name"));
        assertEquals(-34.3, patched.getFieldDouble("Double"));
        assertEquals(12345, patched.getFieldInteger("Integer"));
    }

    @Test
    void patchTypecast() throws InterruptedException {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record = table.post(record);

        record = new AirtableRecord(record.getId());
        record.putField("Integer", "42");
        record = table.patch(record, true);
        assertEquals(42, record.getFieldInteger("Integer"));
    }

    @Test
    void delete() {
        AirtableRecord record = new AirtableRecord();
        record.putField("Name", TEST_NAME);
        record = table.post(record);

        boolean delete = table.delete(record.getId());
        assertTrue(delete);

        AirtableTable.PaginationList list = table.list(querySpec -> {
            querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value(TEST_NAME));
        });
        assertEquals(list.size(), 0);
    }
}