package dev.fuxing.airtable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import dev.fuxing.airtable.fields.AttachmentField;
import dev.fuxing.airtable.fields.CollaboratorField;
import org.apache.commons.lang3.time.FastDateFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.*;

/**
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 22:04
 */
public class AirtableRecord {
    public static final ObjectMapper OBJECT_MAPPER = AirtableApi.OBJECT_MAPPER;
    public static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String id;
    private Map<String, JsonNode> fields;
    private Date createdTime;

    public AirtableRecord() {
        this.fields = new HashMap<>();
    }

    /**
     * @param node json node from https://api.airtable.com/v0
     */
    public AirtableRecord(JsonNode node) {
        this.id = node.path("id").asText();
        this.fields = new HashMap<>();

        try {
            this.createdTime = DATE_FORMAT.parse(node.path("createdTime").asText());
        } catch (ParseException e) {
            // Shouldn't happen, wrapped in IllegalStateException
            throw new IllegalStateException(e);
        }

        node.path("fields").fields().forEachRemaining(entry -> {
            this.fields.put(entry.getKey(), entry.getValue());
        });
    }

    /**
     * @return id of the record, prefixed with 'rec'
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return all fields in the record
     */
    public Map<String, JsonNode> getFields() {
        return fields;
    }

    public void setFields(Map<String, JsonNode> fields) {
        this.fields = fields;
    }

    /**
     * @return date time when the record is created
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @param name  of the field
     * @param value string value
     */
    public void putField(String name, String value) {
        putField(name, (Object) value);
    }

    /**
     * @param name  of the field
     * @param value int value
     */
    public void putField(String name, int value) {
        putField(name, (Object) value);
    }

    /**
     * @param name  of the field
     * @param value long value
     */
    public void putField(String name, long value) {
        putField(name, (Object) value);
    }

    /**
     * @param name  of the field
     * @param value double value
     */
    public void putField(String name, double value) {
        putField(name, (Object) value);
    }

    /**
     * @param name  of the field
     * @param value boolean value
     */
    public void putField(String name, boolean value) {
        putField(name, (Object) value);
    }

    /**
     * @param name of the field
     * @param date object
     */
    public void putField(String name, Date date) {
        putField(name, (Object) DATE_FORMAT.format(date));
    }

    /**
     * @param name   of the field
     * @param values array object
     */
    public void putField(String name, List<String> values) {
        putField(name, (Object) values);
    }

    /**
     * @param name       of the field
     * @param attachment object
     * @see AttachmentField documents for Patch & Create operations
     */
    public void putField(String name, AttachmentField attachment) {
        putField(name, (Object) attachment);
    }

    /**
     * @param name         of the field
     * @param collaborator object
     * @see CollaboratorField documents for Patch & Create operations
     */
    public void putField(String name, CollaboratorField collaborator) {
        putField(name, (Object) collaborator);
    }

    /**
     * @param name  of the field
     * @param value to be mapped into JsonNode
     */
    public void putField(String name, Object value) {
        putField(name, OBJECT_MAPPER.valueToTree(value));
    }

    /**
     * @param name of the field
     * @param node raw JsonNode
     */
    public void putField(String name, JsonNode node) {
        fields.put(name, node);
    }

    /**
     * @param name name of field
     * @return JsonNode or Missing Node if not found
     */
    public JsonNode getField(String name) {
        return getFields().getOrDefault(name, MissingNode.getInstance());
    }

    /**
     * @param name  of field
     * @param clazz to convert field array into
     * @param <T>   converted type
     * @return nonnull List of field in T type
     */
    @Nonnull
    public <T> List<T> getFieldList(String name, Class<T> clazz) {
        JsonNode field = getField(name);
        if (field.isMissingNode()) return Collections.emptyList();
        CollectionType type = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
        return OBJECT_MAPPER.convertValue(field, type);
    }

    @Nullable
    public String getFieldString(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;
        return jsonNode.asText(null);
    }

    @Nullable
    public Integer getFieldInteger(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;
        return jsonNode.asInt();
    }

    @Nullable
    public Long getFieldLong(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;
        return jsonNode.asLong();
    }

    @Nullable
    public Double getFieldDouble(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;
        return jsonNode.asDouble();
    }

    @Nullable
    public Boolean getFieldBoolean(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;
        return jsonNode.asBoolean();
    }

    @Nullable
    public Date getFieldDate(String name) {
        JsonNode jsonNode = fields.get(name);
        if (jsonNode == null) return null;

        try {
            return DATE_FORMAT.parse(jsonNode.asText());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param name of the field
     * @return wrapped into CollaboratorField
     */
    @Nullable
    public CollaboratorField getFieldCollaborator(String name) {
        JsonNode field = getField(name);
        if (field == null) return null;
        return new CollaboratorField(field);
    }

    /**
     * @param name of the field
     * @return wrapped into AttachmentField
     */
    @Nullable
    public AttachmentField getFieldAttachment(String name) {
        JsonNode field = getField(name);
        if (field == null) return null;
        return new AttachmentField(field);
    }

    @Override
    public String toString() {
        return "AirtableRecord{" +
                "id='" + id + '\'' +
                ", fields=" + fields +
                ", createdTime=" + createdTime +
                '}';
    }
}
