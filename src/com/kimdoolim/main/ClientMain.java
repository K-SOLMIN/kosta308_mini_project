package com.kimdoolim.main;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.main.view.LoginView;
import com.kimdoolim.main.view.MainView;

public class ClientMain {
    private static User loginUser = null;

    public static void main(String[] args) {
        new LoginView().loginView();

        MainView mainView = new MainView();

        if(loginUser.getPermission() == Permission.USER) mainView.userMainView();
        else mainView.managerMainView();
    }
}
