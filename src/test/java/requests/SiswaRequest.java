package requests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;

public class SiswaRequest {

    public static Response createSiswa(String token, JSONObject body) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(body.toString())
                .when()
                .post("/api/siswa");
    }

    public static Response getSiswaById(String token, String id) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("id", id)
                .when()
                .get("/api/siswa/{id}");
    }

    public static Response updateSiswa(String token, String id, JSONObject body) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", id)
                .body(body.toString())
                .when()
                .put("/api/siswa/{id}");
    }

    public static Response deleteSiswa(String token, String id) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .pathParam("id", id)
                .when()
                .delete("/api/siswa/{id}");
    }
}