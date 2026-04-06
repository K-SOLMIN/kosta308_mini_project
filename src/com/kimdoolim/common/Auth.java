package com.kimdoolim.common;

import com.kimdoolim.dto.User;

import java.sql.Connection;
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
    public static int login(String memberId, String memberPwd) {
        Connection conn = MySql.getMySql().getConnection();


        threadLocalUserId.set(1);
        return 0;
    }

    public static User getMemberInfo() {
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
