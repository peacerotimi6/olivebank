package com.webapp.bankingportal.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webapp.bankingportal.entity.Notification;
import com.webapp.bankingportal.entity.User;
import com.webapp.bankingportal.exception.NotFoundException;
import com.webapp.bankingportal.repository.NotificationRepository;
import com.webapp.bankingportal.repository.UserRepository;
import com.webapp.bankingportal.util.LoggedinUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getLoggedInUser() {
        String accountNumber = LoggedinUser.getAccountNumber();

        return userRepository.findByAccountAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException(
                        "The logged-in customer's account could not be found."));
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications() {
        User user = getLoggedInUser();

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User user = getLoggedInUser();

        return notificationRepository.countByUser_IdAndReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        User user = getLoggedInUser();

        Notification notification = notificationRepository
                .findByIdAndUser_Id(notificationId, user.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Notification not found."));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public Notification createNotification(
            Long userId, String title, String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found."));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);

        return notificationRepository.save(notification);
    }
}
