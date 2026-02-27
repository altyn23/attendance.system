package com.altynai.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "class_change_logs")
public class ClassChangeLog {
    @Id
    private String id;
    private String classId;
    private String action;
    private String reason;
    private String changedByUserId;
    private String oldTeacherId;
    private String newTeacherId;
    private String oldRoom;
    private String newRoom;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getChangedByUserId() { return changedByUserId; }
    public void setChangedByUserId(String changedByUserId) { this.changedByUserId = changedByUserId; }

    public String getOldTeacherId() { return oldTeacherId; }
    public void setOldTeacherId(String oldTeacherId) { this.oldTeacherId = oldTeacherId; }

    public String getNewTeacherId() { return newTeacherId; }
    public void setNewTeacherId(String newTeacherId) { this.newTeacherId = newTeacherId; }

    public String getOldRoom() { return oldRoom; }
    public void setOldRoom(String oldRoom) { this.oldRoom = oldRoom; }

    public String getNewRoom() { return newRoom; }
    public void setNewRoom(String newRoom) { this.newRoom = newRoom; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
