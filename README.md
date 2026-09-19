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
| Reporting | Allure (allure-testng + allure-rest-assured) | 2.35.5 |

## Struktur Project

src/test/
├── java/
│ ├── requests/ → wrapper RestAssured murni (return Response, tanpa assertion)
│ │ ├── AuthRequest.java
│ │ └── SiswaRequest.java
│ ├── tests/ → test class berisi assertion (TestNG @Test)
│ │ ├── AuthTest.java
│ │ └── SiswaTest.java
│ └── utils/
│ └── BaseTest.java → setup global (baseURI, login akun dummy)
└── resources/
├── testng.xml → TestNG suite definition + listener Allure
├── allure.properties → lokasi output Allure results
└── schemas/
└── LoginSchema.json → JSON schema untuk validasi response login



**Pola desain**: 3 layer terpisah — `requests/` (murni HTTP call), `tests/` (assertion + business logic), `utils/` (setup sekali di awal run). Request builder di `requests/` sengaja dipisah dari assertion supaya reusable dan gampang di-maintain kalau endpoint berubah.

## Base Configuration

- **Base URL**: `https://api.rizqifauzan.com` (di-set sekali lewat `@BeforeSuite` di `BaseTest.java`, jalan satu kali untuk keseluruhan test run)
- **Auth**: `BaseTest` login pakai akun dummy (`AuthRequest.login()`) sekali di awal suite, token-nya (`authToken`) dipakai di semua request `Siswa` sebagai header `Authorization: Bearer <token>`

## Cakupan Test (18 test case)

### AuthTest — 7 test case
| Test | Deskripsi |
|---|---|
| `testRegisterSuccess` | Registrasi akun baru dengan email unik (timestamp) → status 200/201, cek `success`, `message`, `data.id`, `data.email` |
| `testRegisterDuplicateEmail` | Registrasi ulang pakai email yang sama dari test sebelumnya → status 400/409, error "Email sudah terdaftar" |
| `testLoginSuccess` | Login pakai kredensial valid → status 200, cek `success`/`message`/`token`, plus validasi response ke `LoginSchema.json` (kalau file schema-nya ada) |
| `testLoginInvalidPassword` | Login pakai password salah → status 401, pesan error "Email atau password salah" |
| `testGetMeSuccess` | Get data user yang sedang login (`/auth/me`) pakai token valid → status 200, `data.id` & `data.email` terisi |
| `testGetMeUnauthorized` | Get `/auth/me` tanpa token → status 401/403 |
| `testLogoutSuccess` | Login pakai akun hasil registrasi, lalu logout → status 200, pesan sukses |

### SiswaTest — 11 test case (CRUD, sebagian saling `dependsOnMethods`)
| Test | Deskripsi |
|---|---|
| `testGetAllSiswaSuccess` | Ambil seluruh data siswa (pagination default) → status 200 |
| `testGetAllSiswaWithQueryParams` | Ambil data siswa dengan query `page`, `limit`, `kelas`, `sortBy`, `sortOrder` → status 200 |
| `testGetAllSiswaUnauthorized` | Ambil data siswa tanpa token → status 401/403 |
| `testCreateSiswaSuccess` | Create siswa baru (NIS di-generate dari timestamp biar selalu unik tiap run) → status 200/201, simpan `id` & `nis` buat test berikutnya |
| `testCreateSiswaDuplicateNis` | Create siswa dengan NIS yang sama dari test sebelumnya → status 400/409, error "NIS sudah terdaftar" |
| `testGetSiswaByIdSuccess` | Get siswa pakai `id` hasil create → data cocok |
| `testGetSiswaByInvalidId` | Get siswa pakai ID yang gak ada → status 404 |
| `testUpdateSiswaSuccess` | Update seluruh data siswa (PUT) → status 200, nama berubah sesuai request |
| `testPatchSiswaSuccess` | Update sebagian field siswa (PATCH) → status 200, nama berubah sesuai request |
| `testDeleteSiswaSuccess` | Hapus data siswa yang sama → status 200, pesan sukses |
| `testDeleteSiswaNotFound` | Hapus ulang ID yang udah dihapus → status 404 |

## Cara Menjalankan Test

```bash
gradle clean test
```

Config TestNG suite-nya udah di-wire lewat `build.gradle`:
```groovy
test {
    useTestNG {
        suites "src/test/resources/testng.xml"
    }
}
```

## Melihat Report

Test ini pakai filter **AllureRestAssured**, jadi tiap request/response otomatis ke-capture. Listener Allure didaftarkan manual di `testng.xml`:
```xml
<listeners>
    <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
</listeners>
```
dan lokasi hasil mentahnya diarahkan lewat `src/test/resources/allure.properties`:
```properties
allure.results.directory=build/allure-results
```

Setelah run test, generate & buka report-nya:
```bash
allure serve build/allure-results
```

> Perlu **Allure Commandline** ter-install di komputer kamu ([panduan instalasi](https://allurereport.org/docs/install/)).

Report standar Gradle/TestNG juga tetap tersedia di `build/reports/tests/test/index.html`.

## Catatan

- Kredensial di `BaseTest`/`AuthTest` menggunakan akun **dummy/test** untuk kebutuhan demo — bukan kredensial produksi.
- Email akun baru & NIS siswa di-generate dinamis dari timestamp (`System.currentTimeMillis()`) supaya test bisa di-run berkali-kali tanpa bentrok data unik.