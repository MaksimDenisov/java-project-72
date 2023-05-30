package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class UrlController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);

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
        ctx.redirect("/");
    };

    public static Handler getUrls = ctx -> {
        LOGGER.info("Get all urls.");
        //TODO Get all
        PagedList<Url> pagedArticles = new QUrl()
                .setFirstRow(0)
                .setMaxRows(1000)
                .orderBy()
                .id.asc()
                .findPagedList();
        List<Url> urls = pagedArticles.getList();
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
        HttpResponse<String> response = Unirest.post(Objects.requireNonNull(url).getName()).asString();
        UrlCheck check = new UrlCheck();
        check.setStatusCode(response.getStatus());
        check.setUrl(url);
        LOGGER.info("{}, status code = {}.", url.getName(), response.getStatus());
        check.save();
        ctx.redirect("/urls/" + url.getId());
    };
}
