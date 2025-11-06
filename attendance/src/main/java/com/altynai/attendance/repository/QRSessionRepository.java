package com.altynai.attendance.repository;

import com.altynai.attendance.model.QRSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QRSessionRepository extends MongoRepository<QRSession, String> {
    Optional<QRSession> findByQrCode(String qrCode);
    Optional<QRSession> findByQrCodeAndActive(String qrCode, boolean active);
    List<QRSession> findByTeacherId(String teacherId);
    List<QRSession> findByClassId(String classId);
    List<QRSession> findByActive(boolean active);
}
