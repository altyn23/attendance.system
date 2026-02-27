package com.altynai.attendance.settings;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "system_settings")
public class SystemSettings {
    @Id
    private String id;

    private int qrCodeDuration = 15;
    private int lateThreshold = 10;
    private int minAttendanceRate = 75;
    private String semesterStart = "2024-09-01";
    private String semesterEnd = "2025-01-31";
    private String systemVersion = "1.0.0";
    private String databaseType = "MongoDB";
    private String maxUploadSize = "10MB";
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getQrCodeDuration() { return qrCodeDuration; }
    public void setQrCodeDuration(int qrCodeDuration) { this.qrCodeDuration = qrCodeDuration; }
    public int getLateThreshold() { return lateThreshold; }
    public void setLateThreshold(int lateThreshold) { this.lateThreshold = lateThreshold; }
    public int getMinAttendanceRate() { return minAttendanceRate; }
    public void setMinAttendanceRate(int minAttendanceRate) { this.minAttendanceRate = minAttendanceRate; }
    public String getSemesterStart() { return semesterStart; }
    public void setSemesterStart(String semesterStart) { this.semesterStart = semesterStart; }
    public String getSemesterEnd() { return semesterEnd; }
    public void setSemesterEnd(String semesterEnd) { this.semesterEnd = semesterEnd; }
    public String getSystemVersion() { return systemVersion; }
    public void setSystemVersion(String systemVersion) { this.systemVersion = systemVersion; }
    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }
    public String getMaxUploadSize() { return maxUploadSize; }
    public void setMaxUploadSize(String maxUploadSize) { this.maxUploadSize = maxUploadSize; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
