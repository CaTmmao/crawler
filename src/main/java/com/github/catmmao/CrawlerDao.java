package com.github.catmmao;

import java.sql.Connection;
import java.sql.SQLException;

public interface CrawlerDao {
    Connection connection = null;

    void insertLinksToDatabase(String sql, String href) throws SQLException;

    void deleteLinksInDatabase(String sql, String href) throws SQLException;

    String findUnUsedLinkFromDatabase() throws SQLException;

    boolean isProcessedThisLink(String href) throws SQLException;

    void insertNewsToDatabase(String title, String content, String url) throws SQLException;
}
