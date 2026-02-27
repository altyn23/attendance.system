package com.altynai.attendance.settings;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SystemSettingsRepository extends MongoRepository<SystemSettings, String> {
}
