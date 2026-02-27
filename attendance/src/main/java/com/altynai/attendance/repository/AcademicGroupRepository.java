package com.altynai.attendance.repository;

import com.altynai.attendance.model.AcademicGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcademicGroupRepository extends MongoRepository<AcademicGroup, String> {
}
