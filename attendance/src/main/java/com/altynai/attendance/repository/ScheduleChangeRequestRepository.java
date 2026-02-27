package com.altynai.attendance.repository;

import com.altynai.attendance.model.ScheduleChangeRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduleChangeRequestRepository extends MongoRepository<ScheduleChangeRequest, String> {
    List<ScheduleChangeRequest> findByTargetAdminIdOrderByCreatedAtDesc(String targetAdminId);
    List<ScheduleChangeRequest> findByRequesterTeacherIdOrderByCreatedAtDesc(String requesterTeacherId);
    List<ScheduleChangeRequest> findByStatusOrderByCreatedAtDesc(String status);
}
