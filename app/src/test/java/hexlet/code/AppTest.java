package hexlet.code;

import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AppTest {

    private static Javalin app;
    private static String url;
    private static Transaction transaction;
    private static MockWebServer server1;
    private static MockWebServer server2;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        url = "http://localhost:" + port;

        server1 = new MockWebServer();
        server2 = new MockWebServer();

        String pageAll = Files.readString(Path.of("src/test/resources/test1.html"));
        String pageSome = Files.readString(Path.of("src/test/resources/test2.html"));

        server1.enqueue(new MockResponse().setBody(pageAll));
        server2.enqueue(new MockResponse().setBody(pageSome));

        server1.start();
        server2.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        server1.shutdown();
        server2.shutdown();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }
}
