package dev.fuxing.airtable;

import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 16:23
 */
public class AirtableList extends ArrayList<AirtableRecord> implements AirtableTable.PaginationList {

    private String offset;

    public AirtableList(JsonNode node) {
        super(parse(node.path("records")));
        this.offset = node.path("offset").asText(null);
    }

    /**
     * @return offset
     * @see AirtableTable.PaginationList#getOffset()
     */
    @Nullable
    @Override
    public String getOffset() {
        return offset;
    }

    private static List<AirtableRecord> parse(JsonNode node) {
        if (node.isMissingNode()) return Collections.emptyList();

        List<AirtableRecord> records = new ArrayList<>();
        for (JsonNode jsonNode : node) {
            records.add(new AirtableRecord(jsonNode));
        }
        return records;
    }
}
