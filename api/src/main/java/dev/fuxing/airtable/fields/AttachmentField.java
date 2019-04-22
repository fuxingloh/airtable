package dev.fuxing.airtable.fields;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * CREATE:<br>
 * To create new attachments in Proposal, set the field value to an array of attachment objects.
 * When creating an attachment, url is required, and filename is optional.
 * Airtable will download the file at the given url and keep its own copy of it.
 * All other attachment object properties will be generated server-side soon afterward.
 * <p>
 * PATCH:<br>
 * To add attachments to Proposal, add new attachment objects to the existing array.
 * Be sure to include all existing attachment objects that you wish to retain.
 * For the new attachments being added, url is required, and filename is optional.
 * To remove attachments, include the existing array of attachment objects, excluding any that you wish to remove.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 22:04
 */
public final class AttachmentField {
    private String id;
    private String url;
    private String filename;

    private Long size;
    private String type;

    // These may be available if the attachment is an image
    private Map<String, Thumbnail> thumbnails;

    public AttachmentField() {
    }

    public AttachmentField(JsonNode field) {
        this.id = field.path("id").asText();
        this.url = field.path("url").asText();
        this.filename = field.path("filename").asText();

        this.size = field.path("size").asLong();
        this.type = field.path("type").asText();

        if (field.has("thumbnails")) {
            this.thumbnails = new HashMap<>();
            field.path("thumbnails").fields().forEachRemaining(entry -> {
                this.thumbnails.put(entry.getKey(), new Thumbnail(entry.getValue()));
            });
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public Map<String, Thumbnail> getThumbnails() {
        return thumbnails;
    }

    /**
     * @return Json Serializer for POST
     */
    @JsonValue
    public Map<String, String> getJsonValue() {
        Map<String, String> map = new HashMap<>();
        if (id != null) map.put("id", id);
        if (url != null) map.put("url", url);
        if (filename != null) map.put("filename", filename);
        return map;
    }

    /**
     * "small", "large"
     * <p>
     * These may be available if the attachment is an image or document.
     */
    public static final class Thumbnail {
        private String url;
        private Integer width;
        private Integer height;

        private Thumbnail(JsonNode node) {
            this.url = node.path("url").asText();
            this.width = node.path("width").asInt();
            this.height = node.path("height").asInt();
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        @Override
        public String toString() {
            return "Thumbnail{" +
                    "url='" + url + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AttachmentField{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", thumbnails=" + thumbnails +
                '}';
    }
}
