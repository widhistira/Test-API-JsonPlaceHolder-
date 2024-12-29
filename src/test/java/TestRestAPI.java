import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class TestRestAPI {
    @Test
    public void testAPI() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";

        // Step 1: Get a random user Id and email
        Response usersResponse = given()
                .get("/users")
                .then()
                .statusCode(200)
                .extract().response();

        List<Integer> userIds = usersResponse.jsonPath().getList("id");
        List<String> emails = usersResponse.jsonPath().getList("email");

        Random random = new Random();
        int randomIndex = random.nextInt(userIds.size());
        int randomUserId = userIds.get(randomIndex);
        String userEmail = emails.get(randomIndex);

        System.out.println("Random User ID: " + randomUserId);
        System.out.println("User Email: " + userEmail);

        // Step 2: Get Validation Post User ID
        Response postsResponse = given()
                .queryParam("userId", randomUserId)
                .get("/posts")
                .then()
                .statusCode(200)
                .extract().response();

        List<Integer> postIds = postsResponse.jsonPath().getList("id");

        for (int postId : postIds) {
            Assert.assertTrue(postId >= 1 && postId <= 100, "Post ID " + postId + " is invalid.");
        }
        System.out.println("All post ID are valid for userID " + randomUserId);

        // Step 3: Create a post using the same userID
        String title = "Test Post";
        String body = "Test Body";

        Response createPostResponse = given()
                .header("Content-type", "application/json")
                .body("{\n" +
                        "  \"userId\": " + randomUserId + ",\n" +
                        "  \"title\": \"" + title + "\",\n" +
                        "  \"body\": \"" + body + "\"\n" +
                        "}")
                .post("/posts")
                .then()
                .extract().response();

        int statusCode = createPostResponse.getStatusCode();
        String responseBody = createPostResponse.getBody().asString();

        System.out.println("Create Post Response Code: " + statusCode);
        System.out.println("Response Body: " + responseBody);

        Assert.assertTrue(statusCode == 201, "Unexpected status code: " + statusCode);
        Assert.assertTrue(responseBody.contains(title));
        Assert.assertTrue(responseBody.contains(body));
        Assert.assertTrue(responseBody.contains(String.valueOf(randomUserId)));

        System.out.println("Post created successfully for userID " + randomUserId);
    }
}
