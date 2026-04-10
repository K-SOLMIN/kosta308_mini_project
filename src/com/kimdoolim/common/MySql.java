package com.kimdoolim.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySql implements Database {

    // 한글 깨짐 방지를 위해 UTF-8 인코딩 설정 추가(local용)
    //private static final String URL = "jdbc:mysql://localhost:3307/kimdoolim?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul"; //LOCAL용

    //docker용
    private static final String URL = "jdbc:mysql://kimdoolim-mysql:3306/kimdoolim?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul";
    private static final String USER     = "root";
    private static final String PASSWORD = "1111";

    private static final MySql mySql;

    static {
        mySql = new MySql();
    }

    private MySql() {}

    public static MySql getMySql() {
        return mySql;
    }

    @Override
    public Connection getConnection() {
        Connection connection = null;
        int retries = 10;

        while (retries-- > 0) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                return connection;
            } catch (ClassNotFoundException e) {
                System.out.println("드라이버 로드 실패: " + e.getMessage());
                return null;
            } catch (SQLException e) {
                System.out.println("DB 연결 실패, 재시도 중... (" + retries + "회 남음)");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    @Override
    public <T extends AutoCloseable> void close(T t) {
        try {
            if (t != null) t.close();
        } catch (Exception e) {
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