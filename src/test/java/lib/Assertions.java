package lib;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertJsonByName(Response Response, String name, int expectedValue){
        Response.then().assertThat().body("$", hasKey(name));
        int value = Response.jsonPath().getInt(name);
        assertEquals(expectedValue, value, "JSON value <"+value+"> is no equal to expected value <"+expectedValue+">");
    }
}
