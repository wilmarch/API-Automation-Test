package tests;

import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.Test;
import requests.AuthRequest;
import utils.BaseTest;

import java.io.File;

import static org.hamcrest.Matchers.*;

public class AuthTest extends BaseTest {

    private String registeredEmail;
    private final String registeredPassword = "Password123";

    // Positif: Registrasi Akun Baru
    @Test(priority = 1)
    public void testRegisterSuccess() {
        registeredEmail = "user_" + System.currentTimeMillis() + "@example.com";

        JSONObject body = new JSONObject();
        body.put("nama", "Automation User");
        body.put("email", registeredEmail);
        body.put("password", registeredPassword);

        Response response = AuthRequest.register(body);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("success", equalTo(true))
                .body("message", equalTo("Registrasi berhasil"))
                .body("data.id", notNullValue())
                .body("data.email", equalTo(registeredEmail));
    }

    // Negatif: Registrasi Menggunakan Email yang Sudah Terdaftar
    @Test(priority = 2, dependsOnMethods = {"testRegisterSuccess"})
    public void testRegisterDuplicateEmail() {
        JSONObject body = new JSONObject();
        body.put("nama", "Duplicate User");
        body.put("email", registeredEmail);
        body.put("password", registeredPassword);

        Response response = AuthRequest.register(body);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(400), is(409)))
                .body("success", equalTo(false))
                .body("error", equalTo("Email sudah terdaftar"));
    }

    // Positif: Login Sukses & Validasi Schema
    @Test(priority = 3)
    public void testLoginSuccess() {
        Response response = AuthRequest.login("rusdi1@example.com", "Barber123");
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Login berhasil"))
                .body("data.token", notNullValue());

        File schemaFile = new File("src/test/resources/schemas/LoginSchema.json");
        if (schemaFile.exists()) {
            response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(schemaFile));
        }
    }

    // Negatif: Login Password Salah
    @Test(priority = 4)
    public void testLoginInvalidPassword() {
        Response response = AuthRequest.login("rusdi1@example.com", "PasswordSalah123");
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("error", equalTo("Email atau password salah"));
    }

    // Positif: Get Current User (Me)
    @Test(priority = 5)
    public void testGetMeSuccess() {
        Response response = AuthRequest.getMe(authToken);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", notNullValue())
                .body("data.email", notNullValue());
    }

    // Negatif: Get Current User Tanpa Token
    @Test(priority = 6)
    public void testGetMeUnauthorized() {
        Response response = AuthRequest.getMeUnauthorized();
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(401), is(403)))
                .body("success", equalTo(false));
    }

    // Positif: Logout User
    @Test(priority = 7, dependsOnMethods = {"testRegisterSuccess"})
    public void testLogoutSuccess() {
        Response loginRes = AuthRequest.login(registeredEmail, registeredPassword);
        String tempToken = loginRes.jsonPath().getString("data.token");

        Response response = AuthRequest.logout(tempToken);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Logout berhasil"));
    }
}