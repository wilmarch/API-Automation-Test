package requests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

public class AuthRequest {

    public static Response login(String email, String password){
        JSONObject body = new JSONObject();
        body.put("email", email);
        body.put("password", password);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body.toString())
                .when()
                .post("/api/auth/login");
    }
}
