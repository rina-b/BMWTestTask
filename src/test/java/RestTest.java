import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.Address;
import models.UserPOJO;
import org.apache.commons.io.output.WriterOutputStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import util.HibernateUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RestTest {
    Charset utf8 = StandardCharsets.UTF_8;

    @Test
    public void getTest() {

        try (FileWriter fileWriter = new FileWriter("logging.txt");
             PrintStream printStream = new PrintStream(new WriterOutputStream(fileWriter, utf8), true)) {
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(printStream));

            List<UserPOJO> users = given().
                    when().get("https://jsonplaceholder.typicode.com/users").
                    then().assertThat().statusCode(200).log().headers().log().status()
                    .and().extract().jsonPath().getList("", UserPOJO.class);
            assertEquals(10, users.size());

            for (UserPOJO user: users){
                assertThat(user.getEmail(), matchesPattern("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"));

                SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
                Session session = sessionFactory.openSession();
                session.beginTransaction();
                session.save(user);
                session.getTransaction().commit();
                session.close();
                sessionFactory.close();
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void postTest() {
        try (FileWriter fileWriter = new FileWriter("logging.txt");
             PrintStream printStream = new PrintStream(new WriterOutputStream(fileWriter, utf8), true)) {
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(printStream));
            RequestSpecification request = RestAssured.given();
            UserPOJO body = new UserPOJO();
            request.contentType(ContentType.JSON);
            body.setName("Test");
            body.setPhone("14576534");
            body.setAddress(new Address());
            request.body(body, ObjectMapperType.JACKSON_2)
                    .post("https://jsonplaceholder.typicode.com/users").then().assertThat().statusCode(201)
                    .log().headers().log().status();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteTest() {
        try (FileWriter fileWriter = new FileWriter("logging.txt");
             PrintStream printStream = new PrintStream(new WriterOutputStream(fileWriter, utf8), true)) {
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(printStream));
            Response response = given()
                    .header("Content-type", "application/json")
                    .when()
                    .delete("https://jsonplaceholder.typicode.com/users/1")
                    .then().assertThat().statusCode(200)
                    .log().headers().log().status()
                    .extract().response();
            System.out.println(response);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void putTest() {
        try (FileWriter fileWriter = new FileWriter("logging.txt");
             PrintStream printStream = new PrintStream(new WriterOutputStream(fileWriter, utf8), true)) {
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(printStream));

            String requestBody = "{\n"
                    + "    \"name\": \"Adam\",\n"
                    + "    \"username\": \"Java Developer\"\n"
                    + "}";


            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .put("https://jsonplaceholder.typicode.com/users/9")
                    .then().assertThat().statusCode(200)
                    .log().headers().log().status()
                    .extract().response();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
