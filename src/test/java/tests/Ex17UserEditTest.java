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
@Feature("Checking PUT-requests for edit user")
@Issue("PUT-requests")
public class Ex17UserEditTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests= new ApiCoreRequests();
    @Test
    @Description("This test successfully edit user's 'firstName'")
    @DisplayName("Test positive edit user")
    @Severity(CRITICAL)
    public void testEditUserAuthAsSameUser() {
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

        //EDIT USER
        String newName = DataGenerator.getRandomString();
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);
        Response responseEditUser = apiCoreRequests
                .makePutRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"),
                        editData);

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }
    @Test
    @Description("This test verifies that user data cannot be changed if the request is made by an unauthorized user")
    @DisplayName("Test negative edit user without auth")
    @Severity(NORMAL)
    public void testEditUserWithoutAuth(){
        //GENERATE USER
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        String userId = responseCreateAuth.jsonPath().getString("id");

        //EDIT USER
        String newName = "Changed Name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);
        Response responseEditUser = apiCoreRequests
                .makePutRequestWithoutTokenAndCookie("https://playground.learnqa.ru/api/user/"+userId,
                        editData);
        Assertions.assertResponseTextEquals(responseEditUser, "Auth token not supplied");
    }
    @Test
    @Description("The test verifies that one user's data cannot be changed if the request is performed by another user, in this case the base one")
    @DisplayName("Test negative edit user with auth as based user")
    @Severity(MINOR)
    public void testEditUserAuthAsBasedUser(){
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

        //EDIT USER
        String newEmail = "Changed Email";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);
        Response responseEditUser = apiCoreRequests
                .makePutRequest("https://playground.learnqa.ru/api/user/"+userId, header, cookie, editData);
        Assertions.assertResponseTextEquals(responseEditUser, "Please, do not edit test users with ID 1, 2, 3, 4 or 5.");
    }

    @Test
    @Description("The test verifies that one user's data cannot be changed if the request is performed by another user, in this case both users are created")
    @DisplayName("Test negative edit user with auth as another user")
    @Severity(MINOR)
    public void testEditUserAuthAsAnotherUser(){
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

        //EDIT USER№2
        String newEmail = DataGenerator.getRandomEmail();
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);
        Response responseEditUser = apiCoreRequests
                .makePutRequest("https://playground.learnqa.ru/api/user/"+ userId2,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"),
                        editData);

        //GET FOR USER#1
        Response responseUser1Data = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/" + userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUser1Data, "email", newEmail);

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
        Assertions.assertJsonByName(responseUser2Data, "email", userData2.get("email"));
    }
    @Test
    @Description("This test verifies that the 'email' cannot be changed if the format is set incorrectly")
    @DisplayName("Test negative edit user with wrong email format")
    @Severity(MINOR)
    public void testEditUserWithWringData() {
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

        //EDIT USER
        String newEmail = DataGenerator.getRandomString();
        System.out.println("newEmail "+ newEmail);
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);
        Response responseEditUser = apiCoreRequests
                .makePutRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"),
                        editData);

        Assertions.assertResponseTextEquals(responseEditUser, "Invalid email format");

        //GET
        Response responseUserData = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "email", userData.get("email"));
    }
    @Test
    @Description("This test verifies that the 'firstName' cannot be changed if is too short")
    @DisplayName("Test negative edit user with short firstName")
    @Severity(MINOR)
    public void testEditUserWithShortFirstname() {
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

        //EDIT USER
        String newName ="O";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);
        Response responseEditUser = apiCoreRequests
                .makePutRequest("https://playground.learnqa.ru/api/user/"+userId,
                        this.getHeader(responseGetAuth, "x-csrf-token"),
                        this.getCookie(responseGetAuth, "auth_sid"),
                        editData);
        Assertions.assertJsonByName(responseEditUser, "error", "Too short value for field firstName");
    }
}
