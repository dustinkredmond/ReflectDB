# ReflectDB

[![CircleCI](https://circleci.com/gh/dustinkredmond/ReflectDB/tree/master.svg?style=svg)](https://circleci.com/gh/dustinkredmond/ReflectDB/tree/master)

Lightweight tool to provide ORM and facilitate quick table creation using straightforward annotations.

ReflectDB is a simple library that mimics some of the nifty functionalities of larger, well-known frameworks without all of the bulk and mountain of dependencies. ***We don't have any dependencies*** BYODBD (**B**ring **y**our **o**wn **d**ata**b**ase **d**river)

Reflect DB includes features like:
1. Object Relational Mapping (ORM)
2. Query builder tools
3. Automatic table creation (via Java Annotations)
4. Simple (pure Java) configuration
5. Generics e.g. `ReflectDB.fetchSingle("SELECT * FROM PEOPLE WHERE ID = 1", Person.class)` returns an instance of `Person`
6. Convenience queries `ReflectDB.fetchAll(MyDomainObject.class);` returns an entire table of objects.

To see examples of how to use ReflectDB, you can check out some of our examples in the `ReflectDBTest` class, which is where all of our unit tests are written. The beauty of ReflectDB is that nearly 100% of the API can be accessed by static or instance methods of the `org.gserve.reflectdb.ReflectDB` class.

If the `ReflectDB` class doesn't provide a way for you to accomplish your goal, please open a new issue on GitHub, and we'll take a look at implementing this. Although, keep in mind that ReflectDB is meant to be lightweight; it is not intended to be a replacement for enterprise ORM frameworks that are already available.

---
# Getting Started

## 1. Annotate POJO classes to model database tables.

```java
import org.gserve.reflectdb.annotations.*;

@ReflectDBTable(tableName = "PEOPLE")
public class Person {

  @ReflectDBField(fieldName = "ID", fieldType = "INTEGER(11)", notNull = true, primaryKey = true)
  private int id;
  @ReflectDBField(fieldName = "FIRST_NAME", notNull = true)
  private String firstName;
  @ReflectDBField(fieldName = "LAST_NAME", notNull = true)
  private String lastName;
  @ReflectDBField(fieldName = "ALTERNATE_NAME")
  private String altName;
  
  // You MUST have a constructor that takes no arguments, e.g.
  public Person() { super(); }
  
  // getters & setters below
}
```

## 2. Create and configure an instance of `ReflectDB`

```java
import org.gserve.reflectdb.ReflectDB;

public class TestReflectDB {
  private static final ReflectDBConfig CONFIG = new ReflectDBConfig(
          "jdbc:sqlite:MY_DATABASE.db",     // DB connection string
          "username",                       // DB username (if applicable)
          "pass",                           // DB password (if applicable)
          3306);                            // DB port (if applicable) 
  
  public static void main(String[] args) {
      ReflectDB reflectDB = ReflectDB.initialize(CONFIG);                   // Initialize ReflectDB with its config
      reflectDB.addModelClass(MyTableBean.class);                           // Add model classes that ReflectDB should use
      reflectDB.addModelClasses(MyOtherBean.class, YetAnotherBean.class);   // Add multiple
  }  
}
```

## 3. Almost all functionality is available from `ReflectDB` instance

```java
public void myFunction(ReflectDB reflectDB) {
  reflectDB.createTablesIfNotExists();    // Creates database tables based off of your model classes
  reflectDB.insert(new Person(1, "John", "Smith", "")); // Insert a row into database
  Person p = reflectDB.fetchSingle("SELECT * FROM PEOPLE WHERE ID = 1", Person.class); // Query object from database
  System.out.println( p.getFirstName() );   // Prints queried Person's first name, "John"
  
  p.setFirstName("Dustin");
  reflectDB.save(p);
  // Updates changed fields in database
  
  // Fetch all Person from database and delete
  reflectDB.fetchAll(Person.class).forEach(reflectDB::delete);
    
  // Execute a native SQL query
  ResultSet rs = reflectDB.getNativeConnection().prepareStatement("SELECT * FROM PERSON WHERE ...").executeQuery();
    
}
```

### 4. Recap
1. Instantiate `ReflectDB`
2. Call `ReflectDB.initialize(config)`
3. Call `ReflectDB.addModelClass()` to add tables.
4. Call `ReflectDB.createTablesIfNotExists();`
5. Happy developing! Call `ReflectDB` methods to make life simpler.
---

### Note

ReflectDB was developed for use with MySQL or MariaDB databases. It has been extensively tested with both. While it 
is possible that another type of database will work with ReflectDB, you should extensively test this yourself before considering
the use of ReflectDB in a mission-critical project or a production environment. That being said, we try to use ANSI-compliant SQL, so feel free to give it a go in your 
development environment. It probably won't melt your database, but the risk is yours to take.

---

### Additional note

This is not Hibernate or similar ORM framework. It's simply for easily creating tables and running simple queries. `ReflectDB` is better suited for test/development environments than production usage. More comprehensive ORM frameworks should be considered for this use case.

---

### [Release Notes](./RELEASE.md)

