
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
@Feature("Authentication")
@Tag("auth")
public class AuthTests {

    @Test
    @Story("Successful authentication")
    @DisplayName("Valid credentials should generate an authentication token")
    public void validCredentialsShouldGenerateToken() {

        // Arrange
        String username = "admin";
        String password = "password123";

        // Act
        Response response = Allure.step(
                "Send authentication request with valid credentials",
                () -> BookingApiClient.createAuthToken(username, password)
        );

        // Assert
        Allure.step("Verify successful authentication response", () -> {
            assertEquals(200, response.getStatusCode(),
                    "Expected HTTP 200 for the authentication request");

            String token = response.jsonPath().getString("token");

            assertNotNull(token,
                    "Authentication token should not be null");

            assertFalse(token.isBlank(),
                    "Authentication token should not be empty");
        });
    }

    @Test
    @Story("Failed authentication")
    @DisplayName("Invalid credentials should not generate an authentication token")
    public void invalidCredentialsShouldNotGenerateToken() {

        // Arrange
        String username = "invalidUser";
        String password = "wrongPassword";

        // Act
        Response response = Allure.step(
                "Send authentication request with invalid credentials",
                () -> BookingApiClient.createAuthToken(username, password)
        );

        // Assert
        Allure.step("Verify authentication is rejected", () -> {
            assertEquals(200, response.getStatusCode(),
                    "Restful Booker returns HTTP 200 for this authentication response");

            assertEquals(
                    "Bad credentials",
                    response.jsonPath().getString("reason"),
                    "Expected an authentication rejection message"
            );

            assertNull(
                    response.jsonPath().get("token"),
                    "Invalid credentials should not generate a token"
            );
        });
    }
}
