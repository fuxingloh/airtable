# Airtable Java API Interface

This library support all features available in https://airtable.com/api. 

**Latest Release:** 0.3.0<br>
**License:** Apache 2.0<br>
**JDK:** Java 8<br>
**Code Coverage:** 71% 

# Features:
* Supports the new batch API 
* Supports all fields
* Supports all features exposed in https://airtable.com/api (as of 2020/01/20)
* Build in pagination support
* Heavily documented (javadoc)
* Fluent Query Builder for type safe query building
  * `AirtableFormula` fluent builder
  * `AirtableTable.QuerySpec` fluent builder
* Lightweight
  * `commons-lang3` mainly for FastDateFormat, java 8 version is non thread-safe
  * `fluent-hc` to perform REST call
  * `jackson-databind` for handling JSON data
* Proper airtable & client exception handling
  * `AirtableApiException` (from api service: https://api.airtable.com) 
  * `AirtableClientException` (from client: most likely your mistake)
  * Status 429 Backoff 30 seconds auto try
* Customizable HTTP Client (fluent-hc)
* Custom Module: Cache using Guava
* Custom Module: Data Mirroring (e.g. ETL, Lake, MR)

# Download
Hosted in Maven Central.
### Maven
```xml
<dependency>
  <groupId>dev.fuxing</groupId>
  <artifactId>airtable-api</artifactId>
  <version>0.3.0</version>
</dependency>
```
### Gradle
```groovy
compile group: 'dev.fuxing', name: 'airtable-api', version: '0.3.0'
```

# Example
#### Getting the AirtableTable interface.
```java
AirtableApi api = new AirtableApi("key...");
AirtableTable table = api.base("app...").table("Table Name");
```

#### List
Querying and getting a list.

```java
// List with offset support
AirtableTable.PaginationList list = table.list(querySpec -> {
    querySpec.view("View Name");
    querySpec.filterByFormula(LogicalOperator.EQ, field("Value"), value(1));
});

// For next pagination
list.getOffset();
```

#### Iterator
Iterator with automated build in support for pagination.
```java
table.iterator().forEachRemaining(record -> {
    List<AttachmentField> images = record.getFieldAttachmentList("Images");
    String name = record.getFieldString("Name");
});
```

#### Query Spec Builder
All list querystring is supported with functional fluent formula builder.

```java
List<AirtableRecord> list = table.list(query -> {
    // Localisation
    query.cellFormat("string")
    query.timeZone("Asia/Singapore");
    query.userLocale("af");

    // Data filtering
    query.view("View Name");
    query.fields("a", "b");
    
    // Sorting
    query.sort("field-name");
    query.sort("field-name", "desc");
    
    // Pagingation
    query.pageSize(50);
    query.maxRecords(1000);
    query.offset("rec...");
    
    // Vanilla String Formula: NOT({F} = '')
    query.filterByFormula("NOT({F} = '')");
    
    // Compile time Typesafe Query Formula
    // {Value}=1
    query.filterByFormula(LogicalOperator.EQ, AirtableFormula.Object.field("Value"), value(1));
    
    // 1+2
    query.filterByFormula(NumericOperator.ADD, AirtableFormula.Object.value(1), AirtableFormula.Object.value(2))
    
    // {f1}=(AND(1,{f2}))
    query.filterByFormula(LogicalOperator.EQ, field("f1"), parentheses(LogicalFunction.AND, value(1), field("f2")));
});

```
#### Getting an existing record
```java
AirtableRecord record = table.get("rec...");
```

#### Creating a new record
```java
AirtableRecord record = new AirtableRecord();
record.putField("Name", "Posted");

// Attachment support
CollaboratorField collaborator = new CollaboratorField();
collaborator.setEmail("me@email.com");
record.putField("Collaborator", collaborator);

AttachmentField attachment = new AttachmentField();
attachment.setUrl("https://upload.wikimedia.org/wikipedia/commons/5/56/Wiki_Eagle_Public_Domain.png");
record.putFieldAttachments("Attachments", Collections.singletonList(field));

record = table.post(record);
```

#### Patching an existing record
```java
AirtableRecord record = new AirtableRecord();
record.setId("rec...");
record.putField("Name", "Patched");

record = table.patch(record);
```

#### Replacing an existing record
```java
AirtableRecord record = new AirtableRecord();
record.setId("rec...");
record.putField("Name", "Replaced Entirely");

record = table.put(record);
```

#### Deleting an existing record
```java
table.delete("rec...");
```

### 429 Auto Retry
Auto retry is enabled by default. To disable it, you can create an `Executor` without retry.
```java
Executor executor = AirtableExecutor.newInstance(false);
AirtableApi api = new AirtableApi("key...", executor);
AirtableTable table = api.base("app...").table("Table Name");
```
# Cache Module
> Use Airtable as your main database with heavy caching strategy. 

For many read heavy applicaiton, status 429; too many request can be problematic when developing for speed. Cache is a read-only interface that will ignore ignorable `AirtableApiException` (429, 500, 502, 503).  

#### Creating an airtable cache.
AirtableCache uses a different HTTPClient with more concurent connection pool. RetryStrategy is also ignored.
```java
AirtableCache cache = AirtableCache.create(builder -> builder
        .apiKey(System.getenv("AIRTABLE_API_KEY"))
        .app("app3h0gjxLX3Jomw8")
        .table("Test Table")
        // Optional cache control
        .withGet(maxRecords, cacheDuration, cacheTimeUnit)
        .withQuery(maxRecords, cacheDuration, cacheTimeUnit)
);
```

#### Get record by id
Get will always attempt to get the latest record from airtable server.<br>
Fallback read from cache will only happen if any of the ignorable exception is thrown.
```java
AirtableRecord record = cache.get("rec0W9eGVAFSy9Chb");
```

#### Get records results by query spec
Query will always attempt to get the latest result from airtable server.<br>
Fallback read from cache will only happen if any of the ignorable exception is thrown.<br>
The cache key used will be the querystring.
```java
List<AirtableRecord> results = cache.query(querySpec -> {
    querySpec.filterByFormula(LogicalOperator.EQ, field("Name"), value("Name 1"));
});
```

#### Gradle Dependencies
```groovy
compile group: 'dev.fuxing', name: 'airtable-api', version: '0.3.0'
compile group: 'dev.fuxing', name: 'airtable-cache', version: '0.3.0'
```
# Mirror Module
> Use Airtable as your stateless database view for EDA.

For many applications:
* You need a quick and dirty way to look at data from your database. 
* Your product manager don't know how to use SQL. 
* You don't want to manually create scripts and generate them. 
* You like Airtable and how simple it is. 
* You want to use Airtable blocks to generate Analytic.
* You want visibility of internal data.
* You are irritated from the requests your pm requires. (sort by?, you want me to filter WHAT?, why can't you learn how to use group by!)

#### Implementation
```java
// Example Database
Database database = new Database();

AirtableMirror mirror = new AirtableMirror(table, field("PrimaryKey in Airtable")) {
    @Override
    protected Iterator<AirtableRecord> iterator() {
        // Provide an iterator of all your records to mirror over to Airtable.
        Iterator<Database.Data> iterator = database.iterator();

        return new Iterator<AirtableRecord>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public AirtableRecord next() {
                Database.Data data = iterator.next();

                // Map into AirtableRecord
                AirtableRecord record = new AirtableRecord();
                record.putField("Name", data.name);
                record.putField("Checkbox", data.checkbox);
                ...
                return record;
            }
        };
    }

    // Whether a record from your (the iterator) is still same from airtable.
    protected boolean same(AirtableRecord fromIterator, AirtableRecord fromAirtable) {
        // You might want to add a timestamp to simplify checking
        String left = fromIterator.getFieldString("Name");
        String right = fromAirtable.getFieldString("Name");
        return Objects.equals(left, right);
    }

    // Whether a row in airtable still exists in your database
    protected boolean has(String fieldValue) {
        return database.get(fieldValue) != null;
    }
};
// Async run it every 6 hours.
ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
ses.scheduleAtFixedRate(mirror, 0, 6, TimeUnit.HOURS);
```

#### Gradle Dependencies
```groovy
compile group: 'dev.fuxing', name: 'airtable-api', version: '0.3.0'
compile group: 'dev.fuxing', name: 'airtable-mirror', version: '0.3.0'
```
