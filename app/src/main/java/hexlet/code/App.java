package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static int getPort() {
        String port = System.getenv("PORT");
        if (port != null) {
            return Integer.valueOf(port);
        }
        return 5000;
    }


    public static void main(String[] args) {
        LOGGER.info("Start");
        App.getApp().start(getPort());
    }

    public static Javalin getApp() {
        LOGGER.debug("Get Javalin app.");
        return Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"));
    }
}
