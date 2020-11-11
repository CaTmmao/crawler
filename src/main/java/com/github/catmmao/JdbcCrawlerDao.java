package com.github.catmmao;

import java.sql.*;

public class JdbcCrawlerDao implements CrawlerDao {
    Connection connection = DriverManager.getConnection("jdbc:h2:file:/home/catmmao/文档/javalearn/crawler/news", "root", "111111");

    public JdbcCrawlerDao() throws SQLException {
    }

    public void insertLinksToDatabase(String sql, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    public void deleteLinksInDatabase(String sql, String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    public String findUnUsedLinkFromDatabase() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM LINKS_TO_BE_PROCESSED LIMIT 1");
             ResultSet unusedLink = statement.executeQuery()) {
            if (unusedLink.next()) {
                return unusedLink.getString("link");
            }
        }

        return null;
    }

    public boolean isProcessedThisLink(String href) throws SQLException {
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

    public void insertNewsToDatabase(String title, String content, String url) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO NEWS  (title, content, url, created_at, updated_at) values(?, ?, ?, now(), now())")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, url);
            statement.executeUpdate();
        }
    }
}
