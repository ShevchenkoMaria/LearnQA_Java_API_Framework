package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.Assertions;
import lib.BaseTestCase;
import lib.ApiCoreRequests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Epic("Authorisation cases")
@Feature("Authorisation")
public class UserAuthTestWithSteps extends BaseTestCase {
    String cookie;
    String header;
    int userIdAuth;
    private final ApiCoreRequests apiCoreRequests= new ApiCoreRequests();
    @BeforeEach
    public void loginUser(){
        Map<String,String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = apiCoreRequests
                .makePostRequest("https://playground.learnqa.ru/api/user/login",authData);
        this.cookie = this.getCookie(responseGetAuth,"auth_sid");
        this.header = this.getHeader(responseGetAuth,"x-csrf-token");
        this.userIdAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    @Description("This test successfully authrize user by email and password")
    @DisplayName("Test positive auth user")
    public void testAuthUser(){
        Response responseCheckAuth = apiCoreRequests
                .makeGetRequest("https://playground.learnqa.ru/api/user/auth", this.header, this.cookie);
        Assertions.assertJsonByName(responseCheckAuth,"user_id", this.userIdAuth);
    }
    @Description("This test check authrization status sending auth cookie or token")
    @DisplayName("Test negative auth user")
    @ParameterizedTest
    @ValueSource(strings = {"cookies", "headers"})
    public void testNegativeAuthUser(String condition) {
        RequestSpecification spec = RestAssured.given();
        spec.baseUri("https://playground.learnqa.ru/api/user/auth");

        if(condition.equals("cookies")){
            Response responseForCheck = apiCoreRequests.makeGetRequestwithCookie("https://playground.learnqa.ru/api/user/auth", this.cookie);
            Assertions.assertJsonByName(responseForCheck,"user_id",0);
        } else if (condition.equals("headers")){
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken("https://playground.learnqa.ru/api/user/auth", this.header);
            Assertions.assertJsonByName(responseForCheck,"user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition value is known " + condition);
        }
    }
}