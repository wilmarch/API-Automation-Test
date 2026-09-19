package utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeSuite;
import requests.AuthRequest;

public class BaseTest {

    protected static String authToken;

    @BeforeSuite
    public void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "https://api.rizqifauzan.com";
        RestAssured.filters(new AllureRestAssured());

        // Dummy Akun
        Response response = AuthRequest.login("rusdi1@example.com", "Barber123");
        authToken = response.jsonPath().getString("data.token");
    }
}