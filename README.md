# **My Retail Restful Service**
This Application provides **product** fetching and Updating functionalities.

### Tools Used:
1. **Spring boot framework**
2. **Embedded MongoDB** - For Simulation of the NoSQL datastore
3. **Embedded Redis** - Caching datastore
4. **Embedded Wiremock** - For the simulation of the RedSky external API
5. **Junit and Jacoco** - For test and coverage report.

Running and Manual Testing the application.
In Your local environment, No spring profile configuration/selection needed. 
Steps:
1. After cloning the applicaton, `cd` to the myretail directory: `cd <pathTomyretailDir>/myretail`
2. run `mvn clean install`
3. `run java -jar /target/myretail-1.0.0.jar` and the application will be deployed in `http://localhost:8080/`

###### Testing the API steps:
1. To Fetch/Get product by ID. user url: `http://localhost:8080/my-retail/products/{productId}` 
Happy Path is with IDs [13860428, 54456119, 13264003, 12954218]
2. To Update product with ID user url: `http://localhost:8080/my-retail/products/{productId}`   
Request body: `{
   "id": 54456119,
   "name": "The Big Lebowski (Blu-ray) (Widescreen)",
   "current_price": {
   "value": 1500,
   "currency_code": "USD"
   }
   }`

_Test code coverage report:_ visit `<pathToMyretail>/myretail/target/site/jacoco/index.html`
   