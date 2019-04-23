package dev.fuxing.airtable.mirror;

import dev.fuxing.airtable.AirtableRecord;
import dev.fuxing.airtable.AirtableTable;
import dev.fuxing.airtable.formula.AirtableFormula;
import dev.fuxing.airtable.formula.LogicalOperator;

import java.util.Iterator;
import java.util.List;

import static dev.fuxing.airtable.formula.AirtableFormula.Object.value;

/**
 * Created by: Fuxing
 * Date: 2019-04-22
 * Time: 23:41
 */
public abstract class AirtableMirror implements Runnable {

    private final AirtableTable table;
    private final AirtableFormula.Field field;

    /**
     * @param table AirtableTable interface
     * @param field primary key field to track & de-dup
     */
    public AirtableMirror(AirtableTable table, AirtableFormula.Field field) {
        this.table = table;
        this.field = field;
    }

    @Override
    public void run() {
        copy();
        validate();
    }

    /**
     * Mirror data from provided source to Airtable
     * - Data that don't exist will be created
     * - Data that is duplicated will be removed
     * - Data that has changed will be patched
     * - Data that hasn't change will not be persisted again
     */
    protected void copy() {
        iterator().forEachRemaining(fromIterator -> {
            List<AirtableRecord> records = table.list(query -> {
                AirtableFormula.Object value = value(field.getString(fromIterator));
                query.filterByFormula(LogicalOperator.EQ, field, value);
            });

            // Create new Record entry if none existed
            if (records.isEmpty()) {
                table.post(fromIterator);
                return;
            }

            // Remove those after index 1
            for (int i = 1; i < records.size(); i++) {
                table.delete(records.get(i).getId());
            }

            // Skip if same
            AirtableRecord fromAirtable = records.get(0);
            if (same(fromIterator, fromAirtable)) {
                return;
            }

            // Patch into Airtable if different
            fromIterator.setId(fromAirtable.getId());
            table.patch(fromIterator);
        });
    }

    /**
     * Delete records that still exists in Airtable but is removed in provided source
     */
    protected void validate() {
        table.iterator().forEachRemaining(record -> {
            if (has(record)) return;

            // Don't exist any more, delete now
            table.delete(record.getId());
        });
    }

    /**
     * The iterator should contains all the records to be mirrored
     *
     * @return Iterator of AirtableRecord to copy into Airtable
     * @see AirtableMirror#same(AirtableRecord, AirtableRecord) will compare and check if persist is required
     * @see AirtableMirror#has(String) used to check if records still exists in the database
     */
    protected abstract Iterator<AirtableRecord> iterator();

    /**
     * @param fromIterator from your iterator
     * @param fromAirtable from airtable
     * @return whether 2 records are the same, if same no persist will happen
     */
    protected abstract boolean same(AirtableRecord fromIterator, AirtableRecord fromAirtable);

    /**
     * @return whether record still exist in main database, false = deleted
     */
    protected boolean has(AirtableRecord fromAirtable) {
        return has(field.getString(fromAirtable));
    }

    /**
     * @param fieldValue from airtable to check if already exist
     * @return whether record still exist in main database, false = deleted
     */
    protected abstract boolean has(String fieldValue);
}
