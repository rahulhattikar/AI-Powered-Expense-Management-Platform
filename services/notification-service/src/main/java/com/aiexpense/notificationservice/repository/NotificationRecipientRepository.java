package com.aiexpense.notificationservice.repository;

import com.aiexpense.notificationservice.entity.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NotificationRecipientRepository extends
        JpaRepository<NotificationRecipient, Long> {
}
