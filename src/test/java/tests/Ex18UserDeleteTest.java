package tests;

import io.qameta.allure.*;
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
@Feature("Checking DELETE-requests for edit user")
@Issue("DELETE-requests")
public class Ex18UserDeleteTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests= new ApiCoreRequests();
    @Test
    @Description("The test verifies that one user's  cannot be deleted if the request is performed by another user, in this case the base one")
    @DisplayName("Test negative delete user with auth as based user")
    @Severity(MINOR)
    public void testEditUserAuthAsBasedUser() {
        //LOGIN USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);
        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        String header = this.getHeader(responseGetAuth, "x-csrf-token");

        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequest("https://playground.learnqa.ru/api/user/2", header, cookie);
        Assertions.assertResponseTextEquals(responseDeleteUser, "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");
    }
    @Test
    @Description("This test successfully delete user")
    @DisplayName("Test positive delete user")
    @Severity(CRITICAL)
    public void testUserDeleteAuthAsSameUser() {
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        String userId = responseCreateAuth.jsonPath().getString("id");

        //LOGIN USER
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));
        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        //DELETE USER
        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }
    @Test
    @Description("The test verifies that one user's  cannot be deleted if the request is performed by another user")
    @DisplayName("Test negative delete user with auth as another user")
    @Severity(MINOR)
    public void testUserDeleteAuthAsAnotherUser(){
        //GENERATE USER№1
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        String userId = responseCreateAuth.jsonPath().getString("id");

        //GENERATE USER№2
        Map<String, String> userData2 = DataGenerator.getRegistrationData();
        Response responseCreateAuth2 = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData2);
        String userId2 = responseCreateAuth2.jsonPath().getString("id");

        //LOGIN USER№1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));
        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData);

        //DELETE USER№2
        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequest("https://playground.learnqa.ru/api/user/"+ userId2,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));

        //GET FOR USER#1
        Response responseUser1Data = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertResponseTextEquals(responseUser1Data, "User not found");

        //LOGIN USER№2
        Map<String, String> authData2 = new HashMap<>();
        authData2.put("email", userData2.get("email"));
        authData2.put("password", userData2.get("password"));
        Response responseGetAuth2 = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login", authData2);

        //GET FOR USER#1
        Response responseUser2Data = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId2,
                        this.getHeader(responseGetAuth2, "x-csrf-token"),
                        this.getCookie(responseGetAuth2, "auth_sid"));
        Assertions.assertJsonHasField(responseUser2Data,"id");
    }
    @Test
    @Description("This test verifies that user data cannot be deleted if the request is made by an unauthorized user")
    @DisplayName("Test negative delete user data without auth")
    @Severity(NORMAL)
    public void testUserDeleteWithoutAuth(){
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        String userId = responseCreateAuth.jsonPath().getString("id");

        //DELETE USER
        Response responseDeleteUser = apiCoreRequests
                .makeDeleteRequestWithoutTokenAndCookie("https://playground.learnqa.ru/api/user/"+userId);
        Assertions.assertResponseTextEquals(responseDeleteUser, "Auth token not supplied");
    }
}
