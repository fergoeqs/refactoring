package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.exception.ValidationException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@Transactional
public class AvatarService {
    private final StorageService storageService;
    private final UserRepository userRepository;

    public AvatarService(StorageService storageService, UserRepository userRepository) {
        this.storageService = storageService;
        this.userRepository = userRepository;
    }


    public void updateUserAvatar(AppUser user, MultipartFile avatar) throws IOException {
        validateAvatarFile(avatar);
        
        String objectName = "avatar/" + user.getId();
        String contentType = avatar.getContentType();
        storageService.uploadFile("users", objectName, avatar.getInputStream(), contentType);
        user.setPhotoUrl(storageService.generatePublicUrl("users", objectName));
        userRepository.save(user);
    }


    private void validateAvatarFile(MultipartFile avatar) {
        String contentType = avatar.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new ValidationException("Invalid file type. Only PNG and JPEG are allowed.");
        }
    }
}
