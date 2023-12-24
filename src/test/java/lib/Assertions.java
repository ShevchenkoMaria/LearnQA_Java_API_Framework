package lib;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertJsonByName(Response Response, String name, int expectedValue){
        Response.then().assertThat().body("$", hasKey(name));
        int value = Response.jsonPath().getInt(name);
        assertEquals(expectedValue, value, "JSON value <"+value+"> is no equal to expected value <"+expectedValue+">");
    }
    public static void assertJsonByName(Response Response, String name, String expectedValue){
        Response.then().assertThat().body("$", hasKey(name));
        String value = Response.jsonPath().getString(name);
        assertEquals(expectedValue, value, "JSON value <"+value+"> is no equal to expected value <"+expectedValue+">");
    }
    public static void assertResponseTextEquals(Response Response, String expectedAnswer){
        assertEquals(expectedAnswer,Response.asString(),"Response test is not as expected");
    }
    public static void assertResponseCodeEquals(Response Response, int expectedStatusCode){
        assertEquals(expectedStatusCode,Response.statusCode(),"Response status code is not as expected");
    }
    public static void assertJsonHasField(Response Response, String expectedKey){
        Response.then().assertThat().body("$", hasKey(expectedKey));
    }
    public static void assertJsonHasFields(Response Response, String [] expectedFieldNames){
        for (String expectedFieldName : expectedFieldNames){
            Assertions.assertJsonHasField(Response, expectedFieldName);
        }
    }
    public static void assertJsonHasNotField(Response Response, String unexpectedKey){
        Response.then().assertThat().body("$", not(hasKey(unexpectedKey)));
    }
    public static void assertJsonHasNotFields(Response Response, String [] unexpectedFieldNames){
        for (String unexpectedFieldName : unexpectedFieldNames){
            Assertions.assertJsonHasNotField(Response, unexpectedFieldName);
        }
    }
}
