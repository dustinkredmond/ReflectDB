# ReflectDB
Lightweight tool to provide ORM and facilitate quick table creation using straightforward annotations.

ReflectDB is a simple library that mimics some of the nifty functionalities of larger, well-known frameworks without all of the bulk and mountain of dependencies. We have only a single dependency (https://github.com/ronmamo/reflections/), and only for one method call at that!

Reflect DB includes features like:
1. Object Relational Mapping (ORM)
2. Query builder tools
3. Automatic table creation (via Java Annotations)
4. Simple (pure Java) configuration
5. Generics e.g. `ReflectDB.fetchSingle("SELECT * FROM PEOPLE WHERE ID = 1", Person.class)` returns an instance of `Person`
6. Convenience queries `ReflectDB.fetchAll(MyDomainObject.class);` returns an entire table of objects.

To see examples of how to use ReflectDB, you can check out some of our examples in the `ReflectDBTest` class, which is where all of our unit tests are written. The beauty of ReflectDB is that nearly 100% of the API can be accessed by static or instance methods of the `org.gserve.ReflectDB` class.

If the `ReflectDB` class doesn't provide a way for you to accomplish your goal, please open a new issue on GitHub, and we'll take a look at implementing this. Although, keep in mind that ReflectDB is meant to be lightweight; it is not intended to be a replacement for enterprise ORM frameworks that are already available.

---
# Getting Started

## 1. Annotate POJO classes to model database tables.

```
import org.gserve.annotations.*;

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
  
  // getters & setters below

}
```

## 2. Create and configure an instance of `ReflectDB`

```
import org.gserve.ReflectDB;

public class TestReflectDB {
  private static final ReflectDBConfig CONFIG = new ReflectDBConfig(
          "jdbc:sqlite:MY_DATABASE.db",     // DB connection string
          "username",                       // DB username (if applicable)
          "pass",                           // DB password (if applicable)
          3306,                             // DB port (if applicable)
          "com.example.model");             // Package containing your POJO classes 
  
  public static void main(String[] args) {
      ReflectDB reflectDB = ReflectDB.initialize(CONFIG);
  }  
}
```

## 3. Almost all functionality is available from `ReflectDB` instance

```
public void myFunction(ReflectDB reflectDB) {
  reflectDB.createTablesIfNotExists();    // Creates database tables based off of your model classes
  reflectDB.insert(new Person(1, "John", "Smith", "")); // Insert a row into database
  Person p = reflectDB.fetchSingle("SELECT * FROM PEOPLE WHERE ID = 1", Person.class); // Query object from database
  System.out.println( p.getFirstName() );   // Prints queried Person's first name, "John"
  
  p.setFirstName("Dustin");
  reflectDB.save(p);
  // Updates changed fields in database
  
  // Fetch all Person from database and delete
  reflectDB.fetchAll(Person.class).forEach(db::delete);
  
  
  // Execute a native SQL query
  ResultSet rs = reflectDB.getNativeConnection().prepareStatement("SELECT * FROM PERSON WHERE ...").executeQuery();
    
}
```

### 4. Recap
1. Instantiate `ReflectDB`
2. Call `ReflectDB.initialize(config)`
3. Call `ReflectDB.createTablesIfNotExists();
4. Happy developing! Call `ReflectDB` methods to make life simpler.
---

Please feel free to contribute to this repository, let me know if you have any questions!
