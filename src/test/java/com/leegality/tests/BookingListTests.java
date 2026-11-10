
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import com.leegality.framework.models.Booking;
import com.leegality.framework.utils.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookingListTests {

    private Booking booking;
    private int bookingId;
    private String token;

    @BeforeEach
    public void setUp() {

        // Generate authentication token for cleanup
        Response authResponse =
                BookingApiClient.createAuthToken(
                        "admin", "password123"
                );

        assertEquals(200, authResponse.getStatusCode());
        token = authResponse.jsonPath().getString("token");

        assertNotNull(token, "Auth token should not be null");

        // Create unique test data to avoid filter collisions
        booking = TestDataFactory.createBooking();

        String uniqueValue = String.valueOf(System.currentTimeMillis());

        booking.setFirstname("QA" + uniqueValue);
        booking.setLastname("Tester" + uniqueValue);

        Response createResponse =
                BookingApiClient.createBooking(booking);

        assertEquals(200, createResponse.getStatusCode());

        bookingId = createResponse.jsonPath().getInt("bookingid");

        assertTrue(bookingId > 0);
    }

    @AfterEach
    public void tearDown() {

        // Clean up the booking created by this test
        if (bookingId > 0 && token != null) {

            Response deleteResponse =
                    BookingApiClient.deleteBooking(bookingId, token);

            assertEquals(
                    201,
                    deleteResponse.getStatusCode(),
                    "Test booking cleanup should succeed"
            );
        }
    }

    @Test
    @DisplayName("Get all bookings should return a list of booking IDs")
    public void getAllBookingsShouldReturnBookingIds() {

        Response response = BookingApiClient.getAllBookings();

        assertEquals(200, response.getStatusCode());

        List<Integer> bookingIds =
                response.jsonPath().getList("bookingid");

        assertNotNull(bookingIds);
        assertFalse(bookingIds.isEmpty());

        assertTrue(
                bookingIds.contains(bookingId),
                "Created booking should be present in the booking list"
        );
    }

    @Test
    @DisplayName("Filter bookings by first name")
    public void filterBookingsByFirstName() {

        Map<String, String> filters = Map.of(
                "firstname", booking.getFirstname()
        );

        Response response = BookingApiClient.getBookings(filters);

        assertEquals(200, response.getStatusCode());

        List<Integer> bookingIds =
                response.jsonPath().getList("bookingid");

        assertNotNull(bookingIds);

        assertTrue(
                bookingIds.contains(bookingId),
                "Filtered results should contain the created booking"
        );
    }

    @Test
    @DisplayName("Filter bookings by last name")
    public void filterBookingsByLastName() {

        Map<String, String> filters = Map.of(
                "lastname", booking.getLastname()
        );

        Response response = BookingApiClient.getBookings(filters);

        assertEquals(200, response.getStatusCode());

        List<Integer> bookingIds =
                response.jsonPath().getList("bookingid");

        assertNotNull(bookingIds);

        assertTrue(
                bookingIds.contains(bookingId),
                "Filtered results should contain the created booking"
        );
    }

    @Test
    @DisplayName("Filter bookings by check-in date range")
    public void filterBookingsByCheckinDate() {

        LocalDate checkinDate =
                LocalDate.parse(booking.getBookingdates().getCheckin());

        // API uses checkin greater than the supplied date
        String filterDate = checkinDate.minusDays(1).toString();

        Map<String, String> filters = Map.of(
                "checkin", filterDate
        );

        Response response = BookingApiClient.getBookings(filters);

        assertEquals(200, response.getStatusCode());

        List<Integer> bookingIds =
                response.jsonPath().getList("bookingid");

        assertNotNull(bookingIds);

        assertTrue(
                bookingIds.contains(bookingId),
                "Booking should be returned when its check-in date is after the filter date"
        );
    }

    @Test
    @DisplayName("Filter bookings by check-out date range")
    public void filterBookingsByCheckoutDate() {

        LocalDate checkoutDate =
                LocalDate.parse(booking.getBookingdates().getCheckout());

        // API uses checkout less than the supplied date
        String filterDate = checkoutDate.plusDays(1).toString();

        Map<String, String> filters = Map.of(
                "checkout", filterDate
        );

        Response response = BookingApiClient.getBookings(filters);

        assertEquals(200, response.getStatusCode());

        List<Integer> bookingIds =
                response.jsonPath().getList("bookingid");

        assertNotNull(bookingIds);

        assertTrue(
                bookingIds.contains(bookingId),
                "Booking should be returned when its check-out date is before the filter date"
        );
    }
}
