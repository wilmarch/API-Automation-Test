package tests;

import io.restassured.response.Response;
import org.testng.annotations.Test;
import requests.AuthRequest;
import utils.BaseTest;
import io.restassured.module.jsv.JsonSchemaValidator;
import java.io.File;

import static org.hamcrest.Matchers.*;

public class AuthTest extends BaseTest {

    // Positif 1: Login Sukses
    @Test(priority = 1)
    public void testLoginSuccess() {
        Response response = AuthRequest.login("rusdi1@example.com", "Barber123");

        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Login berhasil"))
                .body("data.token", notNullValue());

        File schemaFile = new File("src/test/resources/schemas/LoginSchema.json");
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
    }

    // Negatif 1: Login with Invalid Password
    @Test(priority = 2)
    public void testLoginInvalidPassword() {
        Response response = AuthRequest.login("rezzkoike1@example.com", "SalahPassword123");

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("error", equalTo("Email atau password salah"));
    }
}