package com.altynai.attendance.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "groups")
public class AcademicGroup {
    @Id
    private String id;
    private String groupCode;
    private String groupNameRu;
    private String groupNameKz;
    private int course;
    private String curatorTeacherId;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getGroupNameRu() { return groupNameRu; }
    public void setGroupNameRu(String groupNameRu) { this.groupNameRu = groupNameRu; }

    public String getGroupNameKz() { return groupNameKz; }
    public void setGroupNameKz(String groupNameKz) { this.groupNameKz = groupNameKz; }

    public int getCourse() { return course; }
    public void setCourse(int course) { this.course = course; }

    public String getCuratorTeacherId() { return curatorTeacherId; }
    public void setCuratorTeacherId(String curatorTeacherId) { this.curatorTeacherId = curatorTeacherId; }
}
