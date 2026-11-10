
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import com.leegality.framework.models.Booking;
import com.leegality.framework.utils.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

public class CreateBookingTests {

    @Test
    @DisplayName("Create booking should return booking ID and correct details")
    public void createBookingShouldReturnValidResponse() {

        // Arrange: Prepare booking test data
        Booking booking = TestDataFactory.createBooking();

        // Act: Send POST request
        Response response = BookingApiClient.createBooking(booking);

        // Assert: Validate response
        assertEquals(200, response.getStatusCode());

        assertNotNull(response.jsonPath().get("bookingid"));

        assertEquals(
                booking.getFirstname(),
                response.jsonPath().getString("booking.firstname")
        );

        assertEquals(
                booking.getLastname(),
                response.jsonPath().getString("booking.lastname")
        );

        assertEquals(
                booking.getTotalprice(),
                response.jsonPath().getInt("booking.totalprice")
        );

        assertEquals(
                booking.isDepositpaid(),
                response.jsonPath().getBoolean("booking.depositpaid")
        );
    }
}
