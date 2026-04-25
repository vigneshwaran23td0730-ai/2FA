package com.auth.repository;

import com.auth.entity.OtpAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OtpAttemptRepository extends JpaRepository<OtpAttempt, Long> {
    
    @Query("SELECT COUNT(o) FROM OtpAttempt o WHERE o.userId = :userId AND o.type = :type AND o.createdAt > :since")
    long countByUserIdAndTypeAndCreatedAtAfter(@Param("userId") Long userId, 
                                               @Param("type") OtpAttempt.AttemptType type, 
                                               @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(o) FROM OtpAttempt o WHERE o.ipAddress = :ipAddress AND o.type = :type AND o.createdAt > :since")
    long countByIpAddressAndTypeAndCreatedAtAfter(@Param("ipAddress") String ipAddress, 
                                                  @Param("type") OtpAttempt.AttemptType type, 
                                                  @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(o) FROM OtpAttempt o WHERE o.userId = :userId AND o.type = :type AND o.result = :result AND o.createdAt > :since")
    long countByUserIdAndTypeAndResultAndCreatedAtAfter(@Param("userId") Long userId, 
                                                        @Param("type") OtpAttempt.AttemptType type, 
                                                        @Param("result") OtpAttempt.AttemptResult result, 
                                                        @Param("since") LocalDateTime since);
}