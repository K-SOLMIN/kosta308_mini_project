package com.kimdoolim.common;

import java.sql.Connection;

public interface Database {
    Connection getConnection();

    <T extends AutoCloseable>void close(T t);

    void commit(Connection connection);

    void rollback(Connection connection);
}
