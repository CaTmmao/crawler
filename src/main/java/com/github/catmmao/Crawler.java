package com.github.catmmao;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {
    CrawlerDao dao = new JdbcCrawlerDao();

    public Crawler() throws SQLException {
    }

    public void run() throws SQLException, IOException {
        String indexUrl = "https://sina.cn";

        String currentUrl;
        while ((currentUrl = dao.findUnUsedLinkFromDatabase()) != null) {
            Document document = httpGetAndReturnHtml(currentUrl);

            dao.deleteLinksInDatabase("DELETE FROM LINKS_TO_BE_PROCESSED WHERE link=?", currentUrl);
            dao.insertLinksToDatabase("INSERT INTO LINKS_ALREADY_PROCESSED  (LINK) values(?)", currentUrl);
            addNeedLinksToDatabase(indexUrl, document);
            storeArticleInfoIntoDatabase(document, currentUrl);
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }


    private void addNeedLinksToDatabase(String indexUrl, Document document) throws SQLException {
        ArrayList<Element> links = document.select("a");

        for (Element link : links) {
            String href = link.attr("href");

            if (!dao.isProcessedThisLink(href) && ifInterestLink(indexUrl, href)) {
                dao.insertLinksToDatabase("INSERT INTO LINKS_TO_BE_PROCESSED (LINK) values(?)", href);
            }
        }
    }


    private void storeArticleInfoIntoDatabase(Document document, String url) throws SQLException {
        Elements articles = document.select("article");
        if (!articles.isEmpty()) {
            String title = articles.select("h1").text();
            String content = articles.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
            dao.insertNewsToDatabase(title, content, url);
        }
    }

    private static boolean ifInterestLink(String indexUrl, String href) {
        return ((href.contains("news.sina.cn") || href.equals(indexUrl)) && !href.contains("passport"));
    }

    private static Document httpGetAndReturnHtml(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");

        try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
            HttpEntity entity = response.getEntity();
            return Jsoup.parse(EntityUtils.toString(entity));
        }
    }
}
