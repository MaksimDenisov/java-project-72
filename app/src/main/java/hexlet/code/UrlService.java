package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Objects;
import java.util.Optional;

public class UrlService {

    public static UrlCheck checkUrl(Url url) {
        HttpResponse<String> response = Unirest.post(Objects.requireNonNull(url).getName()).asString();
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
