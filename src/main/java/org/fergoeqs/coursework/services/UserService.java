package org.fergoeqs.coursework.services;

import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.AppUserDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.exception.UnauthorizedAccessException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.enums.RoleType;
import org.fergoeqs.coursework.repositories.UserRepository;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final AppUserMapper appUserMapper;
    private final ClinicsService clinicsService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StorageService storageService,
                       AppUserMapper appUserMapper, ClinicsService clinicsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
        this.appUserMapper = appUserMapper;
        this.clinicsService = clinicsService;
    }

    public AppUser registerUser(String username, String password) {
        Optional<AppUser> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }

        AppUser user = new AppUser(username, passwordEncoder.encode(password));
        user.getRoles().add(RoleType.ROLE_USER);
        return userRepository.save(user);
    }

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<AppUser> findByRole(RoleType role) {
        return userRepository.findByRolesContaining(role);
    }
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<AppUser> authenticate(String username, String password) {
        Optional<AppUser> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            AppUser user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public AppUser getAuthenticatedUser() throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();
            return findByUsername(username)
                    .orElseThrow(() -> new BadRequestException("User not found"));
        }
        throw new UnauthorizedAccessException("User not authenticated");
    }

    public void updateUserAvatar(AppUser user, MultipartFile avatar) throws IOException {
        String contentType = avatar.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Invalid file type. Only PNG and JPEG are allowed.");
        }
        String objectName = "avatar/" + user.getId();
        storageService.uploadFile("users", objectName, avatar.getInputStream(), contentType);
        user.setPhotoUrl(storageService.generatePublicUrl("users", objectName));
        userRepository.save(user);
    }

    public  AppUser updateUser(AppUser appUser, AppUserDTO userDTO) {
        appUserMapper.updateUserFromDTO(userDTO, appUser);
        return userRepository.save(appUser);
    }

    public  AppUser updateUserForAdmin(Long id, AppUserDTO userDTO) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        appUserMapper.updateUserForAdminFromDTO(userDTO, user);
        if (userDTO.clinic() == null) {
            user.setClinic(null);
        } else {
            user.setClinic(clinicsService.findById(userDTO.clinic()));}
        return userRepository.save(user);
    }

    public AppUser updateUserRoles(Long id, RoleType newRole) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);
        return userRepository.save(user);
    }

    public List<AppUser> findAllUsers() {
        return userRepository.findAll();
    }

    public boolean isVet(AppUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_VET"));
    }

    public boolean isAdmin(AppUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

//TODO: роль юзер менять на овнер, если имеет хотя бы одного пета.
}