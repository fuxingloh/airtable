package dev.fuxing.airtable.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.fuxing.airtable.AirtableApi;
import dev.fuxing.airtable.AirtableExecutor;
import dev.fuxing.airtable.AirtableRecord;
import dev.fuxing.airtable.AirtableTable;
import dev.fuxing.airtable.exceptions.AirtableApiException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 2019-04-23
 * Time: 18:08
 */
public class AirtableCacheGuava implements AirtableCache {
    private final Cache<String, AirtableRecord> getCache;
    private final Cache<String, List<AirtableRecord>> queryCache;

    private final AirtableTable table;

    /**
     * Use builder. This is a fallback for those that don't know how to use builder.
     * <p>
     * AirtableCacheGuava need to construct it's own AirtableTable interface because it need to control how 429 auto try works.
     * Default max items is 1200 for get & query because that's the limit for the free Plan.
     *
     * @param apiKey Airtable ApiKey
     * @param base   id of the app. (prefixed with 'app')
     * @param table  table name of the table in Airtable
     */
    public AirtableCacheGuava(String apiKey, String base, String table) {
        this((BuilderImpl) builder());
    }

    AirtableCacheGuava(BuilderImpl builder) {
        AirtableApi api = new AirtableApi(builder.apiKey, AirtableExecutor.newInstanceTurbo());
        this.table = api.base(builder.base).table(builder.table);

        this.getCache = CacheBuilder.newBuilder()
                .maximumSize(builder.getCacheSize)
                .expireAfterAccess(builder.getDuration, builder.getTimeUnit)
                .build();

        this.queryCache = CacheBuilder.newBuilder()
                .maximumSize(builder.queryCacheSize)
                .expireAfterAccess(builder.queryDuration, builder.queryTimeUnit)
                .build();
    }

    @Nullable
    @Override
    public AirtableRecord get(String recordId) throws AirtableApiException {
        try {
            AirtableRecord record = table.get(recordId);
            if (record != null) {
                getCache.put(recordId, record);
            }

            return record;
        } catch (AirtableApiException e) {
            AirtableRecord record = getCache.getIfPresent(recordId);

            // if record exist and error is ignorable
            if (record != null && isIgnorable(e)) {
                return record;
            }

            throw e;
        }
    }

    @Nonnull
    public List<AirtableRecord> query(AirtableTable.QuerySpec querySpec) throws AirtableApiException {
        String cacheKey = StringUtils.trimToEmpty(querySpec.build().getQuery());

        try {
            AirtableTable.PaginationList list = table.list(querySpec);
            if (!list.isEmpty()) {
                queryCache.put(cacheKey, list);
            }

            return list;
        } catch (AirtableApiException e) {
            List<AirtableRecord> list = queryCache.getIfPresent(cacheKey);

            // if record exist and error is ignorable
            if (list != null && isIgnorable(e)) {
                return list;
            }

            throw e;
        }
    }

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
    public static AirtableCache create(Function<Builder.StepKey, Builder> consumer) {
        BuilderImpl builder = (BuilderImpl) consumer.apply(builder());
        return new AirtableCacheGuava(builder);
    }

    /**
     * @return AirtableCacheGuava builder
     */
    public static Builder.StepKey builder() {
        return new BuilderImpl();
    }

    static class BuilderImpl implements Builder, Builder.StepKey, Builder.StepBase, Builder.StepTable {
        String apiKey;
        String base;
        String table;

        int getCacheSize = 1200;
        int getDuration = 12;
        TimeUnit getTimeUnit = TimeUnit.HOURS;

        int queryCacheSize = 1200;
        int queryDuration = 12;
        TimeUnit queryTimeUnit = TimeUnit.HOURS;

        @Override
        public StepBase apiKey(String apiKey) {
            this.apiKey = Objects.requireNonNull(apiKey);
            return this;
        }

        @Override
        public StepTable app(String id) {
            this.base = Objects.requireNonNull(id);
            return this;
        }

        @Override
        public Builder table(String table) {
            this.table = Objects.requireNonNull(table);
            return this;
        }

        @Override
        public Builder withGet(int size, int duration, TimeUnit timeUnit) {
            this.getCacheSize = size;
            this.getDuration = duration;
            this.getTimeUnit = timeUnit;
            return this;
        }

        @Override
        public Builder withQuery(int size, int duration, TimeUnit timeUnit) {
            this.queryCacheSize = size;
            this.queryDuration = duration;
            this.queryTimeUnit = timeUnit;
            return this;
        }
    }

    /**
     * AirtableCache with Guava builder.
     * <p>
     * I saw somewhere someone used a fluent Step builder for force a configuration.
     * I thought it's quite cool I wanted to give it a shot for this module.
     */
    interface Builder {
        interface StepKey {

            /**
             * @param apiKey Airtable ApiKey
             * @return The next step, StepBase
             */
            StepBase apiKey(String apiKey);
        }

        interface StepBase {

            /**
             * @param id of the app. (prefixed with 'app')
             * @return The next step, StepTable
             */
            default StepTable base(String id) {
                return app(id);
            }


            /**
             * @param id of the app. (prefixed with 'app')
             * @return The next step, StepTable
             */
            StepTable app(String id);
        }

        interface StepTable {

            /**
             * @param table table name of the table in Airtable
             * @return the final step, Builder
             */
            Builder table(String table);
        }

        /**
         * @param size     size of get cache
         * @param duration duration of staying in get cache
         * @param timeUnit timeUnit of staying in get cache
         * @return the same Builder instance for fluent chaining
         */
        Builder withGet(int size, int duration, TimeUnit timeUnit);

        /**
         * @param size     size of query cache
         * @param duration duration of staying in query cache
         * @param timeUnit timeUnit of staying in query cache
         * @return the same Builder instance for fluent chaining
         */
        Builder withQuery(int size, int duration, TimeUnit timeUnit);
    }
}