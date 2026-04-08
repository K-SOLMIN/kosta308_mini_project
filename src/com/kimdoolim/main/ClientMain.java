package com.kimdoolim.main;

import com.kimdoolim.alarm.AlarmReceiveThread;
import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.main.view.LoginView;
import com.kimdoolim.main.view.MainView;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    public static PrintWriter out = null;
    public static Socket socket = null;

    public static void main(String[] args) {
        new LoginView().loginView();
        User loginUser = Auth.getUserInfo();

        try {
            socket = new Socket("localhost", 9999);
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("서버로 보낼 userId : " + loginUser.getUserId());
            out.println(loginUser.getUserId());

            new Thread(new AlarmReceiveThread()).start();
        } catch(IOException e) {
            e.printStackTrace();
            out.println("서버소켓에 접속 실패;;");
        }

        MainView mainView = new MainView();

        Permission permission = loginUser.getPermission();

        if (permission == Permission.USER) {
            // 일반 사용자 메뉴
            mainView.userMainView();

        } else if (permission == Permission.MIDDLEADMIN) {
            // 중간 관리자 메뉴
            mainView.middleAdminMainView();

        } else if (permission == Permission.ADMIN) {
            // 상위 관리자 메뉴
            mainView.adminMainView();
        }
    }
}