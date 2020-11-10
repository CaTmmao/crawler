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
import java.sql.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        String indexUrl = "https://sina.cn";

        try (Connection connection = DriverManager.getConnection("jdbc:h2:file:/home/catmmao/文档/javalearn/crawler/news")) {
            String currentUrl;
            while ((currentUrl = findUnUsedLinkFromDatabase(connection)) != null) {
                Document document = httpGetAndReturnHtml(currentUrl);

                deleteLinksInDatabase(connection, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link=?", currentUrl);
                insertLinksToDatabase(connection, "INSERT INTO LINKS_ALREADY_PROCESSED  (LINK) values(?)", currentUrl);
                addNeedLinksToDatabase(indexUrl, document, connection);
                storeArticleInfoIntoDatabase(document);
            }
        }
    }

    private static String findUnUsedLinkFromDatabase(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM LINKS_TO_BE_PROCESSED LIMIT 1");
             ResultSet unusedLink = statement.executeQuery()) {
            if (unusedLink.next()) {
                return unusedLink.getString("link");
            }
        }

        return null;
    }

    private static void insertLinksToDatabase(Connection connection, String sql, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    private static void deleteLinksInDatabase(Connection connection, String sql, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    private static void addNeedLinksToDatabase(String indexUrl, Document document, Connection connection) throws SQLException {
        ArrayList<Element> links = document.select("a");

        for (Element link : links) {
            String href = link.attr("href");

            if (!isProcessedThisLink(connection, href) && ifInterestLink(indexUrl, href)) {
                insertLinksToDatabase(connection, "INSERT INTO LINKS_TO_BE_PROCESSED (LINK) values(?)", href);
            }
        }
    }

    private static boolean isProcessedThisLink(Connection connection, String href) throws SQLException {
        ResultSet resultSet = null;

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM LINKS_TO_BE_PROCESSED WHERE LINK=?")) {
            statement.setString(1, href);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }

        return false;
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
