package com.kimdoolim.service;

import com.kimdoolim.alarm.AlarmSendingManager;
import com.kimdoolim.dao.BlockScheduleDAO;
import com.kimdoolim.dao.ReservationDAO;
import com.kimdoolim.dto.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class BlockScheduleService {

  private final BlockScheduleDAO dao = new BlockScheduleDAO();
  private final ReservationDAO reservationDAO = new ReservationDAO();
  private final AlarmSendingManager sendingManager = AlarmSendingManager.getAlarmSendingManager();

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
    if (count > 0) cancelAndNotify(blockScheduleId, null, null);
    return count > 0 ? "전체 시설/비품에 적용되었습니다. (" + count + "건)" : "적용 중 오류가 발생했습니다.";
  }

  public String applyToTarget(long blockScheduleId, Long facilityId, Long equipmentId) {
    int result = dao.saveBlockDetail(blockScheduleId, facilityId, equipmentId);
    if (result > 0) cancelAndNotify(blockScheduleId, facilityId, equipmentId);
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

  // ─────────────────────────────────────────────────────
  // 적용 후 겹치는 예약 취소 + 사용자 알림
  // ─────────────────────────────────────────────────────
  private void cancelAndNotify(long blockScheduleId, Long facilityId, Long equipmentId) {
    Map<String, Object> info = dao.getById(blockScheduleId);
    if (info == null) return;

    int periodId   = (int)    info.get("periodId");
    String desc    = (String) info.get("desc");
    String reason  = "교시 차단(" + desc + ")으로 인한 자동 취소";

    LocalDate blockDate  = (LocalDate) info.get("date");
    Integer repeatDay    = (Integer)   info.get("repeatDay");
    LocalDate repeatStart = (LocalDate) info.get("repeatStart");
    LocalDate repeatEnd   = (LocalDate) info.get("repeatEnd");

    List<Reservation> cancelled;
    if (blockDate != null) {
      // 특정 날짜 차단: 해당 날짜 하루만
      cancelled = reservationDAO.cancelReservationsForBlockPeriod(
          blockDate, blockDate, periodId, facilityId, equipmentId, reason);
    } else {
      // 반복 요일 차단: 범위 내 해당 요일 날짜들
      cancelled = reservationDAO.cancelReservationsForRepeatBlock(
          repeatDay, repeatStart, repeatEnd, periodId, facilityId, equipmentId, reason);
    }

    for (Reservation r : cancelled) {
      sendingManager.sendingTextToSocketServer("취소", r.getReservationId(), "BLOCK");
    }

    if (!cancelled.isEmpty()) {
      System.out.println("🚫 [교시 차단] " + cancelled.size() + "건의 예약이 자동 취소되었습니다.");
    }
  }
}
