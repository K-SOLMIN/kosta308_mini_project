package com.kimdoolim.main;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.main.view.LoginView;
import com.kimdoolim.main.view.MainView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.System.out;

public class ClientMain {

    public static void main(String[] args) {
        new LoginView().loginView();
        User loginUser = Auth.getUserInfo();

//        try {
//            Socket socket = new Socket("localhost", 8080);
//            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//
//            System.out.println("loginUser : " + loginUser);
//
//            out.println(loginUser.getId());
//
//            new Thread(() -> {
//                try {
//                    BufferedReader msgReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    String alarmMsg = "";
//
//                    while ((alarmMsg = msgReader.readLine()) != null) {
//                        out.println(alarmMsg);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }

        MainView mainView = new MainView();

        if(loginUser.getPermission() == Permission.USER) mainView.userMainView();
        else mainView.managerMainView();
    }
}
