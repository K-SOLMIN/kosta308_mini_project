package com.kimdoolim.main;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.main.view.LoginView;
import com.kimdoolim.main.view.MainView;

public class ClientMain {

    public static void main(String[] args) {
        new LoginView().loginView();
        User loginUser = Auth.getUserInfo();

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