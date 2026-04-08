package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.UserDAO;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.util.List;

public class UserService {

  private final UserDAO userDAO = new UserDAO();

  // ─────────────────────────────────────────────────────
  // 1. 전체 사용자 목록
  // ─────────────────────────────────────────────────────
  public List<User> getAllUsers() {
    return userDAO.findAllUsers();
  }

  // ─────────────────────────────────────────────────────
  // 2. 사용자 등록
  // ─────────────────────────────────────────────────────
  public String registerUser(User user) {
    if (userDAO.isIdDuplicate(user.getId())) {
      return "이미 사용 중인 아이디입니다.";
    }
    int result = userDAO.saveUser(user);
    return result > 0 ? "사용자가 등록되었습니다." : "사용자 등록 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 3. 상태 변경 (활성/비활성 토글)
  // ─────────────────────────────────────────────────────
  public String toggleUserActive(int userId, boolean currentIsActive) {
    int result;
    if (currentIsActive) {
      result = userDAO.deactivateUser(userId);
      return result > 0 ? "비활성화 처리되었습니다. (휴직/전근)" : "처리 중 오류가 발생했습니다.";
    } else {
      result = userDAO.activateUser(userId);
      return result > 0 ? "활성화 처리되었습니다. (복직)" : "처리 중 오류가 발생했습니다.";
    }
  }

  // ─────────────────────────────────────────────────────
  // 4. 권한 변경 (USER ↔ MIDDLEADMIN)
  // ─────────────────────────────────────────────────────
  public String togglePermission(int userId, Permission currentPermission) {
    Permission newPermission = (currentPermission == Permission.USER)
        ? Permission.MIDDLEADMIN : Permission.USER;
    int result = userDAO.updatePermission(userId, newPermission);
    if (result > 0) {
      String label = newPermission == Permission.MIDDLEADMIN ? "중간 관리자" : "일반 사용자";
      return "권한이 [" + label + "] 로 변경되었습니다.";
    }
    return "권한 변경 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 5. 비밀번호 변경 (본인)
  // ─────────────────────────────────────────────────────
  public String changePassword(String currentPwd, String newPwd, String confirmPwd) {
    User loginUser = Auth.getUserInfo();

    if (!loginUser.getPassword().equals(currentPwd)) {
      return "현재 비밀번호가 일치하지 않습니다.";
    }
    if (!newPwd.equals(confirmPwd)) {
      return "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.";
    }
    if (newPwd.length() < 4) {
      return "비밀번호는 4자 이상이어야 합니다.";
    }
    if (newPwd.equals(currentPwd)) {
      return "새 비밀번호가 현재 비밀번호와 동일합니다.";
    }
    int result = userDAO.updatePassword(loginUser.getUserId(), newPwd);
    return result > 0 ? "비밀번호가 변경되었습니다." : "비밀번호 변경 중 오류가 발생했습니다.";
  }
}