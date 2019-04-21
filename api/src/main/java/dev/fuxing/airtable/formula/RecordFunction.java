package dev.fuxing.airtable.formula;

/**
 * @see <a href="https://support.airtable.com/hc/en-us/articles/203255215-Formula-Field-Reference#record_functions">Record functions</a>
 * <p>
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 02:06
 */
public interface RecordFunction extends AirtableFunction {

    /**
     * Returns the creation time of the current record.
     */
    RecordFunction CREATED_TIME = s -> "CREATED_TIME()";

    /**
     * Returns the ID of the current record.
     */
    RecordFunction RECORD_ID = s -> "RECORD_ID()";
}
