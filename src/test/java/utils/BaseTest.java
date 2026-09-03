package utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite
    public void setUp() {
        RestAssured.reset();
        RestAssured.baseURI = "https://api.rizqifauzan.com";
        RestAssured.filters(new AllureRestAssured());
    }
}