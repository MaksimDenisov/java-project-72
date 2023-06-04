package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlControllerTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public final void setUp() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    public void init() {
        assertThat(true).isEqualTo(true);
    }

    @Test
    @DisplayName("The title page showed.")
    public void shouldShowIndexPage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }

    @Test()
    @DisplayName("The required entity added to the database and is displayed on the page")
    public void shouldAddUrl() {
        String name = "https://music.yandex.ru";
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Страница успешно добавлена");

        Url url = new QUrl()
                .name.equalTo(name)
                .findOne();
        assertThat(url).isNotNull();
        assertThat(url.getName()).isEqualTo(name);
    }

    @Test()
    @DisplayName("The exist url not added.")
    public void shouldNotAddExistUrl() {
        String name = "https://music.yandex.ru";
        new Url(name).save();
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Страница уже существует");
    }

    @Test()
    @DisplayName("The incorrect url not added.")
    public void shouldNotAddIncorrectUrl() {
        String name = "incorrect url";
        new Url(name).save();
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Некорректный URL");
    }

    @Test
    @DisplayName("The page with list of urls showed.")
    public void shouldShowUrlsPage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://ru.hexlet.io");
        assertThat(body).contains("https://ya.ru");
    }

    @Test
    @DisplayName("The page with one url showed.")
    public void shouldShowUrlPage() {
        String name = "https://ru.hexlet.io";
        Url url = new QUrl()
                .name.equalTo(name)
                .findOne();
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + url.getId())
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains(name);
        assertThat(body).doesNotContain("https://ya.ru");
    }

    @Test
    @DisplayName("The url check added.")
    public void shouldCheckUrl() throws IOException {
        MockWebServer server = new MockWebServer();
        String mockPage = server.url("/").toString();
        MockResponse mockResponse = new MockResponse().setResponseCode(200).setBody("");
        server.enqueue(mockResponse);
        new Url(mockPage).save();
        Url url = new QUrl()
                .name.equalTo(mockPage)
                .findOne();
        HttpResponse<String> postResponse = Unirest
                .post(baseUrl + "/urls/" + url.getId() + "/checks")
                .asString();

        assertThat(postResponse.getStatus()).isEqualTo(302);

        HttpResponse<String> getResponse = Unirest
                .get(baseUrl + "/urls/" + url.getId())
                .asString();
        assertThat(getResponse.getStatus()).isEqualTo(200);
        assertThat(getResponse.getBody()).contains("<td>200</td>");
        server.shutdown();
    }
}
