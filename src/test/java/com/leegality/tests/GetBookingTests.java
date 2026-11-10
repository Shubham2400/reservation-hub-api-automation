
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import com.leegality.framework.models.Booking;
import com.leegality.framework.utils.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class GetBookingTests {

    @Test
    @DisplayName("Get booking by ID should return the created booking")
    public void getBookingByIdShouldReturnCorrectDetails() {

        // Arrange: Create a booking first
        Booking booking = TestDataFactory.createBooking();

        Response createResponse =
                BookingApiClient.createBooking(booking);

        assertEquals(200, createResponse.getStatusCode());

        int bookingId =
                createResponse.jsonPath().getInt("bookingid");

        assertTrue(bookingId > 0, "Booking ID should be positive");

        // Act: Retrieve the created booking
        Response getResponse =
                BookingApiClient.getBookingById(bookingId);

        // Assert: Validate retrieved booking
        assertEquals(200, getResponse.getStatusCode());

        assertEquals(
                booking.getFirstname(),
                getResponse.jsonPath().getString("firstname")
        );

        assertEquals(
                booking.getLastname(),
                getResponse.jsonPath().getString("lastname")
        );

        assertEquals(
                booking.getTotalprice(),
                getResponse.jsonPath().getInt("totalprice")
        );
    }
}
