
package com.leegality.tests;

import com.leegality.framework.clients.BookingApiClient;
import com.leegality.framework.utils.TestDataFactory;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.*;

@Tag("schema")
public class BookingSchemaTest {

    @Test
    @DisplayName("Verify GET booking response against JSON schema")
    public void verifyBookingResponseSchema() {

        String token = BookingApiClient
                .createAuthToken("admin", "password123")
                .jsonPath()
                .getString("token");

        Integer bookingId = null;

        try {
            // Create a valid booking
            Response createResponse =
                    BookingApiClient.createBooking(
                            TestDataFactory.createBooking()
                    );

            assertEquals(200, createResponse.getStatusCode());

            bookingId = createResponse.jsonPath().getInt("bookingid");

            assertTrue(bookingId > 0);

            // Retrieve the created booking
            Response getResponse =
                    BookingApiClient.getBookingById(bookingId);

            assertEquals(200, getResponse.getStatusCode());

            // Validate the response structure and data types
            getResponse.then()
                    .assertThat()
                    .body(matchesJsonSchemaInClasspath(
                            "schemas/booking-schema.json"
                    ));

        } finally {

            // Clean up the created booking
            if (bookingId != null && token != null) {
                Response deleteResponse =
                        BookingApiClient.deleteBooking(bookingId, token);

                System.out.println(
                        "Schema test cleanup status: "
                                + deleteResponse.getStatusCode()
                );
            }
        }
    }
}
