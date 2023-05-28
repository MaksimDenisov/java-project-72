package hexlet.code.controllers;

import hexlet.code.App;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlControllerTest {

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
    void setUp() {
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void init() {
        assertThat(true).isEqualTo(true);
    }

    @Test
    @DisplayName("The title page has been showed.")
    void shouldShowIndexPage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }

    @Test()
    @DisplayName("The required entity has been added to the database and is displayed on the page")
    void shouldAddUrl() {
        String name = "https://music.yandex.ru";
        HttpResponse responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", name)
                .asEmpty();
        assertThat(responsePost.getStatus()).isEqualTo(302);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/")
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

    @Test
    @DisplayName("The page with list of urls has been showed.")
    void shouldShowUrlsPage() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = response.getBody();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(body).contains("https://ru.hexlet.io");
        assertThat(body).contains("https://ya.ru");
    }

    @Test
    @DisplayName("The page with one url has been showed.")
    void shouldShowUrsPage() {
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
}
