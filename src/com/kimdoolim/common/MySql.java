package com.kimdoolim.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
   싱그론으로 구현할거기때문에 생성자로 접근할 수 없습니다.

 */
public class MySql implements DataBase{
    private static final String URL      = "jdbc:mysql://localhost:3306/kimdoolim";
    private static final String USER     = "root";
    private static final String PASSWORD = "1111";

    private final MySql mySql = new MySql();

    private MySql() { }

    public MySql getMySql() {
        return mySql;
    }

    @Override
    public Connection getConnection() {
        Connection connection = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("DB 연결 성공!");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 실패: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("DB 연결 실패: " + e.getMessage());
        }

        return connection;
    }

    @Override
    public <T extends AutoCloseable> void close(T t) {
        try {
            if(t != null) t.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
