package com.kimdoolim.main;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;
import com.kimdoolim.main.view.LoginView;
import com.kimdoolim.main.view.MainView;
import java.net.Socket;

public class ClientMain {

    public static Socket socket;

    public static void main(String[] args) {
        new LoginView().loginView();
        User loginUser = Auth.getUserInfo();

//        if (loginUser == null) {
//            System.out.println("로그인에 실패했습니다.");
//            return;
//        }

        // 비활성 계정 차단 (Auth.login에서 이미 막히지만 이중 체크)
        if (!loginUser.isActive()) {
            String status = loginUser.getUserStatus();
            if ("전근".equals(status)) {
                System.out.println("전근 처리된 계정입니다. 새 학교 관리자에게 승인을 요청해주세요.");
            } else {
                System.out.println("비활성화된 계정입니다. (" + status + ") 관리자에게 문의해주세요.");
            }
            return;
        }

        MainView mainView = new MainView();

        // 비활성 계정 → 권한 관계없이 제한 메뉴
        if (!loginUser.isActive()) {
            String status = loginUser.getUserStatus();
            if ("전근".equals(status)) {
                System.out.println("※ 전근 처리된 계정입니다. 새 학교 관리자 승인 전까지 조회만 가능합니다.");
            } else {
                System.out.println("※ 휴직 중인 계정입니다. 예약 내역 조회와 마이페이지만 이용 가능합니다.");
            }
            mainView.restrictedUserView();
            return;
        }

        Permission permission = loginUser.getPermission();

        if (permission == Permission.USER) {
            mainView.userMainView();
        } else if (permission == Permission.MIDDLEADMIN) {
            mainView.middleAdminMainView();
        } else if (permission == Permission.ADMIN) {
            mainView.adminMainView();
        }
    }
}