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
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        String indexUrl = "https://sina.cn";
        List<String> unusedLinks = new ArrayList<>();
        unusedLinks.add(indexUrl);

        List<String> usedLinks = new ArrayList<>();

        while (!unusedLinks.isEmpty()) {
            try {
                int unUsedLinksLastIndex = unusedLinks.size() - 1;
                String currentUrl = unusedLinks.get(unUsedLinksLastIndex);
                Document document = httpGetAndReturnHtml(currentUrl);
                usedLinks.add(unusedLinks.remove(unUsedLinksLastIndex));

                addNeedLinks(indexUrl, document, usedLinks, unusedLinks);
                storeArticleInfoIntoDatabase(document);
            } catch (IOException e) {
                throw new IOException(e);
            }
        }
    }

    private static void addNeedLinks(String indexUrl, Document document, List<String> usedLinks, List<String> unusedLinks) {
        ArrayList<Element> links = document.select("a");
        for (Element link : links) {
            String href = link.attr("href");

            if (!usedLinks.contains(href)) {
                if (ifInterestLink(indexUrl, href)) {
                    unusedLinks.add(href);
                }
            }
        }
    }

    private static void storeArticleInfoIntoDatabase(Document document) {
        Elements articles = document.select("article");
        if (!articles.isEmpty()) {
            System.out.println(articles.select("h1").text());
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
