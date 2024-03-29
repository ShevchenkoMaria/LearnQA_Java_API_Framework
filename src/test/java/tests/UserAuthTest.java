package tests;

import io.qameta.allure.Severity;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;

import static io.qameta.allure.SeverityLevel.*;

@Epic("Authorisation cases")
@Feature("Authorisation")
public class UserAuthTest extends BaseTestCase {
    String cookie;
    String header;
    int userIdAuth;
    @BeforeEach
    public void loginUser(){
        Map<String,String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");
        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post("https://playground.learnqa.ru/api/user/login")
                .andReturn();
        this.cookie = this.getCookie(responseGetAuth,"auth_sid");
        this.header = this.getHeader(responseGetAuth,"x-csrf-token");
        this.userIdAuth = this.getIntFromJson(responseGetAuth,"user_id");
    }

    @Test
    @Description("This test successfully authorize user by email and password")
    @DisplayName("Test positive auth user")
    @Severity(CRITICAL)
    public void testAuthUser(){
        Response responseCheckAuth = RestAssured
                .given()
                .header("x-csrf-token",this.header)
                .cookie("auth_sid", this.cookie)
                .get("https://playground.learnqa.ru/api/user/auth")
                .andReturn();
        Assertions.assertJsonByName(responseCheckAuth,"user_id", this.userIdAuth);
    }

    @ParameterizedTest (name = "{displayName} ({argumentsWithNames})")
    @ValueSource(strings = {"cookies", "headers"})
    @Description("This test check authrization status sending auth cookie or token")
    @DisplayName("Test negative auth user")
    @Severity(NORMAL)
    public void testNegativeAuthUser(String condition) {
        RequestSpecification spec = RestAssured.given();
        spec.baseUri("https://playground.learnqa.ru/api/user/auth");
        if(condition.equals("cookies")){
            spec.cookie("auth_sid",this.cookie);
        } else if (condition.equals("headers")){
            spec.header("x-csrf-token",this.header);
        } else {
            throw new IllegalArgumentException("Condition value is known " + condition);
        }
        Response responseForCheck = spec.get().andReturn();
        Assertions.assertJsonByName(responseForCheck,"user_id", 0);
    }
}