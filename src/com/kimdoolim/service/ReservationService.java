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

  // ─────────────────────────────────────────────────────
  // 날짜/교시 유효성 체크 공통 메서드
  //
  // 체크 순서:
  // 1. 오늘 이전 날짜면 → 예약 불가
  // 2. 올해가 아니면 → 예약 불가 (연도 제한)
  // 3. 오늘 날짜인데 이미 끝난 교시면 → 예약 불가
  //    단, 현재 진행중인 교시는 예약 가능
  // ─────────────────────────────────────────────────────
  public String validateDateAndPeriod(LocalDate reservationDate, Period period) {
    LocalDate today = LocalDate.now();
    int currentYear = today.getYear();

    // 1. 오늘 이전 날짜 체크
    if (reservationDate.isBefore(today)) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    // 2. 올해 안에서만 예약 가능
    //    ex) 지금이 2026년이면 2026-12-31 까지만 예약 가능
    if (reservationDate.getYear() != currentYear) {
      return "예약은 올해(" + currentYear + "년) 안에서만 가능합니다.";
    }

    // 3. 오늘 날짜 예약인 경우 - 지난 교시 체크
    if (reservationDate.isEqual(today)) {
      LocalTime now = LocalTime.now();
      LocalTime periodStart = period.getStartTime();
      LocalTime periodEnd = period.getEndTime();

      // 현재 시간이 교시 시작~끝 사이면 진행중인 교시 → 예약 가능
      boolean isOngoing = !now.isBefore(periodStart) && !now.isAfter(periodEnd);

      // 교시가 이미 끝났으면 → 예약 불가
      // (진행중인 교시는 isOngoing = true 이므로 예약 가능)
      if (now.isAfter(periodEnd) && !isOngoing) {
        return "이미 지난 교시는 예약할 수 없습니다. "
            + "(" + period.getPeriodName() + ": "
            + periodStart + " ~ " + periodEnd + ")";
      }
    }

    return null; // null = 문제 없음
  }

  // ─────────────────────────────────────────────────────
  // 시설 예약 신청
  // ─────────────────────────────────────────────────────
  public String requestFacilityReservation(LocalDate reservationDate,
                                           Period period,
                                           Facility facility,
                                           String purpose) {
    // 날짜/교시 유효성 체크
    String validationError = validateDateAndPeriod(reservationDate, period);
    if (validationError != null) return validationError;

    // 중복 예약 체크
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

  // ─────────────────────────────────────────────────────
  // 비품 예약 신청
  // ─────────────────────────────────────────────────────
  public String requestEquipmentReservation(LocalDate reservationDate,
                                            Period period,
                                            Equipment equipment,
                                            String purpose) {
    // 날짜/교시 유효성 체크
    String validationError = validateDateAndPeriod(reservationDate, period);
    if (validationError != null) return validationError;

    // 중복 예약 체크
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

  // ─────────────────────────────────────────────────────
  // 내 예약 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Reservation> getMyReservations() {
    return reservationDAO.findReservationsByUserId(Auth.getUserInfo().getUserId());
  }

  // ─────────────────────────────────────────────────────
  // 반납 가능한 예약 목록 조회 (status = '승인')
  // ─────────────────────────────────────────────────────
  public List<Reservation> getReturnableReservations() {
    return reservationDAO.findReturnableReservations(Auth.getUserInfo().getUserId());
  }

  // ─────────────────────────────────────────────────────
  // 반납 처리
  // ─────────────────────────────────────────────────────
  public String returnReservation(long reservationId, String condition) {
    int result = reservationDAO.saveReturnRequest(reservationId, condition);
    return result > 0 ? "반납이 완료되었습니다!" : "반납 처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 예약 취소 (사용자)
  // ─────────────────────────────────────────────────────
  public String cancelReservation(long reservationId) {
    int result = reservationDAO.cancelReservation(reservationId, Auth.getUserInfo().getUserId());
    return result > 0
        ? "예약이 취소되었습니다."
        : "취소할 수 없습니다. (이미 취소/거절됐거나, 본인 예약이 아닙니다)";
  }

  // ─────────────────────────────────────────────────────
  // 관리자용 기능
  // ─────────────────────────────────────────────────────

  // 대기 중인 예약 목록 (승인/반려 처리용)
  public List<Reservation> getPendingReservations() {
    return reservationDAO.findPendingReservations();
  }

  // 승인된 예약 목록 (강제 취소용)
  public List<Reservation> getApprovedReservations() {
    return reservationDAO.findApprovedReservations();
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