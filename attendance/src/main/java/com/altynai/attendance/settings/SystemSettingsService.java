package com.altynai.attendance.settings;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SystemSettingsService {
    private static final String GLOBAL_ID = "global";
    private final SystemSettingsRepository repository;

    public SystemSettingsService(SystemSettingsRepository repository) {
        this.repository = repository;
    }

    public SystemSettings getSettings() {
        return repository.findById(GLOBAL_ID).orElseGet(() -> {
            SystemSettings defaults = new SystemSettings();
            defaults.setId(GLOBAL_ID);
            return repository.save(defaults);
        });
    }

    public SystemSettings updateSettings(Map<String, Object> newSettings) {
        SystemSettings settings = getSettings();

        if (newSettings.get("qrCodeDuration") instanceof Number n) {
            int value = n.intValue();
            if (value >= 1 && value <= 3600) {
                settings.setQrCodeDuration(value);
            }
        }
        if (newSettings.get("lateThreshold") instanceof Number n) {
            int value = n.intValue();
            if (value >= 0 && value <= 180) {
                settings.setLateThreshold(value);
            }
        }
        if (newSettings.get("minAttendanceRate") instanceof Number n) {
            int value = n.intValue();
            if (value >= 0 && value <= 100) {
                settings.setMinAttendanceRate(value);
            }
        }
        if (newSettings.get("semesterStart") instanceof String v && !v.isBlank()) {
            settings.setSemesterStart(v);
        }
        if (newSettings.get("semesterEnd") instanceof String v && !v.isBlank()) {
            settings.setSemesterEnd(v);
        }
        if (newSettings.get("systemVersion") instanceof String v && !v.isBlank()) {
            settings.setSystemVersion(v);
        }
        if (newSettings.get("databaseType") instanceof String v && !v.isBlank()) {
            settings.setDatabaseType(v);
        }
        if (newSettings.get("maxUploadSize") instanceof String v && !v.isBlank()) {
            settings.setMaxUploadSize(v);
        }

        settings.setUpdatedAt(LocalDateTime.now());
        return repository.save(settings);
    }

    public String currentSemesterCode() {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        if (month >= 9) {
            return now.getYear() + "-Fall";
        }
        if (month >= 1 && month <= 5) {
            return now.getYear() + "-Spring";
        }
        return now.getYear() + "-Summer";
    }
}
