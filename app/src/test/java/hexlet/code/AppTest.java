package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;
    private static MockWebServer server;
    private static final int OK = 200;
    private static final int REDIRECT = 302;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        server = new MockWebServer();
        String pageAll = Files.readString(Path.of("src/test/resources/test1.html"));
        server.enqueue(new MockResponse().setBody(pageAll));
        server.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        server.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest
                .get(baseUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(OK);
        assertThat(response.getBody()).contains("Анализатор страниц");
    }

    @Test
    void testRootURLS() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();

        assertThat(response.getStatus()).isEqualTo(OK);
        assertThat(response.getBody()).contains("Page analyzer");
    }

    @Test
    void testUrl() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/2")
                .asString();

        assertThat(response.getStatus()).isEqualTo(OK);
        assertThat(response.getBody()).contains("https://www.railway.app");
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();

        assertThat(response.getStatus()).isEqualTo(OK);
        assertThat(response.getBody()).contains("https://www.github.com");
        assertThat(response.getBody()).contains("https://www.railway.app");
    }

    @Test
    void testRedirectUrl() {
        final String testRedirect = "https://www.youtube.com/c/google";

        HttpResponse response = Unirest
                .post(baseUrl + "/urls")
                .field("url", testRedirect)
                .asEmpty();

        assertThat(response.getStatus()).isEqualTo(REDIRECT);
        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls");
    }

    @Test
    void testSuccessAddUrlAndPageAlreadyExist() {
        final String testAdd = "https://www.youtube.com";
        final String parsedTestAdd = "www.youtube.com";

        Unirest
                .post(baseUrl + "/urls")
                .field("url", testAdd)
                .asEmpty();

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls")
                .asString();

        assertThat(response.getBody()).contains(parsedTestAdd);
        assertThat(response.getBody()).contains("Страница успешно добавлена");

        Unirest
                .post(baseUrl + "/urls")
                .field("url", testAdd)
                .asEmpty();

        HttpResponse<String> responseWithAlreadyAdd = Unirest
                .get(baseUrl + "/urls")
                .asString();
        String body = responseWithAlreadyAdd.getBody();

        assertThat(body).contains("Страница уже существует");
        assertThat(body).contains(parsedTestAdd);
    }

    @Test
    void testIncorrectAddUrl() {
        final String incorrectUrl = "trompompom.com";

        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", incorrectUrl)
                .asString();

        assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> response1 = Unirest
                .get(baseUrl)
                .asString();

        assertThat(response1.getBody()).contains("Некорректный URL");
    }

    @Test
    void testChecksSiteExist() {
        String testServerSite = server.url("/").toString();
        String testPageServer = testServerSite.substring(0, testServerSite.length() - 1);

        Unirest
                .post(baseUrl + "/urls")
                .field("url", testServerSite)
                .asString();

        Url actualSite = new QUrl()
                .name.equalTo(testPageServer)
                .findOne();

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/" + (actualSite != null ? actualSite.getId() : 0))
                .asString();

        assertThat(response.getStatus()).isEqualTo(OK);
        assertThat(response.getBody()).contains(testPageServer);
    }

    @Test
    void testSetCheckTestPageServer() {
        String testServerSite = server.url("/").toString();
        String testPageServer = testServerSite.substring(0, testServerSite.length() - 1);

        Unirest
                .post(baseUrl + "/urls")
                .field("url", testServerSite)
                .asString();

        Url actualSite = new QUrl()
                .name.equalTo(testPageServer)
                .findOne();

        Unirest
                .post(baseUrl + "/urls/" + (actualSite != null ? actualSite.getId() : 0) + "/checks")
                .asString();

        String checkTestPageServer = Unirest
                .get(baseUrl + "/urls/" + (actualSite != null ? actualSite.getId() : 0))
                .asString()
                .getBody();

        final String expectedDescriptionTest = "Description Test";
        final String expectedTitleTest = "Test title";
        final String expectedH1Test = "Test body";

        assertThat(checkTestPageServer).contains(expectedDescriptionTest);
        assertThat(checkTestPageServer).contains(expectedTitleTest);
        assertThat(checkTestPageServer).contains(expectedH1Test);
    }
}
