package com.finance.smartLedger.fees.infrastructure.persistence;

import com.finance.smartLedger.fees.domain.FeeSchedule;
import com.finance.smartLedger.fees.domain.FeeSchedule.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeScheduleRepository
    extends JpaRepository<FeeSchedule, UUID>, JpaSpecificationExecutor<FeeSchedule> {

  @Query("SELECT f FROM FeeSchedule f WHERE f.code = :code")
  Optional<FeeSchedule> findByCode(@Param("code") String code);

  @Query(
      "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FeeSchedule f WHERE f.code = :code")
  boolean existsByCode(@Param("code") String code);

  @Query("SELECT f FROM FeeSchedule f WHERE f.academicYear = :academicYear")
  List<FeeSchedule> findByAcademicYear(@Param("academicYear") String academicYear);

  @Query(
      "SELECT f FROM FeeSchedule f WHERE f.academicYear = :academicYear AND f.academicTerm = :academicTerm")
  List<FeeSchedule> findByAcademicYearAndTerm(
      @Param("academicYear") String academicYear, @Param("academicTerm") String academicTerm);

  @Query("SELECT f FROM FeeSchedule f WHERE f.classGrade = :classGrade")
  List<FeeSchedule> findByClassGrade(@Param("classGrade") String classGrade);

  @Query("SELECT f FROM FeeSchedule f WHERE f.status = :status")
  List<FeeSchedule> findByStatus(@Param("status") ScheduleStatus status);

  @Query(
      "SELECT f FROM FeeSchedule f WHERE f.academicYear = :academicYear AND f.classGrade = :classGrade")
  List<FeeSchedule> findByAcademicYearAndClassGrade(
      @Param("academicYear") String academicYear, @Param("classGrade") String classGrade);

  @Query("SELECT f FROM FeeSchedule f WHERE f.status = :status AND f.academicYear = :academicYear")
  List<FeeSchedule> findByStatusAndAcademicYear(
      @Param("status") ScheduleStatus status, @Param("academicYear") String academicYear);

  @Query(
      "SELECT f FROM FeeSchedule f WHERE f.effectiveFrom <= :date AND (f.effectiveTo IS NULL OR f.effectiveTo >= :date)")
  List<FeeSchedule> findEffectiveSchedules(@Param("date") LocalDate date);

  @Query(
      "SELECT f FROM FeeSchedule f WHERE f.effectiveFrom <= :date AND (f.effectiveTo IS NULL OR f.effectiveTo >= :date) AND f.status = 'ACTIVE'")
  List<FeeSchedule> findActiveEffectiveSchedules(@Param("date") LocalDate date);

  @Query(
      "SELECT f FROM FeeSchedule f WHERE f.academicYear = :academicYear ORDER BY f.classGrade, f.name")
  List<FeeSchedule> findByAcademicYearOrderByClassGrade(@Param("academicYear") String academicYear);

  @Query("SELECT f FROM FeeSchedule f WHERE f.status = 'ACTIVE' AND f.academicYear = :academicYear")
  List<FeeSchedule> findActiveByAcademicYear(@Param("academicYear") String academicYear);
}
