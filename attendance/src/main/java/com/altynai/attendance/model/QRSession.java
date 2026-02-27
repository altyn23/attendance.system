package com.altynai.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "qr_sessions")
public class QRSession {
    @Id
    private String id;
    
    private String classId; 
    private String className; 
    private String teacherId;
    private String teacherName;
    private String qrCode; 
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; 
    private boolean active;
    private String room;
    private List<String> scannedStudentIds; 
    
    public QRSession() {
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(15); 
        this.active = true;
        this.scannedStudentIds = new ArrayList<>();
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isActive() { return active && LocalDateTime.now().isBefore(expiresAt); }
    public void setActive(boolean active) { this.active = active; }
    
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    
    public List<String> getScannedStudentIds() { return scannedStudentIds; }
    public void setScannedStudentIds(List<String> scannedStudentIds) { this.scannedStudentIds = scannedStudentIds; }
    
    public void addScannedStudent(String studentId) {
        if (!this.scannedStudentIds.contains(studentId)) {
            this.scannedStudentIds.add(studentId);
        }
    }
}
