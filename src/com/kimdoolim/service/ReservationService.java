package com.kimdoolim.service;

import com.kimdoolim.common.Auth;
import com.kimdoolim.dao.ReservationDAO;
import com.kimdoolim.dto.*;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {

  // DAO 객체 생성 (DB 접근은 DAO에게 맡김)
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
  //    → 유효성 검사 후 DAO에 저장 요청
  //    → 결과 메시지 반환 (View에서 출력)
  // ─────────────────────────────────────────────────────
  public String requestFacilityReservation(LocalDate reservationDate,
                                           Period period,
                                           Facility facility,
                                           String purpose) {
    // 오늘 이전 날짜 체크
    if (reservationDate.isBefore(LocalDate.now())) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    // 중복 예약 체크
    boolean isDuplicate = reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), facility.getFacilityId(), null
    );
    if (isDuplicate) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    // 로그인한 사용자 가져오기
    User loginUser = Auth.getUserInfo();

    // Reservation 객체 만들기
    Reservation reservation = Reservation.builder()
        .period(period)
        .user(loginUser)
        .facility(facility)
        .equipment(null)        // 시설 예약 → 비품 없음
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
    // 오늘 이전 날짜 체크
    if (reservationDate.isBefore(LocalDate.now())) {
      return "오늘 이전 날짜는 예약할 수 없습니다.";
    }

    // 중복 예약 체크
    boolean isDuplicate = reservationDAO.isDuplicateReservation(
        reservationDate, period.getPeriodId(), null, equipment.getEquipmentId()
    );
    if (isDuplicate) {
      return "해당 날짜/교시에 이미 예약이 있습니다.";
    }

    // 로그인한 사용자 가져오기
    User loginUser = Auth.getUserInfo();

    // Reservation 객체 만들기
    Reservation reservation = Reservation.builder()
        .period(period)
        .user(loginUser)
        .facility(null)          // 비품 예약 → 시설 없음
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
  // 7. 예약 취소
  // ─────────────────────────────────────────────────────
  public String cancelReservation(long reservationId) {
    User loginUser = Auth.getUserInfo();
    int result = reservationDAO.cancelReservation(reservationId, loginUser.getUserId());

    return result > 0
        ? "예약이 취소되었습니다."
        : "취소할 수 없습니다. (이미 취소/거절됐거나, 본인 예약이 아닙니다)";
  }
}
