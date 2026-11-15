# Hotel Management System — Review 2 & Review 3 Quick Guide

## What changed in this updated copy

1. **Custom interface:** `com.hotel.dao.GenericDAO<T>` defines common CRUD operations (`add`, `update`, `delete`, `getAll`). `CustomerDAO` and `RoomDAO` implement it while retaining the existing method names used by the Swing screens.
2. **Inheritance:** `Customer extends Person`. Common name, phone and email fields/getters/setters now live in `Person`; customer-specific ID, address and ID proof remain in `Customer`.
3. **Custom exceptions:** `HotelException` is the checked base exception; `BookingConflictException` represents invalid booking dates/guest counts; `DatabaseException` is available for database-layer error context.
4. **Exception handling:** `BookingValidator.validate()` throws `BookingConflictException`, and `BookingPanel` catches it and shows a clear Swing warning.
5. **JDBC resource handling:** `BookingDAO.createBooking()` and `PaymentDAO.processPayment()` now use try-with-resources for the Connection, so it is closed even on early return or SQL failure. Transactions commit on success and roll back on failure.
6. **Booking safety:** booking creation locks the room row (`SELECT ... FOR UPDATE`) and checks that the room is AVAILABLE before inserting the booking.
7. **Customer history:** deleting a customer who has booking history is blocked, protecting associated booking/payment records from cascading deletion.

## Run the project on Windows (VS Code)

### Requirements
- JDK 17 (the project source/target is Java 11, which JDK 17 can compile)
- MySQL Server 8.x
- MySQL Workbench
- Maven 3.x

### Database setup
1. Start the MySQL80/MySQL service in Windows Services.
2. Open MySQL Workbench and connect to your local server.
3. Open `database/hotel_management.sql` and execute the whole script.
4. Open `src/main/resources/db.properties` and set the database password:

```properties
db.url=jdbc:mysql://localhost:3306/hotel_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=YOUR_MYSQL_ROOT_PASSWORD
```

Do not commit or share your real password.

### Build and run
Open the project root (the folder containing `pom.xml`) in VS Code, open Terminal, and run:

```powershell
mvn clean package
```

Then run:

```powershell
java -jar target/HotelManagementSystem-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Alternatively, use the Java extension's Run button on `src/main/java/com/hotel/Main.java` after Maven dependencies have loaded.

Default demo login is defined by the SQL seed script. Check `database/hotel_management.sql` for the exact username/password; change it before any real deployment.

## Review 2 demonstration plan (5 + 5 + 5)

### Packages and interfaces — 5 marks
- Show `com.hotel.model.Person` and `Customer extends Person`.
- Explain that `Person` contains common fields while `Customer` contains customer-specific fields.
- Show `GenericDAO<T>` and explain that it defines a common CRUD contract.
- Show `CustomerDAO implements GenericDAO<Customer>` and `RoomDAO implements GenericDAO<Room>`.
- Explain that the generic type `T` allows the same interface to work with different model types.

### Java libraries and exception handling — 5 marks
- `java.sql`: Connection, PreparedStatement, ResultSet, SQLException and Date.
- `java.util`: List and ArrayList.
- `javax.swing`: JFrame/JPanel/JButton/JTable/JOptionPane and SwingWorker.
- `java.awt`: layouts, fonts and colors.
- Explain try-with-resources: resources close automatically.
- Explain transactions: `setAutoCommit(false)`, `commit()` on success and `rollback()` on failure.
- Show `BookingConflictException` being thrown by `BookingValidator` and caught by `BookingPanel`.

### Presentation — 5 marks
1. Launch the application and log in.
2. Show the dashboard and navigation.
3. Add a customer and show it in the customer table.
4. Add/view a room.
5. Create a booking and show the booking table.
6. Add a service, generate a bill and process a checkout/payment (use test data).
7. Show the MySQL tables in Workbench.
8. Show the interface, inheritance and exception files in the IDE.

## Review 3 demonstration plan

Demonstrate one complete hotel workflow: customer registration → room selection → booking → service addition → bill calculation → payment → checkout → room becomes available. Also demonstrate search/listing, update and delete where appropriate, and the reports dashboard.

## Viva questions and short answers

1. **What is a package?** A package groups related Java classes and avoids naming conflicts. This project uses `model`, `dao`, `database`, `ui`, `util` and `exception` packages.
2. **What is an interface?** An interface defines a contract that implementing classes must fulfil. `GenericDAO<T>` defines common CRUD operations.
3. **Why use a generic interface?** The same CRUD contract can be reused for different model types, such as Customer and Room.
4. **What is inheritance?** A class can reuse and extend another class. `Customer` inherits common person details from `Person`.
5. **What is encapsulation?** Keeping fields private and accessing them through methods such as getters and setters.
6. **What is polymorphism?** One interface/reference can represent different implementing objects; a common DAO contract can be used for different entity types.
7. **What is an exception?** An object representing an error or unusual condition during program execution.
8. **What is a custom exception?** An exception class created for an application's own error conditions, such as `BookingConflictException`.
9. **What is JDBC?** Java Database Connectivity, the Java API used to connect to and execute SQL against a database.
10. **What is Connection?** A JDBC object representing a session with the database.
11. **What is PreparedStatement?** A precompiled, parameterized SQL statement; it helps prevent SQL injection when values are bound with setters.
12. **What is ResultSet?** The table-like result returned by a SELECT query.
13. **executeQuery vs executeUpdate?** `executeQuery()` is used for SELECT and returns a ResultSet; `executeUpdate()` is used for INSERT/UPDATE/DELETE and returns the affected row count.
14. **What is a transaction?** A group of database operations treated as one unit.
15. **Why commit and rollback?** Commit saves all operations; rollback undoes the transaction if an operation fails.
16. **What is a primary key?** A column (or set of columns) that uniquely identifies each row.
17. **What is a foreign key?** A constraint linking a column to a key in another table, maintaining referential integrity.
18. **Why use try-with-resources?** It automatically closes resources such as Connection, PreparedStatement and ResultSet.
19. **What is Swing?** Java's GUI toolkit for desktop applications.
20. **What is DAO?** Data Access Object; it separates SQL/database code from the user interface.

## Important limitations to know honestly

- Room availability is still represented by a room-level AVAILABLE/OCCUPIED status. This version prevents simultaneous booking of an already occupied room, but it does not support multiple future reservations for different date ranges in the same room.
- The login implementation still uses the existing database password format. Do not describe it as password-hashed.
- A successful Java compile does not prove the database is reachable. Test the MySQL connection on the presentation machine before the review.
- Do not claim an external payment gateway or printable PDF invoice; payment is recorded in the local database and the bill is displayed by the application.
