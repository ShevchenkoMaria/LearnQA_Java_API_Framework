package tests;

import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

public class Ex15UserRegisterTest extends BaseTestCase {
    private final ApiCoreRequests apiCoreRequests= new ApiCoreRequests();
    @Test
    public void testCreateUserWithExistingEmail() {
        String email = "vinkotov@example.com";
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        Assertions.assertResponseCodeEquals(responseCreateUser, 400);
        Assertions.assertResponseTextEquals(responseCreateUser, "Users with email '" + email +"' already exists");
    }
    @Test
    public void testCreateUserWithNewEmail() {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        Assertions.assertResponseCodeEquals(responseCreateUser, 200);
        Assertions.assertJsonHasField(responseCreateUser,"id");
    }
    @Test
    public void testCreateUserWithUncorrectEmail() {
        String email = "vinkotovexample.com";
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);

        Assertions.assertResponseCodeEquals(responseCreateUser, 400);
        Assertions.assertResponseTextEquals(responseCreateUser, "Invalid email format");
    }
    @Test
    public void testCreateUserWithShortName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", "l");
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        Assertions.assertResponseCodeEquals(responseCreateUser, 400);
        Assertions.assertResponseTextEquals(responseCreateUser, "The value of 'username' field is too short");
    }
    @Test
    public void testCreateUserWithLongName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("username", "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIWWWWWWWWWWWWWWWWWIIIIIIIIIIIIIIIIIIIIIIIIIIIIII" +
                "SSSSSSSSSSSSSSAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPPPPPPPPPPPPPPPPDDDDDDDDDDDDDDDDDaaaaaaaaaaaaaaaaaaaaaa" +
                "ddddddddddddddddddddddffffffffffffffffffffffffNNNNNNNNNNNNNNNNNNNNN");
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        Assertions.assertResponseCodeEquals(responseCreateUser, 400);
        Assertions.assertResponseTextEquals(responseCreateUser, "The value of 'username' field is too long");
    }
    @ParameterizedTest
    @ValueSource(strings = {"email","password","username", "firstName", "lastName"})
    public void testCreateUserWithoutOneField(String field) {
        Map<String, String> userData = new HashMap<>();
        userData = DataGenerator.getRegistrationData(userData);
        userData.remove(field);
        Response responseCreateUser = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/",userData);
        Assertions.assertResponseCodeEquals(responseCreateUser, 400);
        Assertions.assertResponseTextEquals(responseCreateUser, "The following required params are missed: "+field);
    }
}
