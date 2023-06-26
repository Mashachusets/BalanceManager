# BalanceManager
## A Rest API service used to manage bank account balance
### Localhost URLs
 - http://localhost:8080/api/import
 - http://localhost:8080/api/export
 - http://localhost:8080/api//calculate/{accountNumber}
 - Swagger UI: http://localhost:8080/swagger-ui.html

### Requirements
Before running the project, make sure you have the following software installed on your machine:
- Java 8  (or a higher version)
- Maven 3.6 (or a newer version)

### Database Configuration
Make sure you have MySQL database and and configure it as follows:
 - Host: localhost
 - Port: 3306
 - Server Port: 8080

Create a database schema called `balance_manager`.\
Open the `application.properties` file located in the `src/main/resources` directory and modify the following properties to match your MySQL database credentials:
1. Replace `your-username` and `your-password` with your MySQL properties:
 - spring.datasource.username=your-username
 - spring.datasource.password=your-password

2. Save the `application.properties` file.

### Using project
 - Clone project from github : git clone <https://github.com/Mashachusets/BalanceManager.git>
 - Navigate to the project directory: cd BalanceManager
 - Run command: mvn spring-boot:run

### Example file
You can use an "example.csv" file from the
recourses package for file import:
 - src/main/resources/example.csv
 
Feel free to customize the instructions further based on your specific project requirements.
