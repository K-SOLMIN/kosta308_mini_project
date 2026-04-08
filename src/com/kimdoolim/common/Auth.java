package com.kimdoolim.common;

import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Auth {

    private static final Map<Integer, User> loginUser = new ConcurrentHashMap<>();
    private static final ThreadLocal<Integer> threadLocalUserId = new ThreadLocal<>();

    private Auth() {}

    /*
        db에서 아이디 비밀번호가 일치하는 회원의 정보를 member 객체로 가져오고
        객체가 존재한다면 1 존재하지 않는다면 0
        멤버객체가 존재한다면 스레드지역변수에 멤버아이디 저장
        가져온 멤버객체 loginMemberMap에 저장

        한계정으로 중복로그인 시도시 원래 로그인되어있던 프로그램에서 로그아웃 시켜버리고
        현재 로그인시도중인 프로세스에서 로그인되도록..
    */
    public static int login(String userId, String userPwd) {
        Database db = MySql.getMySql();
        Connection conn = db.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT * FROM user WHERE ID = ? AND PASSWORD = ?";

        User user = null;
        int result = 0;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, userId);
            pstmt.setString(2, userPwd);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = User.builder()
                        .userId(rs.getInt("USER_ID"))
                        .schoolId(rs.getInt("SCHOOL_ID"))
                        .id(rs.getString("ID"))
                        .password(rs.getString("PASSWORD"))
                        .permission(Permission.valueOf(rs.getString("PERMISSION")))
                        .name(rs.getString("NAME"))
                        .phone(rs.getString("PHONE"))
                        .gradeNo(rs.getInt("GRADE_NO"))
                        .classNo(rs.getInt("CLASS_NO"))
                        .isActive(Boolean.parseBoolean(rs.getString("IS_ACTIVE")))
                        .build();
            }

            if(user != null) {
                result = 1;
                loginUser.put(user.getUserId(), user);
                threadLocalUserId.set(user.getUserId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close(rs);
            db.close(pstmt);
            db.close(conn);
        }
        System.out.println("loginUser : " + user);
        return result;
    }

    // 로그인한 사용자 정보 가져오는 메소드입니다
    //ex) Auth.getUserInfo() return User
    public static User getUserInfo() {
        Integer id = threadLocalUserId.get();

        if (id == null) return null;

        return loginUser.get(id);
    }

    public static void logout() {
        Integer id = threadLocalUserId.get();

        if (id != null) {
            loginUser.remove(id);
            threadLocalUserId.remove();
        }
    }
}
