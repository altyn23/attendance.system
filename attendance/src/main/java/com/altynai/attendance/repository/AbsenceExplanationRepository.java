package com.altynai.attendance.repository;

import com.altynai.attendance.model.AbsenceExplanation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AbsenceExplanationRepository extends MongoRepository<AbsenceExplanation, String> {
    Optional<AbsenceExplanation> findByAttendanceId(String attendanceId);
    List<AbsenceExplanation> findByStudentIdOrderByCreatedAtDesc(String studentId);
    List<AbsenceExplanation> findByStatusOrderByCreatedAtDesc(String status);
}
