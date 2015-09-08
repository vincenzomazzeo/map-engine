Map Engine
==========

The Map Engine is an engine that, at runtime, maps XML documents to domain objects.

I've designed and developed this Engine for [Alidays S.p.A.](http://www.alidays.it) which has the need to handle many XML documents from service providers. Writing the code to transform these XML documents to domain objects is a really bored and error prone task so the idea of the Engine has came to surface.

The idea is to use the SQL language to query the XML document and create domain objects. In order to do this the Engine uses an in-memory relational database as intermediate tier between XML and domain objects.

A XML map document, created by the developer at design time, is used to instruct the Engine on how to parse the XML document and save the data into the database and how to read them from the database in a more comfortable way.

Jump to the [usage](#usage) section to see an example!

## Technologies
The Engine is written in Java and uses [H2](http://www.h2database.com/html/main.html) as in-memory RDBMS.

## Build

You'll need a machine with minimum Java 6 and Apache Maven 3 installed.

Checkout:

	git clone https://github.com/vincenzomazzeo/map-engine.git

Run Build:

    mvn clean install

## Usage
The Engine needs a directive XML document to work. 
This document is used both at design time to create the classes to map the database tables (see [Code Generation](#CodeGeneration) for more details) and at runtime to instruct the Engine on how to read the XML data document and what queries has to execute.
A directive XML document has to be written for each XML data document to map.

### Clothes Catalog Example
In this example is explained how to transform a XML data document containing data about clothes into an object model.

#### Catalog XML Data Document
Following is the XML data document:
```xml
<?xml version="1.0"?>
<catalog>
   <product description="Cardigan Sweater" product_image="cardigan.jpg">
      <catalog_item gender="Men's">
         <item_number>QWZ5671</item_number>
         <price>39.95</price>
         <size description="Medium">
            <color_swatch image="red_cardigan.jpg">Red</color_swatch>
            <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
         </size>
         <size description="Large">
            <color_swatch image="red_cardigan.jpg">Red</color_swatch>
            <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
         </size>
      </catalog_item>
      <catalog_item gender="Women's">
         <item_number>RRX9856</item_number>
         <price>42.50</price>
         <size description="Small">
            <color_swatch image="red_cardigan.jpg">Red</color_swatch>
            <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
            <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
         </size>
         <size description="Medium">
            <color_swatch image="red_cardigan.jpg">Red</color_swatch>
            <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
            <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
            <color_swatch image="black_cardigan.jpg">Black</color_swatch>
         </size>
         <size description="Large">
            <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
            <color_swatch image="black_cardigan.jpg">Black</color_swatch>
         </size>
         <size description="Extra Large">
            <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
            <color_swatch image="black_cardigan.jpg">Black</color_swatch>
         </size>
      </catalog_item>
   </product>
</catalog>
```

#### Catalog Domain Object
There is only one domain object which has the following structure:
```java
public class Catalog {

	private String description;
	private String image;
	private String gender;
	private String itemNumber;
	private BigDecimal price;
	private String itemSizeDescription;
	private String itemSizeColorImage;
	private String itemSizeColorColor;

	[...]
}
```

#### Catalog Directive XML Document
A directive XML document has to be written in order to instruct the Engine on how to accomplish the transformation.
Following is the directive XML document:
```xml
<?xml version='1.0' encoding='utf-8'?>
<engine-directives debug="false">

    <fetch base-path="catalog">
    	<entity name="product">
    		<foreach value="product">
    			<bind attribute="product_id" type="int">#position()</bind>
    			<bind attribute="description" type="varchar" length="50">@description</bind>
    			<bind attribute="product_image" type="varchar" length="50">@product_image</bind>
    		</foreach>
    	</entity>
    	
    	<entity name="item">
    		<foreach value="product/catalog_item">
    			<bind attribute="product_id" type="int">#position(..)</bind>
    			<bind attribute="item_id" type="int">#position()</bind>
    			<bind attribute="gender" type="varchar" length="7">@gender</bind>
    			<bind attribute="item_number" type="varchar" length="7">item_number</bind>
    			<bind attribute="price" type="decimal" length="4" decimal="2">price</bind>
    		</foreach>
    	</entity>
    	
    	<entity name="item_size">
    		<foreach value="product/catalog_item/size">
    			<bind attribute="product_id" type="int">#position(../..)</bind>
    			<bind attribute="item_id" type="int">#position(..)</bind>
    			<bind attribute="item_size_id" type="int">#position()</bind>
    			<bind attribute="description" type="varchar" length="11">@description</bind>
    		</foreach>
    	</entity>
    	
    	<entity name="item_size_color">
    		<foreach value="product/catalog_item/size/color_swatch">
    			<bind attribute="product_id" type="int">#position(../../..)</bind>
    			<bind attribute="item_id" type="int">#position(../..)</bind>
    			<bind attribute="item_size_id" type="int">#position(..)</bind>
    			<bind attribute="item_size_color_id" type="int">#position()</bind>
    			<bind attribute="image" type="varchar" length="50">@image</bind>
    			<bind attribute="color" type="varchar" length="10">#self()</bind>
    		</foreach>
    	</entity>
    </fetch>

    <map map-package="it.alidays.mapengine.test.bm.generated" aggregator-factory="it.alidays.mapengine.test.CatalogAggregatorFactory">
        <retrieve id="Catalog">
			<![CDATA[
				select
					product.description,
					product.product_image,
					item.gender,
					item.item_number,
					item.price,
					item_size.description as item_size_description,
					item_size_color.image,
					item_size_color.color
				
				from
					product
					inner join
					item on	(		
								product.vuid = item.vuid
							and	product.product_id = item.product_id
							)
					inner join
					item_size on	(
										product.vuid = item_size.vuid
									and product.product_id = item_size.product_id
									and item.item_id = item_size.item_id
									)
					inner join
					item_size_color on	(
											product.vuid = item_size_color.vuid
										and product.product_id = item_size_color.product_id
										and item.item_id = item_size_color.item_id
										and item_size.item_size_id = item_size_color.item_size_id
										)
				
				where
					product.vuid = #vuid#
				
				order by
					item.item_number
					
			]]>
        </retrieve>
    </map>

</engine-directives>
```

#### How It Works
Let's take a closer look to it.

The directive XML document starts with the tag `engine-directives` which has the boolean attribute `debug` used to enable/disable the debug mode. The debug mode starts the H2 database in server mode in order to inspect the tables.

The `engine-directives` tag contains two children, [`fetch`](#Fetch) and [`map`](#Map).

##### Fetch
The `fetch` section is used to instruct the Engine on how to parse the XML data document defining implicitly the tables of the schema. First of all is set the XML starting node using the `base-path` attribute. In this example the `base-path` is `catalog` which is the root of the XML data document. 
```xml
<fetch base-path="catalog">[...]</fetch>
```
At this point is defined a set of entities (`entity`) that represents the database tables. The value of the attribute `name` will be the name of the table. In this example will be created four tables: catalog, item, item_size, item_size_color.
```xml
<entity name="product">[...]</entity>
<entity name="item">[...]</entity>
<entity name="item_size">[...]</entity>
<entity name="item_size_color">[...]</entity>
```
The `foreach` tag defines how to fill the table. A new row will be inserted in the table for each occurrence of the path contained in the `value` attribute added to the `base-path` value. Therefore, in this example, for each occurrence of `catalog/product/catalog_item` the Engine will insert a new record in the table `item`. 
```xml
<foreach value="product/catalog_item">[...]</foreach>
```
The table's columns are defined by the `bind` tag which specifies, moreover, from where to read the value.  The four attributes `attribute`, `type`, `length` and `decimal` define, respectively, the column name, type, length and, if the type is a floating point number, the length of the decimal part. The value of the `bind` tag specifies where to read the value to insert into the cell.  It can be a path or a [function](#FetchFunctions). In the example the table `item` has five columns
```xml
<bind attribute="product_id" type="int">#position(..)</bind>
<bind attribute="item_id" type="int">#position()</bind>
<bind attribute="gender" type="varchar" length="7">@gender</bind>
<bind attribute="item_number" type="varchar" length="7">item_number</bind>
<bind attribute="price" type="decimal" length="4" decimal="2">price</bind>
```
The column `product_id` is of type integer and will be filled with the value of the function [`position`](#Position) that returns the position of the current `catalog_item` tag.
The column `item_id` is of type integer and will be filled with the value of the function [`position`](#Position) that returns the position of the current `product` tag.
The column `gender` is of type varchar with length 7 and will be filled with the value of the `gender` attribute of the current `catalog_item` tag.
The column `item_number` is of type varchar with length 7 and will be filled with the value of the `item_number` tag of the current `catalog_item` tag.
The column `price` is of type decimal with length 4 and 2 decimals and will be filled with the value of the `price` tag of the current `catalog_item` tag.

In this example, after the parsing, the `item` table will containt the following data:

| product_id | item_id | gender  | item_number | price |
| ---------: | ------: | :-----  | ----------: | ----: |
| 1          | 1       | Men's   | QWZ5671     | 39.95 |
| 1          | 2       | Women's | RRX9856     | 42.50 |

##### Map
The `map` section is used to instruct the Engine on how to read the data from the database. It's defined by the `map` tag which has two attributes, `map-package` that specifies the package where to find the classes needed to [map the query results](#MapQueryResult) and the `aggregator-factory` that specifies the _Factory_ to call to create a new [_Aggregator_](#Aggregator). 
```xml
<map map-package="it.alidays.mapengine.test.bm.generated" aggregator-factory="it.alidays.mapengine.test.CatalogAggregatorFactory">[...]</map>
```
Inside the `map` tag there is a set of `retrieve` tags. Each `retrieve` tag specifies the query to be executed against the database. The `id` attribute specifies the prefix of the classes needed to [map the query results](#MapQueryResult).
```xml
<retrieve id="Catalog">[...]</retrieve>
```
>**Note:** Each table has an additional column called `vuid` used to store the UUID of a single mapping task: an instance of the Engine can handle several mapping tasks at the same time, so it's important to know which task each table record belongs to. In order to do this when the Engine starts a new map task, sets an UUID to it and each record that belongs to the task has the UUID stored in the `vuid` column. It's important to use the keyword `#vuid#` in the `where` statement of the queries to retrieve the correct data and if the query contains one or more joins is important to join even the `vuid` column as showed in the example:
> ```sql
[...]
from
  product
  inner join
  item on (product.vuid = item.vuid and [...])
  inner join
  item_size on (product.vuid = item_size.vuid and [...])
  inner join
  item_size_color on (product.vuid = item_size_color.vuid and [...])
where
  product.vuid = #vuid#
[...]
> ```

###### Map Query Result
The Engine needs some classes in order to map query results to objects. More precisely two classes are needed for each `retrieve` tag in the map section. The first one is a POJO that maps a single row of the query result and the second one is a descendant of `AbstractRetrieve<T>` where `T` is the POJO class, that overrides the method
```java
public abstract T getMap(Map<String, Object> data);
```
that returns an instance of `T` filled with the data passed as argument.
These pair classes must be placed in the package specified by the `map-package` attribute of the `map` tag and they have to be called using the prefix specified by the attribute `id` of the `retrieve` tag followed by `Map` for the POJO class and `Retrieve` for the retriever class (see [Map](#Map) for more details).
In the example there is only one `retrieve` tag whose `id` attribute is `Catalog` therefore the POJO class is called `CatalogMap` and the retriever class is called `CatalogRetrieve`.
```java
public class CatalogMap {

    private final String description;
    private final String productImage;
    private final String gender;
    private final String itemNumber;
    private final BigDecimal price;
    private final String itemSizeDescription;
    private final String image;
    private final String color;

    protected CatalogMap(Map<String, Object> data) {
        this.description = ((String) data.get("Description"));
        this.productImage = ((String) data.get("ProductImage"));
        this.gender = ((String) data.get("Gender"));
        this.itemNumber = ((String) data.get("ItemNumber"));
        this.price = ((BigDecimal) data.get("Price"));
        this.itemSizeDescription = ((String) data.get("ItemSizeDescription"));
        this.image = ((String) data.get("Image"));
        this.color = ((String) data.get("Color"));
    }

	[...]

}
```
```java
public class CatalogRetrieve extends AbstractRetrieve<CatalogMap> {

    public CatalogRetrieve(String id) {
        super(id);
    }

    @Override
    public CatalogMap getMap(Map<String, Object> data) {
        return new CatalogMap(data);
    }

}
```

###### Aggregator
The `Aggregator` is the interface to be implemented by the class that will pack the result of the mapping.
Two methods are exposed
```java
public void notifyRetrieveResult(String id, List<Object> data) throws AggregatorException;
	
public Object getMapResult() throws AggregatorException;
```
A mapping task could involve multiple queries (`retrieve` tag) in order to transform a XML data document. The queries' results have to be organized to produce a single result. The `Aggregator` is responsible for carrying out this task. After each query, the Engine calls the `notifyRetrieveResult` method: the `id` argument identifies the query and the `data` argument is the list of mapping objects (`CatalogMap` in the example). When all the queries are executed the engine calls the `getMapResult` method: the returned object is the mapping task result.
The Engine, to instantiate the `Aggregator`, uses a factory defined by the interface `AggregatorFactory` which exposes only one method
```java
public Aggregator make();
```
The class that implements the `AggregatorFactory` must be specified by the `aggregator-factory` attribute of the `retrieve` tag  (see [Map](#Map) for more details).
```java
public class CatalogAggregatorFactory implements AggregatorFactory {

	@Override
	public Aggregator make() {
		return new CatalogAggregator();
	}

}
```
```java
public class CatalogAggregator implements Aggregator {

	private final List<Catalog> result;
	
	public CatalogAggregator() {
		this.result = new ArrayList<>();
	}
	
	@Override
	public void notifyRetrieveResult(String id, List<Object> data) throws AggregatorException {
		switch (MapEngineRetrieveId.valueOf(id)) {
		case Catalog:
			manageCatalog((List<CatalogMap>)(List<?>)data);
			break;
		}
	}

	@Override
	public Object getMapResult() throws AggregatorException {
		return this.result;
	}
	
	private void manageCatalog(List<CatalogMap> items) {
		for (CatalogMap item : items) {
			Catalog catalog = new Catalog();
			this.result.add(catalog);
			catalog.setDescription(item.getDescription());
			catalog.setImage(item.getProductImage());
			catalog.setGender(item.getGender());
			catalog.setItemNumber(item.getItemNumber());
			catalog.setPrice(item.getPrice());
			catalog.setItemSizeDescription(item.getItemSizeDescription());
			catalog.setItemSizeColorImage(item.getImage());
			catalog.setItemSizeColorColor(item.getColor());
		}
	}

}
```

##### Code Generation
To avoid the boilerplate of writing the pair mapping classes (see [Map Query Result](#MapQueryResult) for details) the Engine is capable to generate them for us.
The Java class to launch is `it.alidays.mapengine.codegenerator.MapperEngineCodeGenerator`. It accepts two options, the `-d` option that specifies the absolute path to the project sources and the `-s` option that specifies the directive XML document.
	
	it.alidays.mapengine.codegenerator.MapperEngineCodeGenerator -d<absolutePathToProjectSources> -s<directiveXmlDocument>

In this example, after the code generation, the `it.alidays.mapengine.test.bm.generated` package will contain the classes `CatalogMap`, `CatalogRetrieve` and `MapEngineRetrieveId`.

![Project Tree](/images/project_tree.png)

The package is specified by the `map-package` attribute of the `map` tag
```xml
<map map-package="it.alidays.mapengine.test.bm.generated" aggregator-factory="it.alidays.mapengine.test.CatalogAggregatorFactory">[...]</map>
```
while the classes' names are defined using the `id` attribute of the `entity` tag (see [Map](#Map) and [Map Query Result](#MapQueryResult) for more details).

The `MapEngineRetrieveId` is an enum class containing the values of the `id` attribute of the `entity` tags. This enum could be used by the [`Aggregator`](#Aggregator) to determine the query result passed to the  `notifyRetrieveResult` method.

##### Engine In Action
Once the directive XML document is ready the Engine could be started.
```java
Engine engine = new Engine(Engine.class.getClassLoader().getResourceAsStream("create_catalog_directives.xml"));
ByteArrayInputStream xml = getXml();
List<Catalog> result = (List<Catalog>)engine.run(xml);
for (Catalog catalog : result) {
	System.out.println(catalog);
}
engine.shutdown();
```

>**Note:** The instance of the Engine could - and should because of performance matters (See [Performance](#Performance) for more details) - be reutilized and is thread-safe. The `shutdown` method should be called only at the end.

Here is the output
![Run output](/images/output.png)

##### Configuration
The Engine is configured by a XML configuration file.

The configuration specifies the H2 urls to use in the standard mode and in the debug mode
```xml
<persistence>
	<production url="jdbc:h2:mem:mapengine;DB_CLOSE_DELAY=-1" user="sa" password="sa" />
	<debug url="jdbc:h2:~/mapengine_test" user="sa" password="sa" />
</persistence>
```
defines the converters used by the Engine to prepare the SQL statements
```xml
<database-type-converters>
	<database-type-converter type="int" class="it.alidays.mapengine.core.schema.converter.IntTypeConverter" />
	<database-type-converter type="varchar" class="it.alidays.mapengine.core.schema.converter.VarcharTypeConverter" />
	<database-type-converter type="decimal" class="it.alidays.mapengine.core.schema.converter.DecimalTypeConverter" />
</database-type-converters>
```
and the [functions](#FetchFunctions) that could be used during the [fetch](#Fetch) step
```xml
<fetch-functions>
	<fetch-function name="position" class="it.alidays.mapengine.core.fetch.function.PositionFunction" />
	<fetch-function name="self" class="it.alidays.mapengine.core.fetch.function.SelfFunction" />
</fetch-functions>
```
The `name` attribute of the `fetch-function` is the name to be used in the `bind` tag of the `map` tag of the directive XML document (see [Fetch](#Fetch) for more details) to use the function.

#### Fetch Functions
During the [fetch](#Fetch) step the Engine navigates the XML data document to extrapolate the data to store into the database. In almost all the cases XPath is used to do this but sometimes special functions are necessary. In these cases it's possible to write a function extending the abstract class `Function` which has only one abstract method
```java
public abstract Object evaluate(Element node)
```
that returns the object derived by the node passed as argument according to the function.
To install a new function a `fetch-function` tag has to be added to the [configuration](#Configuration).

Two functions are already defined, [Position](#Position) and [Self](#Self).

##### Position
The `position` function returns the index of the node inside the parent.
For example, having the following XML data document
```xml
<a>
	<b>red</b>
	<b>yellow</b>
	<b>blue</b>
	<b>cyan</b>
</a>
```
and the following directive XML document
```xml
<fetch base-path="a">
	<entity name="b">
		<foreach value="b">
			<bind attribute="b_id" type="int">#position()</bind>
		</foreach>
	</entity>
</fetch>
```
the position function returns `1` when evaluates the `b` tag containg the value `red`, `2` when evaluates the `b` tag containg the value `yellow`, `3` when evaluates the `b` tag containg the value `blue` and so on.
The function permits to navigate towards parents using the `..` notation.
For example, having the following XML data document
```xml
<a>
	<b>red</b>
</a>
```
and the following directive XML document
```xml
<fetch base-path="a">
	<entity name="b">
		<foreach value="b">
			<bind attribute="a_id" type="int">#position(..)</bind>
			<bind attribute="b_id" type="int">#position()</bind>
		</foreach>
	</entity>
</fetch>
```
the position function returns `1` when evaluates the first `bind` and `1` when evaluates the second `bind`.

##### Self
The `self` function returns the value of the tag.
For example, having the following XML data document
```xml
<a>
	<b ba1="ba1" ba2="ba2">b-value</b>
</a>
```
and the following directive XML document
```xml
<fetch base-path="a">
	<entity name="b">
		<foreach value="b">
			<bind attribute="b_value" type="varchar" length="10">#self()</bind>
		</foreach>
	</entity>
</fetch>
```
the self function returns `b-value`.

## Performance
The Engine uses XPath to navigate the XML DOM so most of the performance are related to XPath engine implementation. Actually the Engine uses Jaxen implementation version 1.1.6.

The Engine uses the Java concurrency during the mapping to improve the performance. Actually the multitasking is not configurable and the `Runtime.getRuntime().availableProcessors()` method is used to determine the executors' thread pools.
Three thread pools are started, one for the Engine, one for the Fetcher and one for the Mapper.

These are the result in milliseconds of 10 consecutive mapping tasks on my notebook (Intel i7-4600U @ 2.1GHz and 8GB RAM with Windows 7). The XML data document used is 485.504 bytes length and the directive XML document has 15 `entity` (see [Fetch](#Fetch) for more details) tags and 11 `retrieve` tags (see [Map](#Map) for more details).

| Run | Fetch | DB Insert | Map | DB Clean | Total |
| --: | ----: | --------: | --: | -------: | ----: |
| 1   | 1749  | 451       | 950 | 18       | 3169  |
| 2   | 484   | 69        | 212 | 25       | 790   |
| 3   | 378   | 143       | 200 | 5        | 726   |
| 4   | 357   | 51        | 127 | 5        | 540   |
| 5   | 408   | 104       | 134 | 6        | 653   |
| 6   | 447   | 40        | 88  | 2        | 577   |
| 7   | 300   | 23        | 79  | 4        | 407   |
| 8   | 347   | 16        | 40  | 3        | 407   |
| 9   | 288   | 13        | 59  | 2        | 363   |
| 10  | 339   | 24        | 47  | 2        | 412   |

This table shows that the Engine has a warmup time so it's important to not shutdown it in consecutive mapping tasks.

## License
This engine was developed for [Alidays S.p.A.](http://www.alidays.it) that has decided to release it as an open source project and is distributed under the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
