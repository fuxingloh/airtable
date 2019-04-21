package dev.fuxing.airtable.fields;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * To set a collaborator in a record, set the field value to a user object.
 * A user object must contain either an id or an email that uniquely identifies a user who this base is shared with.
 * An id takes precedence over email if both are present.
 * Any missing properties will be filled in automatically based on the matching user.
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 22:04
 */
public final class CollaboratorField {
    private String id;
    private String email;
    private String name;

    public CollaboratorField() {
    }

    public CollaboratorField(JsonNode field) {
        this.id = field.path("id").asText();
        this.email = field.path("email").asText();
        this.name = field.path("name").asText();
    }

    /**
     * @return unique user id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return user's email address
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return user's display name (optional, may be empty if the user hasn't created an account)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
