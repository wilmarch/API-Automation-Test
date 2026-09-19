package tests;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import requests.AuthRequest;
import requests.SiswaRequest;
import utils.BaseTest;

import static org.hamcrest.Matchers.*;

public class SiswaTest extends BaseTest {

    private String token;
    private String siswaId;

    @BeforeClass
    public void getAuthToken() {
        Response response = AuthRequest.login("rusdi1@example.com", "Barber123");
        token = response.jsonPath().getString("data.token");
    }

    // Positif 2: Create Siswa
    @Test(priority = 1)
    public void testCreateSiswaSuccess() {
        long timestamp = System.currentTimeMillis();
        String randomNis = String.valueOf(timestamp).substring(7);

        JSONObject body = new JSONObject();
        body.put("nama", "Rusdi Barber");
        body.put("nis", randomNis);
        body.put("kelas", "X-IPA-1");
        body.put("jurusan", "IPA");
        body.put("email", "rusdibarber" + randomNis + "@example.com");
        body.put("telepon", "+628123456789");
        body.put("alamat", "Jl. Ngawi No. 67");

        Response response = SiswaRequest.createSiswa(token, body);

        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil ditambahkan"))
                .body("data.id", notNullValue());

        siswaId = response.jsonPath().getString("data.id");
    }

    // Positif 3: Get Siswa by ID
    @Test(priority = 2, dependsOnMethods = {"testCreateSiswaSuccess"})
    public void testGetSiswaByIdSuccess() {
        Response response = SiswaRequest.getSiswaById(token, siswaId);

        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(siswaId));
    }

    // Positif 4: Update Siswa
    @Test(priority = 3, dependsOnMethods = {"testCreateSiswaSuccess"})
    public void testUpdateSiswaSuccess() {
        JSONObject updateBody = new JSONObject();
        updateBody.put("nama", "Imut Barber");

        Response response = SiswaRequest.updateSiswa(token, siswaId, updateBody);

        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil diupdate"));
    }

    // Positif 5: Delete Siswa
    @Test(priority = 4, dependsOnMethods = {"testUpdateSiswaSuccess"})
    public void testDeleteSiswaSuccess() {
        Response response = SiswaRequest.deleteSiswa(token, siswaId);

        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil dihapus"));
    }
}