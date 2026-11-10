
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Reservation Hub API Automation")
@Feature("Negative and Boundary Testing")
@Tag("negative")
public class NegativeBookingTests {

    private void printResult(String scenario, Response response) {

        String details =
                "Scenario: " + scenario
                        + "\nHTTP Status: " + response.getStatusCode()
                        + "\nResponse Body:\n" + response.asString();

        Allure.step("Record result: " + scenario, () -> {
            Allure.addAttachment("Exploratory API Result", details);

            System.out.println("\n===== " + scenario + " =====");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.asString());
        });
    }

    private String getAuthToken() {

        Response authResponse = Allure.step(
                "Generate authentication token for test cleanup",
                () -> BookingApiClient.createAuthToken(
                        "admin", "password123")
        );

        assertEquals(200, authResponse.getStatusCode());

        String token = authResponse.jsonPath().getString("token");

        assertNotNull(token, "Authentication token should be generated");
        assertFalse(token.isBlank(), "Authentication token should not be empty");

        return token;
    }

    private void cleanupIfCreated(Response response, String token) {

        // A successful booking creation returns HTTP 200.
        if (response.getStatusCode() != 200) {
            return;
        }

        Integer bookingId = response.jsonPath().get("bookingid");

        if (bookingId == null) {
            Allure.step("Cleanup skipped because booking ID was not found");
            return;
        }

        Response deleteResponse = Allure.step(
                "Clean up exploratory booking ID: " + bookingId,
                () -> BookingApiClient.deleteBooking(bookingId, token)
        );

        Allure.addAttachment(
                "Cleanup result",
                "Booking ID: " + bookingId
                        + "\nHTTP Status: " + deleteResponse.getStatusCode()
        );

        System.out.println(
                "Cleanup booking ID " + bookingId
                        + " | Delete status: "
                        + deleteResponse.getStatusCode()
        );
    }

    @Test
    @Story("Incorrect JSON data types")
    @DisplayName("Explore API behavior for incorrect booking data types")
    public void exploreIncorrectDataTypes() {

        String token = getAuthToken();

        String invalidPayload = """
            {
              "firstname": "Test",
              "lastname": "User",
              "totalprice": "not-a-number",
              "depositpaid": "not-a-boolean",
              "bookingdates": {
                "checkin": "2026-11-14",
                "checkout": "2026-11-18"
              },
              "additionalneeds": "None"
            }
            """;

        Response response = Allure.step(
                "Submit booking with incorrect JSON data types",
                () -> BookingApiClient.createBookingWithRawBody(invalidPayload)
        );

        printResult("Incorrect data types", response);
        cleanupIfCreated(response, token);
    }

    @Test
    @Story("Nonexistent booking resource")
    @DisplayName("Nonexistent booking ID should return HTTP 404")
    public void exploreNonexistentBookingId() {

        Response response = Allure.step(
                "Retrieve a booking using a nonexistent ID",
                () -> BookingApiClient.getBookingById(999999999)
        );

        printResult("Nonexistent booking ID", response);

        assertEquals(404, response.getStatusCode(),
                "A nonexistent booking should return HTTP 404");
    }

    @Test
    @Story("Invalid and boundary booking payloads")
    @DisplayName("Explore API behavior for invalid booking payloads")
    public void exploreInvalidBookingPayloads() {

        String token = getAuthToken();

        // 1. Empty request body
        Response emptyBody = Allure.step(
                "Submit an empty booking request body",
                () -> BookingApiClient.createBookingWithRawBody("")
        );

        printResult("Empty request body", emptyBody);
        cleanupIfCreated(emptyBody, token);

        // 2. Missing required fields
        Response missingFields = Allure.step(
                "Submit a booking with missing required fields",
                () -> BookingApiClient.createBookingWithRawBody(
                        "{\"firstname\":\"Test\"}")
        );

        printResult("Missing required fields", missingFields);
        cleanupIfCreated(missingFields, token);

        // 3. Zero booking price
        Response zeroPrice = Allure.step(
                "Submit a booking with zero total price",
                () -> BookingApiClient.createBookingWithRawBody("""
                    {
                      "firstname": "Test",
                      "lastname": "User",
                      "totalprice": 0,
                      "depositpaid": false,
                      "bookingdates": {
                        "checkin": "2026-11-14",
                        "checkout": "2026-11-18"
                      },
                      "additionalneeds": "None"
                    }
                    """)
        );

        printResult("Zero total price", zeroPrice);
        cleanupIfCreated(zeroPrice, token);

        // 4. Negative booking price
        Response negativePrice = Allure.step(
                "Submit a booking with negative total price",
                () -> BookingApiClient.createBookingWithRawBody("""
                    {
                      "firstname": "Test",
                      "lastname": "User",
                      "totalprice": -100,
                      "depositpaid": false,
                      "bookingdates": {
                        "checkin": "2026-11-14",
                        "checkout": "2026-11-18"
                      },
                      "additionalneeds": "None"
                    }
                    """)
        );

        printResult("Negative total price", negativePrice);
        cleanupIfCreated(negativePrice, token);

        // 5. Checkout before check-in
        Response invalidDateOrder = Allure.step(
                "Submit a booking where checkout is before check-in",
                () -> BookingApiClient.createBookingWithRawBody("""
                    {
                      "firstname": "Test",
                      "lastname": "User",
                      "totalprice": 100,
                      "depositpaid": false,
                      "bookingdates": {
                        "checkin": "2026-11-20",
                        "checkout": "2026-11-15"
                      },
                      "additionalneeds": "None"
                    }
                    """)
        );

        printResult("Checkout before check-in", invalidDateOrder);
        cleanupIfCreated(invalidDateOrder, token);

        // 6. Malformed check-in date
        Response malformedDate = Allure.step(
                "Submit a booking with malformed check-in date",
                () -> BookingApiClient.createBookingWithRawBody("""
                    {
                      "firstname": "Test",
                      "lastname": "User",
                      "totalprice": 100,
                      "depositpaid": false,
                      "bookingdates": {
                        "checkin": "not-a-date",
                        "checkout": "2026-11-18"
                      },
                      "additionalneeds": "None"
                    }
                    """)
        );

        printResult("Malformed check-in date", malformedDate);
        cleanupIfCreated(malformedDate, token);
    }
}
