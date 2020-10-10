import io.restassured.http.ContentType;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class CovidUsersTest {

  final String ENDPOINT = "https://supervillain.herokuapp.com/v1/user";

  @Test
  public void get_users_should_be_descending() {
    String json =
      when().
        get(ENDPOINT).
      then().
        statusCode(200).
        extract().response().asString();

    JSONArray users = new JSONArray(json);

    // Compare the score of each user to the one next to it in the array
    for (int i = 0; i < users.length() - 1; i++) {
      Assert.assertTrue(users.getJSONObject(i).getInt("score") >= users.getJSONObject(i + 1).getInt("score"));
    }
  }

  @Test
  public void post_valid_add_user() {
    Random random = new Random();
    String validUser = "{\"username\":\"test" + random.nextInt(99999999) + "\",\"score\":100}";

    given().
      contentType(ContentType.JSON).
      body(validUser).
    when().
      post(ENDPOINT).
    then().
      statusCode(201).
      body("status", equalTo("success")).
      body("message", equalTo("User added."));
  }

  @Test
  public void post_invalid_add_user() {
    Random random = new Random();
    // Pass a String for score instead of an integer so it is invalid
    String invalidValidUser = "{\"username\":\"test" + random.nextInt(99999999) + "\",\"score\":\"onemillion\"}";

    given().
      contentType(ContentType.JSON).
      body(invalidValidUser).
    when().
      post(ENDPOINT).
    then().
      statusCode(400).
      body("error", containsString("invalid input syntax for integer: \"onemillion\""));
  }

  @Test
  public void post_already_existing_user() {
    String existingUser = "{\"username\":\"Lewis\",\"score\":100000}";

    given().
      contentType(ContentType.JSON).
      body(existingUser).
    when().
      post(ENDPOINT).
    then().
      statusCode(400).
      body("error", containsString("duplicate key value violates unique constraint \"leaderboard_username_key\""));
  }

  @Test
  public void put_update_user() {
    String existingUser = "{\"username\":\"Lewis\",\"score\":490}";

    given().
      contentType(ContentType.JSON).
      body(existingUser).
    when().
      put(ENDPOINT).
    then().
      statusCode(204);
  }
}
