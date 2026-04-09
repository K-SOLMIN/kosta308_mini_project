package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.FacilityEquipmentDAO;
import com.kimdoolim.dao.UserDAO;
import com.kimdoolim.dto.Permission;
import com.kimdoolim.dto.User;

import java.util.List;

public class UserService {

  private final UserDAO userDAO = new UserDAO();
  private final FacilityEquipmentDAO facilityEquipmentDAO = new FacilityEquipmentDAO();

  // ─────────────────────────────────────────────────────
  // 1. 전체 사용자 목록
  // ─────────────────────────────────────────────────────
  public List<User> getAllUsers() {
    return userDAO.findAllUsers();
  }

  // ─────────────────────────────────────────────────────
  // 2. 중간관리자 목록 (담당자 재배정 시 사용)
  // ─────────────────────────────────────────────────────
  public List<User> getMiddleAdmins() {
    return userDAO.findMiddleAdmins();
  }

  // ─────────────────────────────────────────────────────
  // 3. 사용자 등록
  // ─────────────────────────────────────────────────────
  public String registerUser(User user) {
    if (userDAO.isIdDuplicate(user.getId())) {
      return "이미 사용 중인 아이디입니다.";
    }
    int result = userDAO.saveUser(user);
    return result > 0 ? "사용자가 등록되었습니다." : "사용자 등록 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 4. 휴직 처리 → 담당 시설/비품 담당자 없음으로 변경
  // ─────────────────────────────────────────────────────
  public String setLeaveOfAbsence(int userId) {
    int result = userDAO.setLeaveOfAbsence(userId);
    if (result > 0) {
      facilityEquipmentDAO.clearManagerFromFacilities(userId);
      facilityEquipmentDAO.clearManagerFromEquipments(userId);
      return "휴직 처리되었습니다. 담당 시설/비품이 담당자 없음으로 변경되었습니다.";
    }
    return "처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 5. 복직 처리 (휴직 → 활성)
  // ─────────────────────────────────────────────────────
  public String restoreFromLeave(int userId) {
    int result = userDAO.restoreFromLeave(userId);
    return result > 0 ? "복직 처리되었습니다." : "처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 6. 전근 처리 → 담당 시설/비품 담당자 없음으로 변경
  // ─────────────────────────────────────────────────────
  public String setTransfer(int userId) {
    int result = userDAO.setTransfer(userId);
    if (result > 0) {
      facilityEquipmentDAO.clearManagerFromFacilities(userId);
      facilityEquipmentDAO.clearManagerFromEquipments(userId);
      return "전근 처리되었습니다. 담당 시설/비품이 담당자 없음으로 변경되었습니다.";
    }
    return "처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 7. 전근 승인 (새 학교 관리자가 승인)
  // ─────────────────────────────────────────────────────
  public String approveTransfer(int userId) {
    int result = userDAO.approveTransfer(userId);
    return result > 0 ? "전근 승인이 완료되었습니다. 사용자가 시스템을 사용할 수 있습니다."
        : "처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 8. 권한 변경 (USER ↔ MIDDLEADMIN)
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
  // 9. 비밀번호 변경 (본인)
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