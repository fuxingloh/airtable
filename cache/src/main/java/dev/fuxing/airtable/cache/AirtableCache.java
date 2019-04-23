package dev.fuxing.airtable.cache;

import dev.fuxing.airtable.AirtableRecord;
import dev.fuxing.airtable.AirtableTable;
import dev.fuxing.airtable.exceptions.AirtableApiException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 2019-04-23
 * Time: 17:40
 */
public interface AirtableCache {

    /**
     * <pre>
     * create(builder -> builder
     *      .apiKey("key...")
     *      .app("app...")
     *      .table("Table Name")
     * )
     * </pre>
     *
     * @param consumer for building, requires user to return at step Builder
     * @return AirtableCache
     */
    static AirtableCache create(Function<AirtableCacheGuava.Builder.StepKey, AirtableCacheGuava.Builder> consumer) {
        return AirtableCacheGuava.create(consumer);
    }

    /**
     * Get will always attempt to get the latest record from airtable server.
     * Fallback read from cache will only happen if any of the ignorable exception is thrown.
     * <p>
     * Ignorable {@link AirtableApiException} are: 429, 500, 502, 503
     *
     * @param recordId id of the record, prefixed with 'rec'
     * @return {@link AirtableRecord} or {@code null} if don't exist
     * @throws AirtableApiException if exception is not ignorable or it is not previously cached before
     * @see AirtableCache#isIgnorable(AirtableApiException)
     */
    @Nullable
    AirtableRecord get(String recordId) throws AirtableApiException;

    /**
     * Query will always attempt to get the latest result from airtable server.
     * Fallback read from cache will only happen if any of the ignorable exception is thrown.
     * The cache key used will be the querystring.
     * <p>
     * Ignorable {@link AirtableApiException} are: 429, 500, 502, 503
     *
     * @return List of AirtableRecord or empty List
     * @throws AirtableApiException if exception is not ignorable or it is not previously cached before
     * @see AirtableCache#isIgnorable(AirtableApiException)
     */
    @Nonnull
    default List<AirtableRecord> query() throws AirtableApiException {
        return query(querySpec -> {
        });
    }

    /**
     * Query will always attempt to get the latest result from airtable server.
     * Fallback read from cache will only happen if any of the ignorable exception is thrown.
     * The cache key used will be the querystring.
     * <p>
     * Ignorable {@link AirtableApiException} are: 429, 500, 502, 503
     *
     * @param consumer query spec fluent consumer with all the querystring options.
     * @return List of AirtableRecord or empty List
     * @throws AirtableApiException if exception is not ignorable or it is not previously cached before
     * @see AirtableCache#isIgnorable(AirtableApiException)
     */
    @Nonnull
    default List<AirtableRecord> query(Consumer<AirtableTable.QuerySpec> consumer) throws AirtableApiException {
        AirtableTable.QuerySpec querySpec = AirtableTable.QuerySpec.create();
        consumer.accept(querySpec);
        return query(querySpec);
    }

    /**
     * Query will always attempt to get the latest result from airtable server.
     * Fallback read from cache will only happen if any of the ignorable exception is thrown.
     * The cache key used will be the querystring.
     * <p>
     * Ignorable {@link AirtableApiException} are: 429, 500, 502, 503
     *
     * @param querySpec fluent query spec with all the querystring options
     * @return List of AirtableRecord or empty List
     * @throws AirtableApiException if exception is not ignorable or it is not previously cached before
     * @see AirtableCache#isIgnorable(AirtableApiException)
     */
    @Nonnull
    List<AirtableRecord> query(AirtableTable.QuerySpec querySpec) throws AirtableApiException;

    /**
     * Default: 429, Too Many Ignore
     * Airtable Server Exception: 500, 502, 503
     *
     * @param exception to check
     * @return whether this exception is ignorable
     */
    default boolean isIgnorable(AirtableApiException exception) {
        switch (exception.getCode()) {
            case 429:
            case 500:
            case 502:
            case 503:
                return true;
        }

        return false;
    }
}
