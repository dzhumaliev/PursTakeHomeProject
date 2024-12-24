# **Take-home project**

 

## Single-page application:

### Shows restaurant working hours:
- Open until 12 AM
- Open until 2 AM
- Open 24 hours
      
### Shows the restaurant's status:
- Open until
- open until * will reopen at **
- will reopen at * (if reopening is within the next 24 hours)
- will reopen on * (if reopening is in more than 24 hours)

### Shows the restaurant's current status with color coding:
- Open – Green
- Closed – Red
- Close within an hour – Yellow

## More about the project structure

The project is designed with a modular structure for code clarity and ease of testing, utilizing the MVVM architectural pattern.

The project is divided into four modules:

### app 
- The app module is the main module responsible for initializing and launching the application. It contains the core logic, DI modules, resources, and the manifest.

### data
- The data module serves as a bridge between the application's logic and the data source. It includes server requests (API), configuration, and DTO models.

### domain
- The domain module is responsible for the business logic of the application. It connects the data received from the server with the UI. It contains UseCases, Entities, and repository interfaces.

### ui
- The ui module is responsible for user interactions and data presentation. It handles user actions and listens for clicks on elements.



### The project uses the Hilt design pattern, which simplifies dependency injection, making the code more readable and convenient for testing.




