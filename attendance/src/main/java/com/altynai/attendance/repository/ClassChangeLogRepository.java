package com.altynai.attendance.repository;

import com.altynai.attendance.model.ClassChangeLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ClassChangeLogRepository extends MongoRepository<ClassChangeLog, String> {
    List<ClassChangeLog> findByClassIdOrderByCreatedAtDesc(String classId);
}
