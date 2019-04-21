package dev.fuxing.airtable;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 00:17
 */
public interface AirtableBase {

    /**
     * @param table name of a table from Airtable.
     * @return AirtableTable interface
     */
    AirtableTable table(String table);
}
