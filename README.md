# Airtable Java API Interface

This library support all features available in https://airtable.com/api. 

**Latest Release:** 0.1 (0.1 because https://api.airtable.com/v0)<br>
**License:** Apache 2.0<br>
**JDK:** Java 8

# Features:
* Supports all fields
* Supports all features exposed in https://airtable.com/api (as of 2019/04/21)
* Build in pagination support
* Heavily documented (javadoc)
* Fluent Query Builder (inspired by AWS Java 2 conventions)
  * `AirtableFormula` fluent builder
  * `AirtableTable.QuerySpec` fluent builder
* Lightweight
  * commons-lang3
  * fluent-hc
  * jackson-databind
* Proper airtable & client exception handling
  * `AirtableApiException` (from api service: https://api.airtable.com) 
  * `AirtableClientException` (from client: most likely your mistake)
  * Status 429 Backoff 30 seconds auto try
* Customizable HTTP Client (fluent-hc)

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
    
    // {Value}=1
    query.filterByFormula(LogicalOperator.EQ, AirtableFormula.Object.field("Value"), value(1));
    
    // 1+2
    query.filterByFormula(NumericOperator.ADD, AirtableFormula.Object.value(1), AirtableFormula.Object.value(2))
    
    // {f1}=(AND(1,{f2}))
    query.filterByFormula(LogicalOperator.EQ, field("f1"), parentheses(LogicalFunction.AND, value(1), field("f2")));
});

```
#### Getting a existing record
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

#### Updating a existing record
```java
AirtableRecord record = new AirtableRecord();
record.setId("rec...");
record.putField("Name", "Patched");

record = table.patch(record);
```

#### Deleting a existing record
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

# Download
Hosted in Maven Central.
### Maven
```xml
<dependency>
  <groupId>dev.fuxing</groupId>
  <artifactId>airtable-api</artifactId>
  <version>0.1</version>
</dependency>
```
### Gradle
```
compile group: 'dev.fuxing', name: 'airtable-api', version: '0.1'
```
