package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.FacilityEquipmentDAO;
import com.kimdoolim.dto.Equipment;
import com.kimdoolim.dto.Facility;
import com.kimdoolim.dto.User;

import java.util.List;

public class FacilityEquipmentService {

  private final FacilityEquipmentDAO facilityEquipmentDAO = new FacilityEquipmentDAO();

  // ─────────────────────────────────────────────────────
  // 1. 전체 시설 목록
  // ─────────────────────────────────────────────────────
  public List<Facility> getAllFacilities() {
    return facilityEquipmentDAO.findAllFacilities();
  }

  // ─────────────────────────────────────────────────────
  // 2. 전체 비품 목록
  // ─────────────────────────────────────────────────────
  public List<Equipment> getAllEquipments() {
    return facilityEquipmentDAO.findAllEquipments();
  }

  // ─────────────────────────────────────────────────────
  // 3. 시설 등록
  //    → 담당자는 현재 로그인한 관리자로 자동 설정
  // ─────────────────────────────────────────────────────
  public String registerFacility(Facility facility) {
    User loginUser = Auth.getUserInfo();
    int result = facilityEquipmentDAO.saveFacility(facility, loginUser.getUserId());
    return result > 0 ? "시설이 등록되었습니다!" : "시설 등록 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 4. 비품 등록
  //    → 담당자는 현재 로그인한 관리자로 자동 설정
  // ─────────────────────────────────────────────────────
  public String registerEquipment(Equipment equipment) {
    User loginUser = Auth.getUserInfo();
    int result = facilityEquipmentDAO.saveEquipment(equipment, loginUser.getUserId());
    return result > 0 ? "비품이 등록되었습니다!" : "비품 등록 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 5. 시설 상태 수정
  // ─────────────────────────────────────────────────────
  public String updateFacilityStatus(long facilityId, String status) {
    int result = facilityEquipmentDAO.updateFacilityStatus(facilityId, status);
    return result > 0 ? "시설 상태가 [" + status + "] 로 변경되었습니다." : "상태 변경 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 6. 비품 상태 수정
  // ─────────────────────────────────────────────────────
  public String updateEquipmentStatus(long equipmentId, String status) {
    int result = facilityEquipmentDAO.updateEquipmentStatus(equipmentId, status);
    return result > 0 ? "비품 상태가 [" + status + "] 로 변경되었습니다." : "상태 변경 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 7. 시설 삭제
  // ─────────────────────────────────────────────────────
  public String deleteFacility(long facilityId) {
    int result = facilityEquipmentDAO.deleteFacility(facilityId);
    return result > 0 ? "시설이 삭제되었습니다." : "삭제 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 8. 비품 삭제
  // ─────────────────────────────────────────────────────
  public String deleteEquipment(long equipmentId) {
    int result = facilityEquipmentDAO.deleteEquipment(equipmentId);
    return result > 0 ? "비품이 삭제되었습니다." : "삭제 중 오류가 발생했습니다.";
  }
}