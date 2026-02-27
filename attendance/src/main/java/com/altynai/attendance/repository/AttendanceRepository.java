package com.altynai.attendance.repository;

import com.altynai.attendance.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    List<Attendance> findByStudentId(String studentId);
    List<Attendance> findByStudentIdOrderByDateDescCheckInTimeDesc(String studentId);
    List<Attendance> findByClassId(String classId);
    List<Attendance> findByTeacherId(String teacherId);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByStudentIdAndDate(String studentId, LocalDate date);
    List<Attendance> findByClassIdAndDate(String classId, LocalDate date);
    List<Attendance> findByStudentIdAndClassId(String studentId, String classId);
    Optional<Attendance> findByStudentIdAndClassIdAndDate(String studentId, String classId, LocalDate date);
    Optional<Attendance> findByStudentIdAndQrSessionId(String studentId, String qrSessionId);
    List<Attendance> findByGroupAndDate(String group, LocalDate date);
    List<Attendance> findByStudentIdAndSemester(String studentId, String semester);
}
