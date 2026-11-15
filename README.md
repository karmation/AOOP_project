# Hotel Management System (Java + MySQL + JDBC)

A complete, runnable desktop application for hotel management, built as a college project using Java Swing, JDBC, and MySQL.

## Features

- **Authentication**: Secure login system (`admin` / `admin123`).
- **Dashboard**: Modern UI navigation with sidebar.
- **Room Management**: Add, update, delete rooms. Track availability and pricing.
- **Customer Management**: Register guests with full validation (email, phone, ID proof).
- **Booking Management**: Create bookings, check-in, check-out dates validation. Auto-updates room status to OCCUPIED using JDBC Transactions.
- **Services Management**: Offer extra services (food, laundry). Add them dynamically to active bookings.
- **Billing System**: Auto-calculates room charges (per day) and aggregates service charges. Processes payments and frees up the room (changes status back to AVAILABLE) using SQL transactions.
- **Reports**: Real-time dashboard showing total revenue, active bookings, and available rooms.

## Technologies Used

- **Java 11+**
- **Java Swing** for GUI
- **FlatLaf** (Flat Look and Feel) for modern UI
- **MySQL 8.0+** for the database
- **JDBC (MySQL Connector/J)**
- **Maven** for dependency management

## Architecture

This project follows the **MVC (Model-View-Controller)** pattern conceptually, separated into layers:

```
UI Layer (Java Swing Panels)
       ↓
DAO Layer (Data Access Object - SQL Queries)
       ↓
JDBC (Database Connection & Transactions)
       ↓
MySQL (Relational Database)
```

## Database Structure

- `users`
- `rooms`
- `customers`
- `bookings`
- `services`
- `customer_services`
- `payments`

## Installation & Setup

### 1. Setup MySQL Database
1. Open MySQL Workbench or your terminal.
2. Run the provided SQL script: `database/hotel_management.sql`.
   This will create the database, tables, and insert sample data (including the default admin user).

### 2. Configure Database Credentials
1. Open `src/main/resources/db.properties`.
2. Update the `db.user` and `db.password` fields to match your local MySQL credentials.
   ```properties
   db.url=jdbc:mysql://localhost:3306/hotel_management?useSSL=false&serverTimezone=UTC
   db.user=your_mysql_username
   db.password=your_mysql_password
   ```

### 3. Build and Run the Project

**Using an IDE (IntelliJ IDEA, Eclipse, or NetBeans):**
1. Open the IDE and select **Open** or **Import Project**.
2. Select the `HotelManagementSystem` folder (make sure to import it as a **Maven Project**).
3. Wait for Maven to download the dependencies (`mysql-connector-java` and `flatlaf`).
4. Locate `src/main/java/com/hotel/Main.java`.
5. Right-click `Main.java` and select **Run Main.main()**.

**Using Command Line (Maven):**
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.hotel.Main"
```

## Default Login

```
Username: admin
Password: admin123
```

## Future Improvements

- Replace plain text passwords with BCrypt hashing.
- Add PDF bill generation (e.g., using iText).
- Add data export features (Excel/CSV).
- Implement a calendar view for bookings.

---

## Viva Preparation Notes

### 1. Java Concepts Used
- **OOP (Object-Oriented Programming):**
  - **Classes & Objects**: `User`, `Room`, `Customer` are classes. When we fetch data from the DB, we create *objects* of these classes.
  - **Encapsulation**: All fields in the model classes are `private` and accessed via `public getter/setter` methods. This protects the data state.
- **Exception Handling**: We use `try-catch` blocks extensively, especially around JDBC operations (catching `SQLException`), so the app doesn't crash if the database goes down. We show `JOptionPane` alerts instead.
- **Collections**: We use `List` (specifically `ArrayList`) to store multiple rows returned from the DB before displaying them in the Swing JTables.

### 2. JDBC Concepts Used
- **Driver**: `com.mysql.cj.jdbc.Driver` - acts as a bridge between Java and MySQL.
- **Connection**: Established via `DriverManager.getConnection()`. Represents the physical session with the DB.
- **PreparedStatement**: Used for *all* queries. It compiles the query on the database side and safely injects parameters using `?`. This prevents **SQL Injection** attacks.
- **ResultSet**: Holds the data retrieved from a `SELECT` query. We iterate through it using `rs.next()`.
- **Transactions (`commit()` and `rollback()`)**: Used in `BookingDAO` and `PaymentDAO`. When checking out a user, we must (1) Insert Payment, (2) Update Booking status, (3) Update Room status. We set `con.setAutoCommit(false)`. If any of the 3 steps fail, we call `con.rollback()` to undo everything. If all succeed, we call `con.commit()`.

### 3. SQL Concepts Used
- **Primary & Foreign Keys**: Used to link tables (e.g., `bookings.customer_id` links to `customers.customer_id`). We use `ON DELETE CASCADE` so if a customer is deleted, their bookings go away automatically.
- **Aggregate Functions**: `SUM(amount)` in `PaymentDAO` to calculate Total Revenue. `COUNT(*)` to get active bookings.
- **JOINs**: Used in `ServiceDAO` to join `customer_services` with `services` to get the actual service name and price for a booking's bill.

### Common Viva Questions

**Q1. Why did you use PreparedStatement instead of Statement?**
*Answer:* PreparedStatement is faster because it pre-compiles the SQL query. More importantly, it is secure against SQL Injection because user inputs are treated as raw values, not executable code.

**Q2. What is the DAO pattern?**
*Answer:* DAO stands for Data Access Object. It abstracts and encapsulates all access to the data source. Our UI classes don't know *how* to save a Customer; they just call `customerDAO.addCustomer()`. This separates the business/UI logic from the database logic.

**Q3. Explain how check-out works in your database.**
*Answer:* It uses a JDBC Transaction. We start by turning off auto-commit. Then we execute three UPDATE/INSERT statements: inserting the payment record, changing the booking status to COMPLETED, and freeing up the room by changing its status to AVAILABLE. Only if all three succeed do we commit the transaction.

**Q4. What is a NullPointerException and how do you avoid it?**
*Answer:* It occurs when you call a method on a reference variable that points to `null`. We avoid it by checking for nulls (e.g., in `ValidationUtil`) before performing operations, and by ensuring our DAOs return empty Lists instead of null when no records are found.


## Review 2 & Review 3 preparation

See [REVIEW_2_3_CRASH_GUIDE.md](REVIEW_2_3_CRASH_GUIDE.md) for the added AOOP concepts, build/run steps, demonstration plan, limitations, and viva questions.
