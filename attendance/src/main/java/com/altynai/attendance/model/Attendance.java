package com.altynai.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Document(collection = "attendance")
public class Attendance {
    @Id
    private String id;
    
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String classId;
    private String className;
    private String teacherId;
    private String teacherName;
    private String qrSessionId;
    private LocalDate date;
    private LocalDateTime checkInTime;
    private boolean present;
    private String status; 
    private String notes;
    private String group;
    private String semester;
    
    public Attendance() {
        this.date = LocalDate.now();
        this.checkInTime = LocalDateTime.now();
        this.present = true;
        this.status = "PRESENT";
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    
    public String getQrSessionId() { return qrSessionId; }
    public void setQrSessionId(String qrSessionId) { this.qrSessionId = qrSessionId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }
    
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
    
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
