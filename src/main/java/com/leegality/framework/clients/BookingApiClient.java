package com.leegality.framework.clients;

import com.leegality.framework.models.Booking;
import com.leegality.framework.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import io.qameta.allure.restassured.AllureRestAssured;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingApiClient {

    private static RequestSpecification getBaseRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.getBaseUrl())
                .addFilter(new AllureRestAssured())
                .build();
    }

    public static Response getPing() {

        return given()
                .spec(getBaseRequestSpec())
                .when()
                .get("/ping");
    }

    public static Response getBookingById(int bookingId) {

        return given()
                .spec(getBaseRequestSpec())
                .when()
                .get("/booking/{id}", bookingId);
    }


    // Create a new booking
    public static Response createBooking(Booking bookingRequest) {

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .when()
                .post("/booking");
    }

    // Get all booking IDs
    public static Response getAllBookings() {

        return given()
                .spec(getBaseRequestSpec())
                .when()
                .get("/booking");
    }

    // Update an entire booking using PUT
    public static Response updateBooking(
            int bookingId, Booking bookingRequest, String token) {

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(bookingRequest)
                .when()
                .put("/booking/{id}", bookingId);
    }

    // Partially update a booking using PATCH
    public static Response partialUpdateBooking(
            int bookingId, Map<String, Object> bookingRequest, String token) {

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(bookingRequest)
                .when()
                .patch("/booking/{id}", bookingId);
    }

    // Delete a booking
    public static Response deleteBooking(int bookingId, String token) {

        return given()
                .spec(getBaseRequestSpec())
                .cookie("token", token)
                .when()
                .delete("/booking/{id}", bookingId);
    }

    // Generate an authentication token
    public static Response createAuthToken(
            String username, String password) {

        Map<String, String> credentials = Map.of(
                "username", username,
                "password", password
        );

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .body(credentials)
                .when()
                .post("/auth");
    }
    public static Response updateBookingWithoutAuth(
            int bookingId, Booking bookingRequest) {

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .when()
                .put("/booking/{id}", bookingId);
    }

    public static Response partialUpdateBookingWithoutAuth(
            int bookingId, Map<String, Object> bookingRequest) {

        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .body(bookingRequest)
                .when()
                .patch("/booking/{id}", bookingId);
    }

    public static Response deleteBookingWithoutAuth(int bookingId) {

        return given()
                .spec(getBaseRequestSpec())
                .when()
                .delete("/booking/{id}", bookingId);
    }
    public static Response createBookingWithRawBody(String requestBody) {
        return given()
                .spec(getBaseRequestSpec())
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/booking");
    }
}
