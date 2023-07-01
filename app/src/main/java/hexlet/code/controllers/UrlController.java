package hexlet.code.controllers;

import hexlet.code.services.UrlService;
import hexlet.code.domain.Url;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class UrlController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

    public static Handler addUrl = ctx -> {
        String paramUrl = ctx.formParam("url");
        try {
            if (UrlService.addUrl(paramUrl)) {
                LOGGER.info("{} added", paramUrl);
                ctx.sessionAttribute("flash", "Страница успешно добавлена");
            } else {
                LOGGER.info("{} has not been added because this is an exist URL.", paramUrl);
                ctx.sessionAttribute("flash", "Страница уже существует");
            }
        } catch (Exception e) {
            LOGGER.info("{} has not been added because this is an incorrect URL.", paramUrl);
            ctx.sessionAttribute("flash", "Некорректный URL");
        }
        ctx.redirect("/urls");
    };

    public static Handler getUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        LOGGER.info("Get urls page {}.", page);
        int pageCount = UrlService.getPageCount();
        if (page < 1 || page > pageCount) {
            ctx.redirect("/urls");
            return;
        }
        List<Map<String, Object>> urls = UrlService.getPage(page);
        ctx.attribute("currentPage", page);
        ctx.attribute("urls", urls);
        ctx.attribute("pageCount", pageCount);
        ctx.render("urls.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        LOGGER.info("Show url with id {}.", id);
        Url url = UrlService.getUrlById(id);
        ctx.attribute("url", url);
        ctx.render("url.html");
    };

    public static Handler addCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        LOGGER.info("Start check url with id {}.", id);
        Url url = UrlService.checkUrlById(id);
        if (url != null) {
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.redirect("/urls/" + url.getId());
        } else {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/urls/");
        }
    };
}
