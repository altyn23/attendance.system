package com.altynai.attendance.repository;

import com.altynai.attendance.model.Class;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ClassRepository extends MongoRepository<Class, String> {
    List<Class> findByTeacherId(String teacherId);
    List<Class> findByGroup(String group);
    List<Class> findByDepartment(String department);
    List<Class> findByDayOfWeek(DayOfWeek dayOfWeek);
    List<Class> findByGroupAndDayOfWeek(String group, DayOfWeek dayOfWeek);
    List<Class> findByTeacherIdAndDayOfWeek(String teacherId, DayOfWeek dayOfWeek);
    List<Class> findBySemester(String semester);
}
