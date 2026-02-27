package com.altynai.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "schedule_change_requests")
public class ScheduleChangeRequest {
    @Id
    private String id;
    private String requesterTeacherId;
    private String requesterTeacherName;
    private String targetAdminId;
    private String classId;
    private String type;
    private LocalDate requestedDate;
    private String newTeacherId;
    private String newRoom;
    private String reason;
    private String status = "PENDING";
    private String adminComment;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime processedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRequesterTeacherId() { return requesterTeacherId; }
    public void setRequesterTeacherId(String requesterTeacherId) { this.requesterTeacherId = requesterTeacherId; }

    public String getRequesterTeacherName() { return requesterTeacherName; }
    public void setRequesterTeacherName(String requesterTeacherName) { this.requesterTeacherName = requesterTeacherName; }

    public String getTargetAdminId() { return targetAdminId; }
    public void setTargetAdminId(String targetAdminId) { this.targetAdminId = targetAdminId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public String getNewTeacherId() { return newTeacherId; }
    public void setNewTeacherId(String newTeacherId) { this.newTeacherId = newTeacherId; }

    public String getNewRoom() { return newRoom; }
    public void setNewRoom(String newRoom) { this.newRoom = newRoom; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminComment() { return adminComment; }
    public void setAdminComment(String adminComment) { this.adminComment = adminComment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
