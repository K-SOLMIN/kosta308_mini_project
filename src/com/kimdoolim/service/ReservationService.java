package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.ReservationDAO;
import com.kimdoolim.dto.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationService {

  private final ReservationDAO reservationDAO = new ReservationDAO();

  // ─────────────────────────────────────────────────────
  // 날짜/교시 유효성 체크
  // ─────────────────────────────────────────────────────
  public String validateDateAndPeriod(LocalDate reservationDate, Period period) {
    LocalDate today = LocalDate.now();
    int currentYear = today.getYear();

    if (reservationDate.isBefore(today)) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    if (reservationDate.getYear() != currentYear) {
      return "예약은 올해(" + currentYear + "년) 안에서만 가능합니다.";
    }

    if (reservationDate.isEqual(today)) {
      LocalTime now = LocalTime.now();
      LocalTime periodStart = period.getStartTime();
      LocalTime periodEnd = period.getEndTime();
      boolean isOngoing = !now.isBefore(periodStart) && !now.isAfter(periodEnd);

      if (now.isAfter(periodEnd) && !isOngoing) {
        return "이미 지난 교시는 예약할 수 없습니다. "
            + "(" + period.getPeriodName() + ": "
            + periodStart + " ~ " + periodEnd + ")";
      }
    }

    return null;
  }

  // ─────────────────────────────────────────────────────
  // 사용자용 기능
  // ─────────────────────────────────────────────────────

  public List<Period> getAvailablePeriods() {
    return reservationDAO.findAllPeriods();
  }

  public List<Facility> getAvailableFacilities() {
    return reservationDAO.findAvailableFacilities();
  }

  public List<Equipment> getAvailableEquipments() {
    return reservationDAO.findAvailableEquipments();
  }

  public String requestFacilityReservation(LocalDate reservationDate,
                                           Period period,
                                           Facility facility,
                                           String purpose) {
    if (reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), facility.getFacilityId(), null)) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    User loginUser = Auth.getUserInfo();
    Reservation reservation = Reservation.builder()
        .period(period).user(loginUser).facility(facility).equipment(null)
        .purpose(purpose).reservationDate(reservationDate).targetType("FACILITY")
        .build();

    int result = reservationDAO.saveReservation(reservation);
    return result > 0 ? "예약이 신청되었습니다! (승인 대기 중)" : "예약 신청 중 오류가 발생했습니다.";
  }

  public String requestEquipmentReservation(LocalDate reservationDate,
                                            Period period,
                                            Equipment equipment,
                                            String purpose) {
    if (reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), null, equipment.getEquipmentId())) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    User loginUser = Auth.getUserInfo();
    Reservation reservation = Reservation.builder()
        .period(period).user(loginUser).facility(null).equipment(equipment)
        .purpose(purpose).reservationDate(reservationDate).targetType("EQUIPMENT")
        .build();

    int result = reservationDAO.saveReservation(reservation);
    return result > 0 ? "예약이 신청되었습니다! (승인 대기 중)" : "예약 신청 중 오류가 발생했습니다.";
  }

  public List<Reservation> getMyReservations() {
    return reservationDAO.findReservationsByUserId(Auth.getUserInfo().getUserId());
  }

  public List<Reservation> getReturnableReservations() {
    return reservationDAO.findReturnableReservations(Auth.getUserInfo().getUserId());
  }

  public String returnReservation(long reservationId, String condition) {
    int result = reservationDAO.saveReturnRequest(reservationId, condition);
    return result > 0 ? "반납이 완료되었습니다!" : "반납 처리 중 오류가 발생했습니다.";
  }

  public String cancelReservation(long reservationId) {
    int result = reservationDAO.cancelReservation(reservationId, Auth.getUserInfo().getUserId());
    return result > 0
        ? "예약이 취소되었습니다."
        : "취소할 수 없습니다. (이미 취소/거절됐거나, 본인 예약이 아닙니다)";
  }

  // ─────────────────────────────────────────────────────
  // 관리자용 기능
  // 권한에 따라 자동으로 분기
  // ADMIN       → 전체 예약 조회
  // MIDDLEADMIN → 담당 시설/비품 예약만 조회
  // ─────────────────────────────────────────────────────

  // 대기 중인 예약 목록
  public List<Reservation> getPendingReservations() {
    User loginUser = Auth.getUserInfo();

    if (loginUser.getPermission() == Permission.ADMIN) {
      // 상위 관리자 → 전체 조회
      return reservationDAO.findPendingReservations();
    } else {
      // 중간 관리자 → 담당 시설/비품만 조회
      return reservationDAO.findPendingReservationsByManagerId(loginUser.getUserId());
    }
  }

  // 승인된 예약 목록 (강제 취소용)
  public List<Reservation> getApprovedReservations() {
    User loginUser = Auth.getUserInfo();

    if (loginUser.getPermission() == Permission.ADMIN) {
      // 상위 관리자 → 전체 조회
      return reservationDAO.findApprovedReservations();
    } else {
      // 중간 관리자 → 담당 시설/비품만 조회
      return reservationDAO.findApprovedReservationsByManagerId(loginUser.getUserId());
    }
  }

  // 예약 승인
  public String approveReservation(long reservationId) {
    int result = reservationDAO.approveReservation(reservationId);
    return result > 0 ? "예약이 승인되었습니다." : "승인 처리 중 오류가 발생했습니다.";
  }

  // 예약 반려
  public String rejectReservation(long reservationId, String reason) {
    int result = reservationDAO.rejectReservation(reservationId, reason);
    return result > 0 ? "예약이 반려되었습니다." : "반려 처리 중 오류가 발생했습니다.";
  }

  // 예약 강제 취소
  public String forceCancelReservation(long reservationId, String reason) {
    int result = reservationDAO.forcecancelReservation(reservationId, reason);
    return result > 0 ? "예약이 강제 취소되었습니다." : "강제 취소 처리 중 오류가 발생했습니다.";
  }
}