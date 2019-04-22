package dev.fuxing.airtable;

import dev.fuxing.airtable.formula.AirtableFormula;
import dev.fuxing.airtable.formula.AirtableFunction;
import dev.fuxing.airtable.formula.AirtableOperator;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 2019-04-21
 * Time: 00:17
 */
public interface AirtableTable {

    /**
     * @return name of the base the table is in.
     */
    String baseName();

    /**
     * @return name of the table
     */
    String tableName();

    /**
     * To iterator all records in a table.
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @return Iterator support for AirtableRecord, using build in airtable pagination to paginate all the records
     */
    default Iterator<AirtableRecord> iterator() {
        return iterator(querySpec -> {
        });
    }

    /**
     * To iterator records in a table.
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @param consumer query spec fluent consumer with all the querystring options.
     * @return Iterator support for AirtableRecord, using build in airtable pagination to paginate all the records
     */
    default Iterator<AirtableRecord> iterator(Consumer<QuerySpec> consumer) {
        QuerySpec querySpec = QuerySpec.create();
        consumer.accept(querySpec);
        return iterator(querySpec);
    }

    /**
     * To list records in a table, issue a GET request to the Records endpoint.
     * <p>
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @param querySpec fluent query spec with all the querystring options
     * @return Iterator support for AirtableRecord, using build in airtable pagination to paginate all the records
     */
    default Iterator<AirtableRecord> iterator(QuerySpec querySpec) {
        return new Iterator<AirtableRecord>() {
            PaginationList pagination = list(querySpec);
            Iterator<AirtableRecord> records = pagination.iterator();

            @Override
            public boolean hasNext() {
                if (records.hasNext()) return true;

                if (pagination.getOffset() != null) {
                    pagination = list(querySpec.offset(pagination.getOffset()));
                    records = pagination.iterator();
                    return hasNext();
                }
                return false;
            }

            @Override
            public AirtableRecord next() {
                return records.next();
            }
        };
    }

    /**
     * To list records in a table without query spec.
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @return PaginationList of AirtableRecord
     */
    default PaginationList list() {
        return list(querySpec -> {
        });
    }

    /**
     * To list records in a table.
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @param consumer query spec fluent consumer with all the querystring options.
     * @return PaginationList of AirtableRecord
     */
    default PaginationList list(Consumer<QuerySpec> consumer) {
        QuerySpec querySpec = QuerySpec.create();
        consumer.accept(querySpec);
        return list(querySpec);
    }

    /**
     * To list records in a table, issue a GET request to the Records endpoint.
     * <p>
     * Returned records do not include any fields with "empty" values, e.g. "", [], or false.
     *
     * @param querySpec fluent query spec with all the querystring options
     * @return PaginationList of AirtableRecord
     */
    PaginationList list(QuerySpec querySpec);

    /**
     * To retrieve an existing record in the table, issue a GET request to the record endpoint.
     * Any "empty" fields (e.g. "", [], or false) in the record will not be returned.
     * <p>
     * In attachment objects included in the retrieved record, only id, url, and filename are always returned.
     * Other attachment properties may not be included.
     *
     * @param recordId id of the record
     * @return AirtableRecord, {@code null} if don't exist
     */
    @Nullable
    AirtableRecord get(String recordId);

    /**
     * To create a new record, issue a POST request to the Records endpoint.
     * <p>
     * You can include all, some, or none of the field values.
     * Returns the created record object if the call succeeded, including a record ID which will uniquely identify the record within Sales CRM.
     * <p>
     * To create new attachments, set the field value to an array of attachment objects.
     * When creating an attachment, url is required, and filename is optional.
     * Airtable will download the file at the given url and keep its own copy of it.
     * All other attachment object properties will be generated server-side soon afterward.
     * <p>
     * To set a collaborator in a Field, set the field value to a user object.
     * A user object must contain either an id or an email that uniquely identifies a user who this base is shared with.
     * An id takes precedence over email if both are present. Any missing properties will be filled in automatically based on the matching user.
     * <p>
     * Values for Computed values are automatically computed by Airtable and cannot be directly created.
     *
     * @param record to create
     * @return Created Record.
     */
    default AirtableRecord post(AirtableRecord record) {
        return post(record, false);
    }

    /**
     * To create a new record, issue a POST request to the Records endpoint.
     * <p>
     * You can include all, some, or none of the field values.
     * Returns the created record object if the call succeeded, including a record ID which will uniquely identify the record within Sales CRM.
     * <p>
     * To create new attachments, set the field value to an array of attachment objects.
     * When creating an attachment, url is required, and filename is optional.
     * Airtable will download the file at the given url and keep its own copy of it.
     * All other attachment object properties will be generated server-side soon afterward.
     * <p>
     * To set a collaborator in a Field, set the field value to a user object.
     * A user object must contain either an id or an email that uniquely identifies a user who this base is shared with.
     * An id takes precedence over email if both are present.
     * Any missing properties will be filled in automatically based on the matching user.
     * <p>
     * Values for Computed values are automatically computed by Airtable and cannot be directly created.
     *
     * @param record   to create
     * @param typecast The Airtable API will perform best-effort automatic data conversion from string values if the typecast parameter is passed in.
     *                 Automatic conversion is disabled by default to ensure data integrity, but it may be helpful for integrating with 3rd party data sources.
     * @return Created Record.
     */
    AirtableRecord post(AirtableRecord record, boolean typecast);

    /**
     * To update some (but not all) fields of records, issue a PATCH request to the record endpoint.
     * Any fields that are not included will not be updated.
     * <p>
     * To add attachments, add new attachment objects to the existing array.
     * Be sure to include all existing attachment objects that you wish to retain.
     * For the new attachments being added, url is required, and filename is optional.
     * To remove attachments, include the existing array of attachment objects, excluding any that you wish to remove.
     * <p>
     * To set a collaborator, set the field value to a user object.
     * A user object must contain either an id or an email that uniquely identifies a user who this base is shared with.
     * An id takes precedence over email if both are present.
     * Any missing properties will be filled in automatically based on the matching user.
     * <p>
     * Values for Computed values are automatically computed by Airtable and cannot be directly created.
     *
     * @param record to patch
     * @return Patched Record
     */
    default AirtableRecord patch(AirtableRecord record) {
        return patch(record, false);
    }

    /**
     * To update some (but not all) fields of records, issue a PATCH request to the record endpoint.
     * Any fields that are not included will not be updated.
     * <p>
     * To add attachments, add new attachment objects to the existing array.
     * Be sure to include all existing attachment objects that you wish to retain.
     * For the new attachments being added, url is required, and filename is optional.
     * To remove attachments, include the existing array of attachment objects, excluding any that you wish to remove.
     * <p>
     * To set a collaborator, set the field value to a user object.
     * A user object must contain either an id or an email that uniquely identifies a user who this base is shared with.
     * An id takes precedence over email if both are present.
     * Any missing properties will be filled in automatically based on the matching user.
     * <p>
     * Values for Computed values are automatically computed by Airtable and cannot be directly created.
     *
     * @param record   to patch
     * @param typecast The Airtable API will perform best-effort automatic data conversion from string values if the typecast parameter is passed in.
     *                 Automatic conversion is disabled by default to ensure data integrity, but it may be helpful for integrating with 3rd party data sources.
     * @return Patched Record
     */
    AirtableRecord patch(AirtableRecord record, boolean typecast);

    /**
     * To delete a record from Table, issue a DELETE request to the record endpoint.
     *
     * @param recordId id of the record
     * @return whether record has been deleted.
     */
    boolean delete(String recordId);

    /**
     * A fluent interface for querying records in Airtable/Application/Table.
     */
    interface QuerySpec {

        /**
         * @return QuerySpec instance for fluent chaining
         */
        static QuerySpec create() {
            return new AirtableApi.QuerySpecImpl();
        }

        /**
         * @param offset for next list of the pagination
         * @return QuerySpec instance for fluent chaining
         * @see PaginationList#getOffset()
         */
        QuerySpec offset(String offset);

        /**
         * Only data for fields whose names are in this list will be included in the records.
         * If you don't need every field, you can use this parameter to reduce the amount of data transferred.
         *
         * @param fields fields whose names are in this list will be included in the records.
         * @return QuerySpec instance for fluent chaining
         */
        default QuerySpec fields(String... fields) {
            return fields(Arrays.asList(fields));
        }

        /**
         * Only data for fields whose names are in this list will be included in the records.
         * If you don't need every field, you can use this parameter to reduce the amount of data transferred.
         *
         * @param fields fields whose names are in this list will be included in the records.
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec fields(List<String> fields);

        /**
         * A formula used to filter records. The formula will be evaluated for each record, and if the result is not 0, false, "", NaN, [], or #Error! the record will be included in the response.
         * <p>
         * If combined with view, only records in that view which satisfy the formula will be returned.
         * <p>
         * For example, to only include records where a field isn't empty, pass in: NOT({Field Name} = '')
         *
         * @param formula a formula used to filter records
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec filterByFormula(String formula);

        /**
         * @param operator an airtable operator to use
         * @param left     value
         * @param right    value
         * @param others   values on the right
         * @return QuerySpec instance for fluent chaining
         * @see QuerySpec#filterByFormula(String)
         */
        default QuerySpec filterByFormula(AirtableOperator operator, AirtableFormula.Object left, AirtableFormula.Object right, AirtableFormula.Object... others) {
            return filterByFormula(operator.apply(left, right, others));
        }

        /**
         * @param function an airtable function to use
         * @param objects  in the function
         * @return QuerySpec instance for fluent chaining
         * @see QuerySpec#filterByFormula(String)
         */
        default QuerySpec filterByFormula(AirtableFunction function, AirtableFormula.Object... objects) {
            return filterByFormula(function.apply(objects));
        }

        /**
         * The maximum total number of records that will be returned in your requests.
         * If this value is larger than pageSize (which is 100 by default), you may have to load multiple pages to reach this total.
         * See the Pagination section below for more.
         *
         * @param size the maximum total number of records that will be returned in your requests.
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec maxRecords(int size);

        /**
         * The number of records returned in each request.
         * Must be less than or equal to 100.
         * Default is 100.
         * See the Pagination section below for more.
         *
         * @param size must be less than or equal to 100
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec pageSize(int size);

        /**
         * @param field to sort records by in asc order.
         * @return QuerySpec instance for fluent chaining
         */
        default QuerySpec sort(String field) {
            return sort(field, null);
        }

        /**
         * A list of sort objects that specifies how the records will be ordered. Each sort object must have a field key specifying the name of the field to sort on, and an optional direction key that is either "asc" or "desc". The default direction is "asc".
         * <p>
         * For example, to sort records by a field in descending order, send these two query parameters:<br>
         * sort[0][field]=Field Name
         * sort[0][direction]=desc
         *
         * @param field     to sort records by.
         * @param direction to sort the field by, optional. Defaults to asc.
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec sort(String field, @Nullable String direction);

        /**
         * The name or ID of a view in the table.
         * If set, only the records in that view will be returned.
         * The records will be sorted according to the order of the view.
         *
         * @param name or id of a view in the current table.
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec view(String name);

        /**
         * The format that should be used for cell values. Supported values are: <br>
         * "json": cells will be formatted as JSON, depending on the field type.<br>
         * "string": cells will be formatted as user-facing strings, regardless of the field type. Note: You should not rely on the format of these strings, as it is subject to change.<br>
         * The default is "json".
         *
         * @param format optional cell format be used for cell values.
         * @return QuerySpec instance for fluent chaining
         */
        QuerySpec cellFormat(String format);

        /**
         * The time zone that should be used to format dates when using "string" as the cellFormat.
         * This parameter is required when using "string" as the cellFormat.
         *
         * @param zone time zone identifier, required if cellFormat is 'string'
         * @return QuerySpec instance for fluent chaining
         * @see <a href="https://support.airtable.com/hc/en-us/articles/216141558-Supported-timezones-for-SET-TIMEZONE">Airtable: Time Zone</a>
         */
        QuerySpec timeZone(String zone);

        /**
         * @param zoneId instance, required if cellFormat is 'string'
         * @return QuerySpec instance for fluent chaining
         * @see QuerySpec#timeZone(String)
         */
        default QuerySpec timeZone(ZoneId zoneId) {
            return timeZone(zoneId.getId());
        }

        /**
         * The user locale that should be used to format dates when using "string" as the cellFormat.
         * This parameter is required when using "string" as the cellFormat.
         *
         * @param locale modifiers, required if cellFormat is 'string'
         * @return QuerySpec instance for fluent chaining
         * @see <a href="https://support.airtable.com/hc/en-us/articles/220340268-Supported-locale-modifiers-for-SET-LOCALE">Airtable: User Locale</a>
         */
        QuerySpec userLocale(String locale);

        /**
         * @param locale instance, required if cellFormat is 'string'
         * @return QuerySpec instance for fluent chaining
         * @see QuerySpec#userLocale(String)
         */
        default QuerySpec userLocale(Locale locale) {
            return userLocale(locale.toLanguageTag().toLowerCase());
        }

        /**
         * @return query spec into uri
         */
        URI build();
    }

    /**
     * The server returns one page of records at a time.
     * Each page will contain pageSize records, which is 100 by default.
     * <p>
     * If there are more records, the response will contain an offset.
     * To fetch the next page of records, include offset in the next request's parameters.
     * <p>
     * Pagination will stop when you've reached the end of your table.
     * If the maxRecords parameter is passed, pagination will stop once you've reached this maximum.
     * <p>
     * Iteration may timeout due to client inactivity or server restarts.
     * In that case, the client will receive a 422 response with error message LIST_RECORDS_ITERATOR_NOT_AVAILABLE.
     * It may then restart iteration from the beginning.
     */
    interface PaginationList extends List<AirtableRecord> {

        /**
         * Iteration may timeout due to client inactivity or server restarts.
         * In that case, the client will receive a 422 response with error message LIST_RECORDS_ITERATOR_NOT_AVAILABLE.
         * It may then restart iteration from the beginning.
         *
         * @return If there are more records, the response will contain an offset. To fetch the next page of records, include offset in the next request's parameters.
         */
        @Nullable
        String getOffset();
    }
}
