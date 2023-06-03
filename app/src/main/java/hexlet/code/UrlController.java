package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UrlController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);
    public static Handler index = ctx -> {
        ctx.render("index.html");
    };
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy hh:mm");

    public static Handler addUrl = ctx -> {
        String paramUrl = ctx.formParam("url");
        try {
            URL url = new URL(Objects.requireNonNull(paramUrl));
            String port = ((url.getPort() != -1) ? (":" + url.getPort()) : "");
            Url urlEntity = new Url(String.format("%s://%s%s", url.getProtocol(), url.getHost(), port));
            Url existUrl = new QUrl()
                    .name.equalTo(urlEntity.getName())
                    .findOne();
            if (existUrl != null) {
                LOGGER.info("{} has not been added because this is an exist URL.", paramUrl);
                ctx.sessionAttribute("flash", "Страница уже существует");
            } else {
                urlEntity.save();
                LOGGER.info("{} added", url);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
            }
        } catch (Exception e) {
            LOGGER.info("{} has not been added because this is an incorrect URL.", paramUrl);
            ctx.sessionAttribute("flash", "Некорректный URL");
        }
        ctx.redirect("/urls");
    };

    public static Handler getUrls = ctx -> {
        LOGGER.info("Get all urls.");
        List<Map<String, Object>> urls = new QUrl()
                .orderBy()
                .id.asc()
                .findList()
                .stream()
                .map(url -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", url.getId());
                    map.put("name", url.getName());
                    UrlCheck lastCheck = url.getUrlChecks().stream()
                            .max(Comparator.comparing(UrlCheck::getCreatedAt)).orElse(null);
                    map.put("lastDate", (lastCheck != null) ? lastCheck.getCreatedAt() : null);
                    map.put("lastStatus", (lastCheck != null) ? lastCheck.getStatusCode() : "");
                    return map;
                }).collect(Collectors.toList());
        ctx.attribute("urls", urls);
        ctx.render("urls.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        LOGGER.info("Show url with id {}.", id);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        ctx.attribute("url", url);
        ctx.render("url.html");
    };

    public static Handler addCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        LOGGER.info("Start check url with id {}.", id);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        UrlCheck check = UrlService.checkUrl(url);
        check.setUrl(url);
        check.save();
        ctx.redirect("/urls/" + url.getId());
    };
}
