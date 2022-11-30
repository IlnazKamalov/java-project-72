package hexlet.code;

import io.javalin.Javalin;

public class App {

    public static void main(String[] args) {
        Javalin app = getApp();
        int port = getPort();
        app.start(port);
    }

    public static Javalin getApp() {
        return Javalin.create()
                .get("/", ctx -> ctx.result("Hello, World!"));
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "1313");
        return Integer.parseInt(port);
    }
}
