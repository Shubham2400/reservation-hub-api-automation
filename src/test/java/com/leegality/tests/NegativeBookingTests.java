
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Reservation Hub API Automation")
@Feature("Negative and Boundary Testing")
@Tag("negative")

public class NegativeBookingTests {

    // Generate test payloads for individual parameterized executions
    static Stream<Arguments> invalidPayloadScenarios() {

        LocalDate checkin = LocalDate.now().plusDays(7);
        LocalDate checkout = LocalDate.now().plusDays(11);

        String validCheckin = checkin.toString();
        String validCheckout = checkout.toString();

        return Stream.of(

                Arguments.of(
                        "Incorrect JSON data types",
                        bookingPayload(
                                "\"not-a-number\"",
                                "\"not-a-boolean\"",
                                validCheckin,
                                validCheckout
                        )
                ),

                Arguments.of(
                        "Empty request body",
                        ""
                ),

                Arguments.of(
                        "Missing required fields",
                        "{\"firstname\":\"Test\"}"
                ),

                Arguments.of(
                        "Zero total price",
                        bookingPayload(
                                "0", "false",
                                validCheckin, validCheckout
                        )
                ),

                Arguments.of(
                        "Negative total price",
                        bookingPayload(
                                "-100", "false",
                                validCheckin, validCheckout
                        )
                ),

                Arguments.of(
                        "Checkout before check-in",
                        bookingPayload(
                                "100", "false",
                                checkin.plusDays(6).toString(),
                                checkin.plusDays(1).toString()
                        )
                ),

                Arguments.of(
                        "Malformed check-in date",
                        bookingPayload(
                                "100", "false",
                                "not-a-date",
                                validCheckout
                        )
                )
        );
    }

    // Reusable JSON payload builder
    private static String bookingPayload(
            String totalPrice,
            String depositPaid,
            String checkin,
            String checkout) {

        return """
                {
                  "firstname": "Test",
                  "lastname": "User",
                  "totalprice": %s,
                  "depositpaid": %s,
                  "bookingdates": {
                    "checkin": "%s",
                    "checkout": "%s"
                  },
                  "additionalneeds": "None"
                }
                """.formatted(
                totalPrice,
                depositPaid,
                checkin,
                checkout
        );
    }

    // Record actual API behavior in Allure
    private void printResult(String scenario, Response response) {

        String details =
                "Scenario: " + scenario
                        + "\nHTTP Status: " + response.getStatusCode()
                        + "\nResponse Body:\n" + response.asString();

        Allure.step("Record result: " + scenario, () -> {

            Allure.addAttachment(
                    "Exploratory API Result",
                    details
            );

            System.out.println("\n===== " + scenario + " =====");
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.asString());
        });
    }

    // Generate authentication token for cleanup
    private String getAuthToken() {

        Response authResponse = Allure.step(
                "Generate authentication token for test cleanup",
                () -> BookingApiClient.createAuthToken(
                        "admin", "password123"
                )
        );

        assertEquals(200, authResponse.getStatusCode());

        String token = authResponse.jsonPath().getString("token");

        assertNotNull(token, "Authentication token should be generated");
        assertFalse(token.isBlank(), "Authentication token should not be empty");

        return token;
    }

    // Clean up bookings created by exploratory scenarios
    private void cleanupIfCreated(Response response, String token) {

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

        assertEquals(
                201,
                deleteResponse.getStatusCode(),
                "Exploratory booking cleanup should succeed"
        );

        System.out.println(
                "Cleanup booking ID " + bookingId
                        + " | Delete status: "
                        + deleteResponse.getStatusCode()
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("invalidPayloadScenarios")
    @Story("Invalid and boundary booking payloads")
    @DisplayName("Explore API behavior for invalid and boundary payloads")
    public void exploreInvalidBookingPayloads(
            String scenario,
            String invalidPayload) {

        String token = getAuthToken();

        Response response = Allure.step(
                "Submit scenario: " + scenario,
                () -> BookingApiClient.createBookingWithRawBody(invalidPayload)
        );

        assertNotNull(response, "API response should not be null");

        printResult(scenario, response);

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

        assertEquals(
                404,
                response.getStatusCode(),
                "A nonexistent booking should return HTTP 404"
        );
    }
}
