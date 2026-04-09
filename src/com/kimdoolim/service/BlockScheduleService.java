package com.kimdoolim.service;

import com.kimdoolim.dao.BlockScheduleDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BlockScheduleService {

  private final BlockScheduleDAO dao = new BlockScheduleDAO();

  public List<Map<String, Object>> getAllBlockSchedules() {
    return dao.findAll();
  }

  public String addSpecificBlock(LocalDate date, int periodId, String description,
                                 Long facilityId, Long equipmentId) {
    int result = dao.saveSpecificBlock(date, periodId, description, facilityId, equipmentId);
    return result > 0 ? "특정 날짜/교시 차단이 등록되었습니다." : "등록 중 오류가 발생했습니다.";
  }

  public String addRepeatBlock(int dayOfWeek, LocalDate startDate, LocalDate endDate,
                               int periodId, String description,
                               Long facilityId, Long equipmentId) {
    int result = dao.saveRepeatBlock(dayOfWeek, startDate, endDate, periodId, description, facilityId, equipmentId);
    return result > 0 ? "반복 요일/교시 차단이 등록되었습니다." : "등록 중 오류가 발생했습니다.";
  }

  public String deleteBlockSchedule(long id) {
    int result = dao.delete(id);
    return result > 0 ? "삭제되었습니다." : "삭제 중 오류가 발생했습니다.";
  }

  // 예약 시 체크용
  public String checkBlocked(LocalDate date, int periodId, Long facilityId, Long equipmentId) {
    String reason = dao.findBlockedReason(date, periodId, facilityId, equipmentId);
    if (reason != null) {
      return "해당 날짜/교시는 예약이 제한되어 있습니다. (사유: " + reason + ")";
    }
    return null;
  }
}