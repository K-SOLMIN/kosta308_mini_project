package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.ReservationDAO;
import com.kimdoolim.dto.*;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

  private final ReservationDAO reservationDAO = new ReservationDAO();

  // ─────────────────────────────────────────────────────
  // 1. 교시 목록 반환
  // ─────────────────────────────────────────────────────
  public List<Period> getAvailablePeriods() {
    return reservationDAO.findAllPeriods();
  }

  // ─────────────────────────────────────────────────────
  // 2. 시설 목록 반환
  // ─────────────────────────────────────────────────────
  public List<Facility> getAvailableFacilities() {
    return reservationDAO.findAvailableFacilities();
  }

  // ─────────────────────────────────────────────────────
  // 3. 비품 목록 반환
  // ─────────────────────────────────────────────────────
  public List<Equipment> getAvailableEquipments() {
    return reservationDAO.findAvailableEquipments();
  }

  // ─────────────────────────────────────────────────────
  // 4. 시설 예약 신청
  // ─────────────────────────────────────────────────────
  public String requestFacilityReservation(LocalDate reservationDate,
                                           Period period,
                                           Facility facility,
                                           String purpose) {
    if (reservationDate.isBefore(LocalDate.now())) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    boolean isDuplicate = reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), facility.getFacilityId(), null
    );
    if (isDuplicate) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    User loginUser = Auth.getUserInfo();

    Reservation reservation = Reservation.builder()
        .period(period)
        .user(loginUser)
        .facility(facility)
        .equipment(null)
        .purpose(purpose)
        .reservationDate(reservationDate)
        .targetType("FACILITY")
        .build();

    int result = reservationDAO.saveReservation(reservation);
    return result > 0 ? "예약이 신청되었습니다! (승인 대기 중)" : "예약 신청 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 5. 비품 예약 신청
  // ─────────────────────────────────────────────────────
  public String requestEquipmentReservation(LocalDate reservationDate,
                                            Period period,
                                            Equipment equipment,
                                            String purpose) {
    if (reservationDate.isBefore(LocalDate.now())) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    boolean isDuplicate = reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), null, equipment.getEquipmentId()
    );
    if (isDuplicate) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    User loginUser = Auth.getUserInfo();

    Reservation reservation = Reservation.builder()
        .period(period)
        .user(loginUser)
        .facility(null)
        .equipment(equipment)
        .purpose(purpose)
        .reservationDate(reservationDate)
        .targetType("EQUIPMENT")
        .build();

    int result = reservationDAO.saveReservation(reservation);
    return result > 0 ? "예약이 신청되었습니다! (승인 대기 중)" : "예약 신청 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 6. 내 예약 목록 조회
  // ─────────────────────────────────────────────────────
  public List<Reservation> getMyReservations() {
    User loginUser = Auth.getUserInfo();
    return reservationDAO.findReservationsByUserId(loginUser.getUserId());
  }

  // ─────────────────────────────────────────────────────
  // 7. 반납 가능한 예약 목록 조회
  //    → status = '승인' 인 것만
  // ─────────────────────────────────────────────────────
  public List<Reservation> getReturnableReservations() {
    User loginUser = Auth.getUserInfo();
    return reservationDAO.findReturnableReservations(loginUser.getUserId());
  }

  // ─────────────────────────────────────────────────────
  // 8. 반납 처리
  //    → condition : 이상 없으면 '정상', 있으면 사용자 입력값
  // ─────────────────────────────────────────────────────
  public String returnReservation(long reservationId, String condition) {
    int result = reservationDAO.saveReturnRequest(reservationId, condition);
    return result > 0 ? "반납이 완료되었습니다!" : "반납 처리 중 오류가 발생했습니다.";
  }

  // ─────────────────────────────────────────────────────
  // 9. 예약 취소
  // ─────────────────────────────────────────────────────
  public String cancelReservation(long reservationId) {
    User loginUser = Auth.getUserInfo();
    int result = reservationDAO.cancelReservation(reservationId, loginUser.getUserId());

    return result > 0
        ? "예약이 취소되었습니다."
        : "취소할 수 없습니다. (이미 취소/거절됐거나, 본인 예약이 아닙니다)";
  }
}