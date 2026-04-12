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

  public long addSpecificBlock(LocalDate date, int periodId, String description) {
    return dao.saveSpecificBlock(date, periodId, description);
  }

  public long addRepeatBlock(int dayOfWeek, LocalDate startDate, LocalDate endDate,
                             int periodId, String description) {
    return dao.saveRepeatBlock(dayOfWeek, startDate, endDate, periodId, description);
  }

  public String deleteBlockSchedule(long id) {
    int result = dao.delete(id);
    return result > 0 ? "삭제되었습니다." : "삭제 중 오류가 발생했습니다.";
  }

  public String applyToAll(long blockScheduleId) {
    int count = dao.applyBlockDetailToAll(blockScheduleId);
    return count > 0 ? "전체 시설/비품에 적용되었습니다. (" + count + "건)" : "적용 중 오류가 발생했습니다.";
  }

  public String applyToTarget(long blockScheduleId, Long facilityId, Long equipmentId) {
    int result = dao.saveBlockDetail(blockScheduleId, facilityId, equipmentId);
    return result > 0 ? "적용되었습니다." : "적용 중 오류가 발생했습니다.";
  }

  public Map<String, Object> getBlockDetails(long blockScheduleId) {
    return dao.getBlockDetailsForDisplay(blockScheduleId);
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
