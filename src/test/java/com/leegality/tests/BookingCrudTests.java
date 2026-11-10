
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
@Feature("Booking CRUD Operations")
@Tag("crud")
public class BookingCrudTests {

    private String getAuthToken() {
        Response authResponse = Allure.step(
                "Generate authentication token",
                () -> BookingApiClient.createAuthToken("admin", "password123")
        );

        assertEquals(200, authResponse.getStatusCode());

        String token = authResponse.jsonPath().getString("token");

        assertNotNull(token, "Authentication token should be generated");
        assertFalse(token.isBlank(), "Authentication token should not be empty");

        return token;
    }

    private int createBookingAndGetId() {
        Booking booking = TestDataFactory.createBooking();

        Response response = Allure.step(
                "Create a booking for test execution",
                () -> BookingApiClient.createBooking(booking)
        );

        assertEquals(200, response.getStatusCode(),
                "Test setup: booking creation should succeed");

        int bookingId = response.jsonPath().getInt("bookingid");

        assertTrue(bookingId > 0,
                "Test setup: booking ID should be positive");

        return bookingId;
    }

    private void cleanupBooking(int bookingId, String token) {
        Allure.step("Clean up test booking with ID: " + bookingId, () -> {
            Response cleanupResponse =
                    BookingApiClient.deleteBooking(bookingId, token);

            Allure.addAttachment(
                    "Cleanup response",
                    "text/plain",
                    "Booking ID: " + bookingId
                            + "\nHTTP Status: " + cleanupResponse.getStatusCode()
            );
        });
    }

    @Test
    @Story("Complete booking update using PUT")
    @DisplayName("PUT should replace booking details with valid authentication")
    public void putShouldUpdateCompleteBooking() {

        String token = getAuthToken();
        int bookingId = createBookingAndGetId();

        try {
            Booking updatedBooking = new Booking(
                    "Rahul",
                    "Sharma",
                    250,
                    false,
                    TestDataFactory.createBooking().getBookingdates(),
                    "Lunch"
            );

            Response updateResponse = Allure.step(
                    "Update complete booking details using PUT",
                    () -> BookingApiClient.updateBooking(
                            bookingId, updatedBooking, token)
            );

            Allure.step("Verify PUT update response", () ->
                    assertEquals(200, updateResponse.getStatusCode())
            );

            Response getResponse = Allure.step(
                    "Retrieve booking to verify persisted changes",
                    () -> BookingApiClient.getBookingById(bookingId)
            );

            Allure.step("Validate all updated booking fields", () -> {
                assertEquals(200, getResponse.getStatusCode());

                assertEquals("Rahul",
                        getResponse.jsonPath().getString("firstname"));

                assertEquals("Sharma",
                        getResponse.jsonPath().getString("lastname"));

                assertEquals(250,
                        getResponse.jsonPath().getInt("totalprice"));

                assertFalse(
                        getResponse.jsonPath().getBoolean("depositpaid"));

                assertEquals("Lunch",
                        getResponse.jsonPath().getString("additionalneeds"));
            });

        } finally {
            cleanupBooking(bookingId, token);
        }
    }

    @Test
    @Story("Partial booking update using PATCH")
    @DisplayName("PATCH should update only the supplied booking field")
    public void patchShouldUpdateOnlySpecifiedField() {

        String token = getAuthToken();
        int bookingId = createBookingAndGetId();

        try {
            Response beforeUpdate = Allure.step(
                    "Retrieve original booking details",
                    () -> BookingApiClient.getBookingById(bookingId)
            );

            assertEquals(200, beforeUpdate.getStatusCode());

            String originalLastName =
                    beforeUpdate.jsonPath().getString("lastname");

            int originalPrice =
                    beforeUpdate.jsonPath().getInt("totalprice");

            Map<String, Object> patchRequest =
                    Map.of("firstname", "UpdatedFirstName");

            Response patchResponse = Allure.step(
                    "Update only firstname using PATCH",
                    () -> BookingApiClient.partialUpdateBooking(
                            bookingId, patchRequest, token)
            );

            Allure.step("Verify PATCH response", () ->
                    assertEquals(200, patchResponse.getStatusCode())
            );

            Response afterUpdate = Allure.step(
                    "Retrieve booking after partial update",
                    () -> BookingApiClient.getBookingById(bookingId)
            );

            Allure.step("Verify updated and unchanged fields", () -> {
                assertEquals(200, afterUpdate.getStatusCode());

                assertEquals("UpdatedFirstName",
                        afterUpdate.jsonPath().getString("firstname"));

                // Fields not included in PATCH should remain unchanged.
                assertEquals(originalLastName,
                        afterUpdate.jsonPath().getString("lastname"));

                assertEquals(originalPrice,
                        afterUpdate.jsonPath().getInt("totalprice"));
            });

        } finally {
            cleanupBooking(bookingId, token);
        }
    }

    @Test
    @Story("Booking deletion using DELETE")
    @DisplayName("DELETE should remove booking with valid authentication")
    public void deleteShouldRemoveBooking() {

        String token = getAuthToken();
        int bookingId = createBookingAndGetId();

        try {
            Response deleteResponse = Allure.step(
                    "Delete booking using DELETE",
                    () -> BookingApiClient.deleteBooking(bookingId, token)
            );

            Allure.step("Verify successful deletion response", () ->
                    assertEquals(201, deleteResponse.getStatusCode())
            );

            Response getResponse = Allure.step(
                    "Verify deleted booking is no longer accessible",
                    () -> BookingApiClient.getBookingById(bookingId)
            );

            assertEquals(404, getResponse.getStatusCode(),
                    "Deleted booking should no longer be available");

        } finally {
            // Safe cleanup attempt; the booking may already be deleted.
            cleanupBooking(bookingId, token);
        }
    }
}
