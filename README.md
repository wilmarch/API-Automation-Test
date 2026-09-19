# API Automation Test — api.rizqifauzan

Automated API testing suite untuk endpoint **Authentication** dan **Siswa (Student)** di `api.rizqifauzan.com`, dibangun pakai **RestAssured + TestNG**.

## Tech Stack & Library

| Kategori | Tools/Library | Versi |
|---|---|---|
| Bahasa | Java | - |
| Build Tool | Gradle | - |
| Test Runner | TestNG | 7.7.1 |
| API Testing | RestAssured | 5.3.0 |
| JSON Handling | org.json | 20220924 |
| Schema Validation | rest-assured json-schema-validator | 5.3.0 |
| Reporting | Allure (allure-testng + allure-rest-assured) | 2.24.0 |

## Struktur Project

```
src/test/
├── java/
│   ├── requests/          → wrapper RestAssured murni (return Response, tanpa assertion)
│   │   ├── AuthRequest.java
│   │   └── SiswaRequest.java
│   ├── tests/             → test class berisi assertion (TestNG @Test)
│   │   ├── AuthTest.java
│   │   └── SiswaTest.java
│   └── utils/
│       └── BaseTest.java  → setup global (baseURI, Allure filter)
└── resources/
    ├── testng.xml         → TestNG suite definition
    └── schemas/
        └── LoginSchema.json  → JSON schema untuk validasi response login
```

**Pola desain**: 3 layer terpisah — `requests/` (murni HTTP call), `tests/` (assertion + business logic), `utils/` (setup sekali di awal run). Request builder di `requests/` sengaja dipisah dari assertion supaya reusable dan gampang di-maintain kalau endpoint berubah.

## Base Configuration

- **Base URL**: `https://api.rizqifauzan.com` (di-set sekali di `BaseTest.java` lewat `@BeforeSuite`, jalan satu kali untuk keseluruhan test run)
- **Auth**: login dulu buat dapetin token (`AuthRequest.login()`), token disimpan lalu dipakai sebagai header `Authorization: Bearer <token>` di semua request `Siswa`

## Cakupan Test (6 test case)

### AuthTest — 2 test case
| Test | Deskripsi |
|---|---|
| `testLoginSuccess` | Login pakai kredensial valid → status 200, cek `success`/`message`/`token`, plus validasi response cocok sama `LoginSchema.json` |
| `testLoginInvalidPassword` | Login pakai password salah → status 401, pesan error sesuai |

### SiswaTest — 4 test case (CRUD chain, saling `dependsOnMethods`)
| Test | Deskripsi |
|---|---|
| `testCreateSiswaSuccess` | Create siswa baru (NIS di-generate dari timestamp biar selalu unik tiap run) → status 200/201, simpan `id` buat dipakai test berikutnya |
| `testGetSiswaByIdSuccess` | Get siswa pakai `id` dari hasil create → data cocok |
| `testUpdateSiswaSuccess` | Update nama siswa → status 200, pesan sukses |
| `testDeleteSiswaSuccess` | Delete siswa yang sama → status 200, pesan sukses |

`@BeforeClass` di `SiswaTest` otomatis login duluan (`getAuthToken()`) sebelum test manapun jalan, jadi token selalu siap dipakai di seluruh class.

## Cara Menjalankan Test

```bash
./gradlew test
```

Config TestNG suite-nya udah di-wire otomatis lewat `build.gradle`:
```groovy
test {
    useTestNG {
        suites "src/test/resources/testng.xml"
    }
}
```

## Melihat Report

Test ini pakai filter **Allure** (`AllureRestAssured`), jadi tiap request/response otomatis ke-capture buat report Allure. Setelah run:

```bash
allure serve build/allure-results
```

> Perlu Allure Commandline ter-install di komputer kamu ([panduan instalasi](https://allurereport.org/docs/install/)). Kalau belum ada plugin Allure Gradle, hasil mentahnya tetap tersimpan di `build/allure-results/` walau belum otomatis ke-render jadi HTML.

Report standar Gradle/TestNG juga tetap tersedia di `build/reports/tests/test/index.html`.

## Catatan

- Kredensial di `AuthTest`/`SiswaTest` menggunakan akun **dummy/test** untuk kebutuhan demo (lihat kode sumber atau environment variable lokal) — bukan kredensial produksi, dan sengaja tidak ditampilkan di sini.
- NIS siswa di-generate dinamis dari timestamp (`System.currentTimeMillis()`) supaya test bisa di-run berkali-kali tanpa bentrok data unik.
