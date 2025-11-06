package com.altynai.attendance.account;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String email;

    private String firstName;
    private String lastName;
    private String phone;
    private String passwordHash;
    private String role = "STUDENT"; 
    
    private String department; 
    private String group; 
    private String profileImage; 
    private java.time.LocalDateTime registrationDate;
    private java.time.LocalDateTime lastLoginDate;
    
    private String resetCode;
    private Long resetCodeExpires;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public java.time.LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(java.time.LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public java.time.LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(java.time.LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }

    public Long getResetCodeExpires() { return resetCodeExpires; }
    public void setResetCodeExpires(Long resetCodeExpires) { this.resetCodeExpires = resetCodeExpires; }
    
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}
