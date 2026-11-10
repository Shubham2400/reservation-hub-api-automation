
package com.leegality.tests;
import com.leegality.framework.clients.BookingApiClient;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;


@Epic("Reservation Hub API")
@Feature("Health Check")
public class HealthCheckTest {

    @Test
    @Story("Verify API availability")
    @DisplayName("GET /ping should return HTTP 201")
    public void healthCheckShouldReturnCreatedStatus() {

        Response response = BookingApiClient.getPing();

        assertEquals(
                201,
                response.getStatusCode(),
                "Health check endpoint should return HTTP 201"
        );
    }
}
