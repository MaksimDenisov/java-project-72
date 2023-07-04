package hexlet.code.services;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UrlService {
    private static final int PAGE_SIZE = 10;

    public static List<Map<String, Object>> getPage(int page) {
        return new QUrl()
                .setFirstRow(--page * PAGE_SIZE)
                .setMaxRows(PAGE_SIZE)
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
    }

    public static int getPageCount() {
        return new QUrl().findCount() / 10 + 1;
    }

    public static Url getUrlById(long id) {
        return new QUrl()
                .id.equalTo(id)
                .findOne();
    }

    public static boolean addUrl(String paramUrl) throws MalformedURLException {
        URL url = new URL(Objects.requireNonNull(paramUrl));
        String port = ((url.getPort() != -1) ? (":" + url.getPort()) : "");
        Url urlEntity = new Url(String.format("%s://%s%s", url.getProtocol(), url.getHost(), port));
        Url existUrl = new QUrl()
                .name.equalTo(urlEntity.getName())
                .findOne();
        if (existUrl != null) {
            return false;
        }
        urlEntity.save();
        return true;
    }

    public static Url checkUrlById(long id) {
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        if (url == null) {
            throw new IllegalArgumentException("Can't found entity with id = " + id);
        }
        UrlCheck check = UrlService.checkUrl(url);
        check.setUrl(url);
        check.save();
        return url;
    }

    private static UrlCheck checkUrl(Url url) {
        HttpResponse<String> response;
        response = Unirest.get(Objects.requireNonNull(url).getName()).asString();
        int statusCode = response.getStatus();
        Document document = Jsoup.parse(response.getBody());
        String title = document.title();
        String h1 = Optional.ofNullable(document.selectFirst("h1"))
                .map(Element::text).orElse("");
        String description = Optional.ofNullable(document.selectFirst("meta[name=description]"))
                .map(element -> element.attr("content")).orElse("");
        return new UrlCheck(statusCode, title, h1, description);
    }
}
