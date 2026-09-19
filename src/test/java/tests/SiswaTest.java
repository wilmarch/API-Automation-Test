package tests;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.Test;
import requests.SiswaRequest;
import utils.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class SiswaTest extends BaseTest {

    private String siswaId;
    private String savedNis;

    // Positif: Ambil Seluruh Data Siswa (Default Pagination)
    @Test(priority = 1)
    public void testGetAllSiswaSuccess() {
        Response response = SiswaRequest.getAllSiswa(authToken, null);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", notNullValue());
    }

    // Positif: Ambil Seluruh Data Siswa dengan Query Parameter
    @Test(priority = 2)
    public void testGetAllSiswaWithQueryParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("page", 1);
        params.put("limit", 5);
        params.put("kelas", "X-IPA-1");
        params.put("sortBy", "nama");
        params.put("sortOrder", "asc");

        Response response = SiswaRequest.getAllSiswa(authToken, params);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    // Negatif: Ambil Data Siswa Tanpa Token Otorisasi
    @Test(priority = 3)
    public void testGetAllSiswaUnauthorized() {
        Response response = SiswaRequest.getAllSiswaUnauthorized();
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(401), is(403)))
                .body("success", equalTo(false));
    }

    // Positif: Tambah Siswa Baru
    @Test(priority = 4)
    public void testCreateSiswaSuccess() {
        long timestamp = System.currentTimeMillis();
        savedNis = String.valueOf(timestamp).substring(7);

        JSONObject body = new JSONObject();
        body.put("nama", "Rusdi Barber");
        body.put("nis", savedNis);
        body.put("kelas", "X-IPA-1");
        body.put("jurusan", "IPA");
        body.put("email", "rusdi" + savedNis + "@example.com");
        body.put("telepon", "+628123456789");
        body.put("alamat", "Jl. Ngawi No. 67");

        Response response = SiswaRequest.createSiswa(authToken, body);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(200), is(201)))
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil ditambahkan"))
                .body("data.id", notNullValue())
                .body("data.nis", equalTo(savedNis));

        siswaId = response.jsonPath().getString("data.id");
    }

    // Negatif: Tambah Siswa dengan NIS yang Sudah Ada
    @Test(priority = 5, dependsOnMethods = {"testCreateSiswaSuccess"})
    public void testCreateSiswaDuplicateNis() {
        JSONObject body = new JSONObject();
        body.put("nama", "Rusdi Kloning");
        body.put("nis", savedNis);
        body.put("kelas", "X-IPA-1");
        body.put("jurusan", "IPA");
        body.put("email", "kloning" + savedNis + "@example.com");
        body.put("telepon", "+628123456780");
        body.put("alamat", "Jl. Duplikat No. 1");

        Response response = SiswaRequest.createSiswa(authToken, body);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(anyOf(is(400), is(409)))
                .body("success", equalTo(false))
                .body("error", equalTo("NIS sudah terdaftar"));
    }

    // Positif: Ambil Siswa Berdasarkan ID
    @Test(priority = 6, dependsOnMethods = {"testCreateSiswaSuccess"})
    public void testGetSiswaByIdSuccess() {
        Response response = SiswaRequest.getSiswaById(authToken, siswaId);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data.id", equalTo(siswaId))
                .body("data.nis", equalTo(savedNis));
    }

    // Negatif: Ambil Siswa dengan ID Tidak Terdaftar
    @Test(priority = 7)
    public void testGetSiswaByInvalidId() {
        Response response = SiswaRequest.getSiswaById(authToken, "id-palsu-tidak-ada");
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("error", equalTo("Siswa tidak ditemukan"));
    }

    // Positif: Update Seluruh Data Siswa (PUT)
    @Test(priority = 8, dependsOnMethods = {"testCreateSiswaSuccess"})
    public void testUpdateSiswaSuccess() {
        JSONObject updateBody = new JSONObject();
        updateBody.put("nama", "Imut Barber");
        updateBody.put("email", "imut" + savedNis + "@example.com");

        Response response = SiswaRequest.updateSiswa(authToken, siswaId, updateBody);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil diupdate"))
                .body("data.nama", equalTo("Imut Barber"));
    }

    // Positif: Update Sebagian Field Data Siswa (PATCH)
    @Test(priority = 9, dependsOnMethods = {"testUpdateSiswaSuccess"})
    public void testPatchSiswaSuccess() {
        JSONObject patchBody = new JSONObject();
        patchBody.put("nama", "Jane Patched");

        Response response = SiswaRequest.patchSiswa(authToken, siswaId, patchBody);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil diupdate"))
                .body("data.nama", equalTo("Jane Patched"));
    }

    // Positif: Hapus Data Siswa
    @Test(priority = 10, dependsOnMethods = {"testPatchSiswaSuccess"})
    public void testDeleteSiswaSuccess() {
        Response response = SiswaRequest.deleteSiswa(authToken, siswaId);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", equalTo("Siswa berhasil dihapus"));
    }

    // Negatif: Hapus Kembali ID yang Sudah Terhapus
    @Test(priority = 11, dependsOnMethods = {"testDeleteSiswaSuccess"})
    public void testDeleteSiswaNotFound() {
        Response response = SiswaRequest.deleteSiswa(authToken, siswaId);
        response.then().log().ifValidationFails();

        response.then()
                .statusCode(404)
                .body("success", equalTo(false))
                .body("error", equalTo("Siswa tidak ditemukan"));
    }
}