package dev.fuxing.airtable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.fuxing.airtable.exceptions.AirtableApiException;
import dev.fuxing.airtable.exceptions.AirtableClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 2019-04-20
 * Time: 22:04
 */
public class AirtableApi {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String apiKey;
    private final Executor executor;

    /**
     * @param apiKey Airtable ApiKey
     */
    public AirtableApi(String apiKey) {
        this(apiKey, AirtableExecutor.newInstance());
    }

    /**
     * @param apiKey   Airtable ApiKey
     * @param executor Executor to use for this api to use.
     */
    public AirtableApi(String apiKey, Executor executor) {
        this.apiKey = apiKey;
        this.executor = executor;
    }

    /**
     * Go to https://airtable.com/api to find the name of your app.
     *
     * @param app id of the app. (prefixed with 'app')
     * @return {@link AirtableApplication} interface
     * @see AirtableApi#app(String) 'base' is just a synonym for 'app'
     */
    public Application base(String app) {
        return app(app);
    }

    /**
     * Go to https://airtable.com/api to find the name of your app.
     *
     * @param app id of the app. (prefixed with 'app')
     * @return {@link AirtableApplication} interface
     */
    public Application app(String app) {
        return new Application(app);
    }

    /**
     * Implemented AirtableApplication interface.
     * <p>
     * It's named Application because Airtable named it Application.
     *
     * @see AirtableApplication inteface for all available methods.
     */
    public final class Application implements AirtableApplication {
        private final String base;

        private Application(String base) {
            this.base = base;
        }

        /**
         * @param table name of the table in Airtable
         * @return Table api object
         */
        public Table table(String table) {
            return new Table(base, table);
        }
    }

    /**
     * Implemented AirtableTable interface.
     *
     * @see AirtableTable interface for all available methods.
     */
    public final class Table implements AirtableTable {
        private final String base;
        private final String table;

        private Table(String base, String table) {
            this.base = base;
            this.table = table;
        }

        /**
         * @return name of the base the table is in.
         */
        public String baseName() {
            return base;
        }

        /**
         * @return name of the table
         */
        public String tableName() {
            return table;
        }

        @Override
        public PaginationList list(QuerySpec querySpec) {
            try {
                URI uri = new URIBuilder(querySpec.build())
                        .setScheme("https")
                        .setHost("api.airtable.com")
                        .setPathSegments("v0", base, table)
                        .build();

                Request request = Request.Get(uri)
                        .addHeader("Authorization", "Bearer " + apiKey);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return new AirtableList(node);
            } catch (URISyntaxException | IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public AirtableRecord post(AirtableRecord record, boolean typecast) {
            try {
                Request request = Request.Post(createUri())
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .bodyString(toString(record, typecast), ContentType.APPLICATION_JSON);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return new AirtableRecord(node);
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public List<AirtableRecord> patch(List<AirtableRecord> records, boolean typecast) {
            AirtableClientException.assert10Records(records);

            try {
                Request request = Request.Patch(createUri())
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .bodyString(toString(records, typecast), ContentType.APPLICATION_JSON);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return AirtableList.parse(node.path("records"));
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public List<AirtableRecord> put(List<AirtableRecord> records, boolean typecast) {
            AirtableClientException.assert10Records(records);

            try {
                Request request = Request.Put(createUri())
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .bodyString(toString(records, typecast), ContentType.APPLICATION_JSON);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return AirtableList.parse(node.path("records"));
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Nullable
        @Override
        public AirtableRecord get(String recordId) {
            try {
                Request request = Request.Get(createUri(recordId))
                        .addHeader("Authorization", "Bearer " + apiKey);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return new AirtableRecord(node);
            } catch (IOException e) {
                throw new AirtableClientException(e);
            } catch (AirtableApiException e) {
                // For Get Request, status 404 is resolved into null
                if (e.getCode() == 404) return null;
                throw e;
            }
        }

        @Override
        public List<AirtableRecord> post(List<AirtableRecord> records, boolean typecast) {
            AirtableClientException.assert10Records(records);


            try {
                Request request = Request.Post(createUri())
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .bodyString(toString(records, typecast), ContentType.APPLICATION_JSON);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return AirtableList.parse(node.path("records"));
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public AirtableRecord patch(AirtableRecord record, boolean typecast) {
            try {
                Request request = Request.Patch(createUri(record.getId()))
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .bodyString(toString(record, typecast), ContentType.APPLICATION_JSON);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return new AirtableRecord(node);
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public boolean delete(String recordId) {
            try {
                Request request = Request.Delete(createUri(recordId))
                        .addHeader("Authorization", "Bearer " + apiKey);

                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                return node.path("deleted").asBoolean();
            } catch (IOException e) {
                throw new AirtableClientException(e);
            }
        }

        @Override
        public List<String> delete(List<String> recordIds) {
            AirtableClientException.assert10Records(recordIds);

            try {
                URIBuilder uriBuilder = new URIBuilder()
                        .setScheme("https")
                        .setHost("api.airtable.com")
                        .setPathSegments("v0", base, table);

                recordIds.forEach(s -> {
                    uriBuilder.addParameter("records[]", s);
                });

                Request request = Request.Delete(uriBuilder.build())
                        .addHeader("Authorization", "Bearer " + apiKey);


                JsonNode node = executor.execute(request)
                        .handleResponse(AirtableApi.this::handleResponse);

                List<String> ids = new ArrayList<>();
                for (JsonNode jsonNode : node.path("records")) {
                    if (jsonNode.path("deleted").asBoolean()) {
                        ids.add(jsonNode.path("id").asText());
                    }
                }
                return ids;
            } catch (IOException | URISyntaxException e) {
                throw new AirtableClientException(e);
            }
        }

        /**
         * @param record to convert to json string
         * @return json string
         */
        private String toString(AirtableRecord record, boolean typecast) {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            if (typecast) {
                node.put("typecast", typecast);
            }

            ObjectNode fields = node.putObject("fields");
            record.getFields().forEach(fields::set);
            try {
                return OBJECT_MAPPER.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                throw new AirtableClientException(e);
            }
        }

        /**
         * @param records to convert into json string
         * @return json string
         */
        private String toString(List<AirtableRecord> records, boolean typecast) {
            ObjectNode node = OBJECT_MAPPER.createObjectNode();
            if (typecast) {
                node.put("typecast", typecast);
            }

            ArrayNode arrayNode = node.putArray("records");
            for (AirtableRecord record : records) {
                ObjectNode recordNode = OBJECT_MAPPER.createObjectNode();
                if (record.getId() != null) {
                    recordNode.put("id", record.getId());
                }

                ObjectNode fields = recordNode.putObject("fields");
                record.getFields().forEach(fields::set);

                arrayNode.add(recordNode);
            }

            try {
                return OBJECT_MAPPER.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                throw new AirtableClientException(e);
            }
        }

        private URI createUri() {
            try {
                return new URIBuilder()
                        .setScheme("https")
                        .setHost("api.airtable.com")
                        .setPathSegments("v0", base, table)
                        .build();
            } catch (URISyntaxException e) {
                throw new AirtableClientException(e);
            }
        }

        private URI createUri(String recordId) {
            try {
                return new URIBuilder("https://api.airtable.com")
                        .setPathSegments("v0", base, table, recordId)
                        .build();
            } catch (URISyntaxException e) {
                throw new AirtableClientException(e);
            }
        }
    }

    /**
     * @param response to handle
     * @return JsonNode
     * @throws AirtableClientException client error, not caused by airtable api
     * @throws AirtableApiException    server error, originated from https://api.airtable.com/v0
     */
    @Nullable
    private JsonNode handleResponse(HttpResponse response) throws AirtableClientException, AirtableApiException {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(response.getEntity().getContent());

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 200) return node;

            JsonNode error = node.path("error");
            if (error.isTextual()) {
                throw new AirtableApiException(status.getStatusCode(), error.asText(), error.asText());
            }

            if (error.isObject()) {
                String type = error.path("type").asText(null);
                String message = error.path("message").asText(null);
                throw new AirtableApiException(status.getStatusCode(), type, message);
            }

            throw new AirtableApiException(status.getStatusCode(), null, null);
        } catch (IOException e) {
            throw new AirtableClientException(e);
        }
    }

    /**
     * QuerySpec implementation with URI builder
     */
    public static final class QuerySpecImpl implements AirtableTable.QuerySpec {

        private URIBuilder builder;
        private Map<String, String> sort = new HashMap<>();

        QuerySpecImpl() {
            this.builder = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.airtable.com")
                    .setPathSegments("v0");
        }

        @Override
        public AirtableTable.QuerySpec offset(String offset) {
            builder.setParameter("offset", offset);
            return this;
        }

        @Override
        public AirtableTable.QuerySpec fields(List<String> fields) {
            for (int i = 0; i < fields.size(); i++) {
                builder.setParameter("fields[]", fields.get(i));
            }
            return this;
        }

        @Override
        public AirtableTable.QuerySpec filterByFormula(String formula) {
            builder.setParameter("filterByFormula", formula);
            return this;
        }

        @Override
        public AirtableTable.QuerySpec maxRecords(int size) {
            builder.setParameter("maxRecords", String.valueOf(size));
            return this;
        }

        @Override
        public AirtableTable.QuerySpec pageSize(int size) {
            builder.setParameter("pageSize", String.valueOf(size));
            return this;
        }

        @Override
        public AirtableTable.QuerySpec sort(String field, @Nullable String direction) {
            sort.put(field, StringUtils.lowerCase(direction));
            return this;
        }

        @Override
        public AirtableTable.QuerySpec view(String name) {
            builder.setParameter("view", name);
            return this;
        }

        @Override
        public AirtableTable.QuerySpec cellFormat(String format) {
            builder.setParameter("cellFormat", format);
            return this;
        }

        @Override
        public AirtableTable.QuerySpec timeZone(String zone) {
            builder.setParameter("timeZone", zone);
            return this;
        }

        @Override
        public AirtableTable.QuerySpec userLocale(String locale) {
            builder.setParameter("userLocale", locale);
            return this;
        }

        /**
         * @throws AirtableClientException uri syntax exception when building the uri
         */
        @Override
        public URI build() {
            int sortI = 0;
            for (Map.Entry<String, String> next : sort.entrySet()) {
                builder.setParameter("sort[" + sortI + "][field]", next.getKey());
                if (next.getValue() != null) {
                    builder.setParameter("sort[" + sortI + "][direction]", next.getValue());
                }
                sortI++;
            }

            try {
                return builder.build();
            } catch (URISyntaxException e) {
                throw new AirtableClientException(e);
            }
        }
    }
}
