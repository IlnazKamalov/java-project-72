package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import io.javalin.Javalin;
import io.javalin.core.validation.ValidationException;
import io.javalin.http.HttpCode;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {

    public static void main(String[] args) {
        Javalin app = getApp();
        int port = getPort();
        app.start(port);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        app.exception(ValidationException.class, (error, ctx) ->
                ctx.json(error.getErrors()).status(HttpCode.UNPROCESSABLE_ENTITY));

        addRoutes(app);

        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "1313");
        return Integer.parseInt(port);
    }

    private static TemplateEngine getTemplateEngine() {
        ClassLoaderTemplateResolver classLoaderTemplateResolver = new ClassLoaderTemplateResolver();
        classLoaderTemplateResolver.setPrefix("/templates/");
        classLoaderTemplateResolver.setSuffix(".html");

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(classLoaderTemplateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
        app.routes(() -> path("urls", () -> {
            post(UrlController.addUrl);
            get(UrlController.showUrls);
            get("{id}", UrlController.showUrl);
            post("{id}/checks", UrlController.checkUrl);
        }));
    }
}
