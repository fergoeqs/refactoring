package org.fergoeqs.coursework.services;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.Notification;
import org.fergoeqs.coursework.repositories.NotificationRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate,
                               JavaMailSender mailSender, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.mailSender = mailSender;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    public List<Notification> findAllByUserId(Long userId) {
        return notificationRepository.findAllByAppUserId(userId);
    }

    public void sendWebNotificationToUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(username, "/topic/notifications", message);
    }

    public void sendEmailNotification(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("coshkodevochka@yandex.ru");
        mailSender.send(message);
    }

    public void sendNotification(Long userId, String message, String email) {
        sendWebNotificationToUser(userId.toString(), message);
        sendEmailNotification(email, "VetCare", message);
        Notification notification = new Notification();
        notification.setAppUser(userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId)));
        notification.setContent(message);
        notificationRepository.save(notification);
    }
}
