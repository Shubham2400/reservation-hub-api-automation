
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import com.leegality.framework.models.Booking;
import com.leegality.framework.utils.TestDataFactory;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Reservation Hub API Automation")
@Feature("Authentication and Authorization")
@Tag("auth")
public class ProtectedEndpointAuthTests {

    private int createBookingAndGetId() {

        Booking booking = TestDataFactory.createBooking();

        Response response = Allure.step(
                "Create a booking for authorization testing",
                () -> BookingApiClient.createBooking(booking)
        );

        assertEquals(200, response.getStatusCode(),
                "Test setup: booking creation should succeed");

        int bookingId = response.jsonPath().getInt("bookingid");

        assertTrue(bookingId > 0,
                "Test setup: booking ID should be valid");

        return bookingId;
    }

    private void cleanupBooking(int bookingId) {

        Response authResponse = Allure.step(
                "Generate authentication token for test cleanup",
                () -> BookingApiClient.createAuthToken("admin", "password123")
        );

        String token = authResponse.jsonPath().getString("token");

        if (token != null && !token.isBlank()) {

            Allure.step("Clean up test booking with ID: " + bookingId, () -> {

                Response deleteResponse =
                        BookingApiClient.deleteBooking(bookingId, token);

                Allure.addAttachment(
                        "Cleanup response",
                        "text/plain",
                        "Booking ID: " + bookingId
                                + "\nHTTP Status: "
                                + deleteResponse.getStatusCode()
                );
            });
        }
    }

    @Test
    @Story("PUT authorization validation")
    @DisplayName("PUT should reject missing and invalid authentication")
    public void putShouldRejectInvalidAuthentication() {

        int bookingId = createBookingAndGetId();

        try {
            Booking updatedBooking = TestDataFactory.createBooking();

            Response missingTokenResponse = Allure.step(
                    "Send PUT request without authentication token",
                    () -> BookingApiClient.updateBookingWithoutAuth(
                            bookingId, updatedBooking)
            );

            Allure.step("Verify PUT rejects missing authentication", () ->
                    assertEquals(
                            403,
                            missingTokenResponse.getStatusCode(),
                            "PUT should reject requests without an authentication token"
                    )
            );

            Response invalidTokenResponse = Allure.step(
                    "Send PUT request with an invalid authentication token",
                    () -> BookingApiClient.updateBooking(
                            bookingId, updatedBooking, "invalid-token")
            );

            Allure.step("Verify PUT rejects invalid authentication", () ->
                    assertEquals(
                            403,
                            invalidTokenResponse.getStatusCode(),
                            "PUT should reject requests with an invalid token"
                    )
            );

        } finally {
            cleanupBooking(bookingId);
        }
    }

    @Test
    @Story("PATCH authorization validation")
    @DisplayName("PATCH should reject missing and invalid authentication")
    public void patchShouldRejectInvalidAuthentication() {

        int bookingId = createBookingAndGetId();

        try {
            Map<String, Object> patchRequest =
                    Map.of("firstname", "UpdatedName");

            Response missingTokenResponse = Allure.step(
                    "Send PATCH request without authentication token",
                    () -> BookingApiClient.partialUpdateBookingWithoutAuth(
                            bookingId, patchRequest)
            );

            Allure.step("Verify PATCH rejects missing authentication", () ->
                    assertEquals(
                            403,
                            missingTokenResponse.getStatusCode(),
                            "PATCH should reject requests without an authentication token"
                    )
            );

            Response invalidTokenResponse = Allure.step(
                    "Send PATCH request with an invalid authentication token",
                    () -> BookingApiClient.partialUpdateBooking(
                            bookingId, patchRequest, "invalid-token")
            );

            Allure.step("Verify PATCH rejects invalid authentication", () ->
                    assertEquals(
                            403,
                            invalidTokenResponse.getStatusCode(),
                            "PATCH should reject requests with an invalid token"
                    )
            );

        } finally {
            cleanupBooking(bookingId);
        }
    }

    @Test
    @Story("DELETE authorization validation")
    @DisplayName("DELETE should reject missing and invalid authentication")
    public void deleteShouldRejectInvalidAuthentication() {

        int bookingId = createBookingAndGetId();

        try {
            Response missingTokenResponse = Allure.step(
                    "Send DELETE request without authentication token",
                    () -> BookingApiClient.deleteBookingWithoutAuth(bookingId)
            );

            Allure.step("Verify DELETE rejects missing authentication", () ->
                    assertEquals(
                            403,
                            missingTokenResponse.getStatusCode(),
                            "DELETE should reject requests without an authentication token"
                    )
            );

            Response invalidTokenResponse = Allure.step(
                    "Send DELETE request with an invalid authentication token",
                    () -> BookingApiClient.deleteBooking(
                            bookingId, "invalid-token")
            );

            Allure.step("Verify DELETE rejects invalid authentication", () ->
                    assertEquals(
                            403,
                            invalidTokenResponse.getStatusCode(),
                            "DELETE should reject requests with an invalid token"
                    )
            );

        } finally {
            cleanupBooking(bookingId);
        }
    }
}