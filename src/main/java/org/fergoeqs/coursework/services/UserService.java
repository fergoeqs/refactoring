package org.fergoeqs.coursework.services;

import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.AppUserDTO;
import org.fergoeqs.coursework.exception.BusinessException;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.exception.UnauthorizedAccessException;
import org.fergoeqs.coursework.exception.ValidationException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.enums.RoleType;
import org.fergoeqs.coursework.repositories.UserRepository;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AppUserMapper appUserMapper;
    private final ClinicsService clinicsService;

    public UserService(UserRepository userRepository, AppUserMapper appUserMapper, ClinicsService clinicsService) {
        this.userRepository = userRepository;
        this.appUserMapper = appUserMapper;
        this.clinicsService = clinicsService;
    }

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Cacheable(value = "usersByRole", key = "#role.name()")
    public List<AppUser> findByRole(RoleType role) {
        return userRepository.findByRolesContaining(role);
    }

    @Transactional
    @CacheEvict(value = {"usersByRole", "allUsers"}, allEntries = true)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
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

    @Transactional
    @CacheEvict(value = {"usersByRole", "allUsers"}, allEntries = true)
    public AppUser updateUser(AppUser appUser, AppUserDTO userDTO) {
        appUserMapper.updateUserFromDTO(userDTO, appUser);
        return userRepository.save(appUser);
    }

    @Transactional
    @CacheEvict(value = {"usersByRole", "allUsers"}, allEntries = true)
    public AppUser updateUserForAdmin(Long id, AppUserDTO userDTO) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        appUserMapper.updateUserForAdminFromDTO(userDTO, user);
        if (userDTO.clinic() == null) {
            user.setClinic(null);
        } else {
            user.setClinic(clinicsService.findById(userDTO.clinic()));
        }
        return userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = {"usersByRole", "allUsers"}, allEntries = true)
    public AppUser updateUserRoles(Long id, RoleType newRole) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.getRoles().clear();
        user.getRoles().add(newRole);
        return userRepository.save(user);
    }

    @Cacheable(value = "allUsers")
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