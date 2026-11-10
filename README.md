# Reservation Hub API Automation

A REST API automation framework developed as part of the Leegality QA Engineer assessment. The framework uses Java, Rest Assured, JUnit 5, Maven and Allure to validate the Reservation Hub booking APIs.

## 1. Project Overview

The framework covers functional, negative, authentication, CRUD, schema validation and exploratory API testing.

**Base URL:** https://restful-booker.herokuapp.com

**API Documentation:** https://restful-booker.herokuapp.com/apidoc/index.html

## 2. Technology Stack

- Java 17
- Maven
- Rest Assured
- JUnit 5
- Allure Reporting
- JSON Schema Validator
- IntelliJ IDEA

## 3. Framework Structure

```text
reservation_hub_api_automation/
│
├── src/
│   ├── main/java/com/leegality/framework/
│   │   ├── clients/
│   │   │   └── BookingApiClient.java
│   │   ├── config/
│   │   │   └── ConfigManager.java
│   │   ├── models/
│   │   │   ├── Booking.java
│   │   │   └── BookingDates.java
│   │   └── utils/
│   │       └── TestDataFactory.java
│   │
│   └── test/
│       ├── java/com/leegality/tests/
│       │   ├── AuthTests.java
│       │   ├── BookingCrudTests.java
│       │   ├── BookingSchemaTest.java
│       │   ├── CreateBookingTests.java
│       │   ├── GetBookingTests.java
│       │   ├── HealthCheckTest.java
│       │   ├── NegativeBookingTests.java
│       │   └── ProtectedEndpointAuthTests.java
│       │
│       └── resources/
│           ├── config.properties
│           └── schemas/
│               └── booking-schema.json
│
├── BUGS.md
├── README.md
├── pom.xml
└── .gitignore
```

## 4. Test Coverage

| Area | Coverage |
|---|---|
| Health Check | API availability and response validation |
| Authentication | Valid and invalid credentials |
| Create Booking | Booking creation and response validation |
| Retrieve Booking | Retrieve all bookings and booking by ID |
| CRUD Operations | Create, retrieve, update, partially update and delete |
| Authorization | Missing and invalid authentication tokens |
| Negative Testing | Empty payloads, missing fields, invalid data types and dates |
| Boundary Testing | Zero and negative booking prices |
| Schema Validation | JSON response structure and data types |
| Exploratory Testing | Actual API behaviour and potential defects |

## 5. Prerequisites

Ensure the following are installed:

- JDK 17
- Apache Maven 3.x
- Git
- IntelliJ IDEA (optional)

Verify Java and Maven:

```bash
java -version
mvn -version
```

## 6. Configuration

The base URL is configured in:

`src/test/resources/config.properties`

```properties
base.url=https://restful-booker.herokuapp.com
```

The framework uses a centralized configuration manager to load the base URL.

## 7. Execute Tests

Run the complete test suite:

```bash
mvn clean test
```

Run an individual test class:

```bash
mvn -Dtest=AuthTests test
mvn -Dtest=BookingCrudTests test
mvn -Dtest=NegativeBookingTests test
```

## 8. Allure Reporting

The framework integrates Allure with JUnit 5 and Rest Assured.

The reporting integration captures HTTP request and response details, while Allure steps provide additional test execution context.

Generate and open the Allure report:

```bash
mvn clean test
mvn allure:serve
```

The report includes:

- Overall test execution results
- Test-level execution details
- Request and response information
- HTTP status codes
- Logical test execution steps
- Failure details

The generated Allure results are stored under:

`target/allure-results`

## 9. Defect Findings

Exploratory testing identified potential API validation and error-handling issues.

The observations, request payloads, expected versus actual behaviour and initial severity assessments are documented in:

`BUGS.md`

These findings should be evaluated against the authoritative API contract and applicable business rules.

## 10. Test Data and Cleanup

Test data is generated dynamically through `TestDataFactory`.

Where exploratory or CRUD tests create temporary bookings, cleanup operations are performed to avoid leaving unnecessary test records in the public demo API.

## 11. Notes

- This project uses the publicly available Restful Booker demo API.
- The demo service may reset data or change behaviour.
- Test results can vary depending on the availability and state of the external service.
- Authentication credentials used by the framework are demo credentials only.
- Generated reports and build artifacts are not intended to be committed to the repository.

---

**Developed as part of the Leegality QA Engineer – API Automation Assessment.**