import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;
import java.io.FileReader;
import java.io.IOException;
import static org.testng.Assert.*;

public class APITest {
    @Test
    public void Test() throws IOException, ParseException {
        // read json file
        FileReader reader = new FileReader("src/main/resources/users.json");
        // parse json data
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(reader);
        JSONArray arrUsers = (JSONArray) obj;
        arrUsers.forEach(u -> {
            JSONObject exp = ((JSONObject)u);
            RestAssured.baseURI = "https://reqres.in";
            Response res = RestAssured
                    .given()
                    .contentType(ContentType.JSON)
                    .body(u)
                    .post("/api/users");
            // check status code is 201
            assertEquals(res.statusCode(), 201, "status code");
            // check response time is within 2 seconds
            assertTrue(res.time() <= 2000);
            var act = res.then().extract();
            // check response body
            assertEquals(act.path("name"), exp.get("name"), "response body.name");
            assertEquals(act.path("job"), exp.get("job"), "response body.job");
            assertFalse(act.path("id").toString().isEmpty(), "response body id");
            assertFalse(act.path("createdAt").toString().isEmpty(), "response body createdAt");
            // check response header has Content-type and application/json
            assertTrue(res.headers().hasHeaderWithName("Content-Type"), "response header name");
            assertTrue(res.contentType().startsWith("application/json"), "response header value");
            // deserialize JSON response to POJO
            Users users = res.body().as(Users.class);
        });
    }
}
