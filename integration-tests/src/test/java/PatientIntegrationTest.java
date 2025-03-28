import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PatientIntegrationTest {
    private static String token;
    private static UUID patientId;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";

        String loginPayload = """
                {"email": "testuser@test.com",
                "password": "password123"
                }
                """;

        token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
    }

    @Test
    public void shouldGetAllPatients() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body(not(empty()));
    }

    @Test
    public void shouldCreatePatient() {
        String patientPayload = """
                {
                    "name": "Manh Nguyen",
                    "email": "manhnguyen@example.com",
                    "address": "123 Main St, Springfield",
                    "dateOfBirth": "1985-06-15",
                    "registeredDate": "2024-01-10"
                }
                """;

        patientId = UUID.fromString(given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(patientPayload)
                .when()
                .post("/api/patients")
                .then()
                .statusCode(200)
                .body("name", equalTo("Manh Nguyen"))
                .extract()
                .jsonPath()
                .getString("id"));
    }

    @Test
    public void shouldUpdatePatient() {
        String updatePayload = """
                {
                    "name": "Manh Nguyen Updated",
                    "address": "123 Main St, Springfield",
                    "dateOfBirth": "1985-06-15",
                    "email": "manhnguyen1@example.com"
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body(updatePayload)
                .when()
                .put("/api/patients/" + patientId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Manh Nguyen Updated"));
    }

    @Test
    public void shouldDeletePatient() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/patients/" + patientId)
                .then()
                .statusCode(204);
    }
}
