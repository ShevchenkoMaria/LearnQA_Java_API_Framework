package tests;

import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.qameta.allure.SeverityLevel.*;

@Epic("Checking different types request with url 'https://playground.learnqa.ru/api/user/'")
@Feature("Checking GET-requests for edit user")
@Issue("GET-requests")
public class Ex16UserGetTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests= new ApiCoreRequests();
    @Test
    @Description("This test verifies that user data cannot be received if the request is made by an unauthorized user")
    @DisplayName("Test negative get user data without auth")
    @Severity(NORMAL)
    public void testGetUserDataNotAuth(){
        Response responseUserData = RestAssured
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();
        String [] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasField(responseUserData,"username");
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }
    @Test
    @Description("This test successfully get user data")
    @DisplayName("Test positive get user data")
    @Severity(CRITICAL)
    public void testGetUserDetailsAuthAsSameUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
        String cookie = this.getCookie(responseGetAuth,"auth_sid");
        String header = this.getHeader(responseGetAuth,"x-csrf-token");
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get("https://playground.learnqa.ru/api/user/2")
                .andReturn();

        String [] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData,expectedFields);
    }
    @Test
    @Description("The test verifies that one user's data cannot be received if the request is performed by another user, in this case the base one")
    @DisplayName("Test negative get user with auth as based user")
    @Severity(MINOR)
    public void testGetUserDetailsAuthAsAnotherUser(){
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        String userId = responseCreateAuth.jsonPath().getString("id");

        //LOGIN USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);
        String cookie = this.getCookie(responseGetAuth,"auth_sid");
        String header = this.getHeader(responseGetAuth,"x-csrf-token");

        //GET USER DETAILS
        Response responseUserData = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/"+userId, header, cookie);

        String [] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasField(responseUserData,"username");
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }
}
