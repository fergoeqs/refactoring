package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.HealthUpdateDTO;
import org.fergoeqs.coursework.models.HealthUpdate;
import org.fergoeqs.coursework.services.HealthUpdatesService;
import org.fergoeqs.coursework.services.PetsService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.HealthUpdateMapper;
import org.fergoeqs.coursework.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Health Updates", description = "API для управления обновлениями состояния здоровья")
@RestController
@RequestMapping("/api/health")
public class HealthUpdateController {
    private final HealthUpdatesService healthUpdateService;
    private final HealthUpdateMapper healthUpdateMapper;
    private final UserService userService;
    private final PetsService petsService;
    private static final Logger logger = LoggerFactory.getLogger(HealthUpdateController.class);

    public HealthUpdateController(HealthUpdatesService healthUpdateService, HealthUpdateMapper healthUpdateMapper,
                                  UserService userService, PetsService petsService) {
        this.healthUpdateService = healthUpdateService;
        this.healthUpdateMapper = healthUpdateMapper;
        this.userService = userService;
        this.petsService = petsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHealthUpdate(@PathVariable Long id) throws BadRequestException {
        try {
            HealthUpdate healthUpdate = healthUpdateService.findById(id);
            org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
            if (healthUpdate.getPet() != null) {
                SecurityUtils.checkResourceAccessThroughPet(currentUser, healthUpdate.getPet(), false);
            }
            return ResponseEntity.ok(healthUpdateMapper.toDTO(healthUpdate));
        } catch (Exception e) {
            logger.error("Error while fetching health update with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/first/{petId}")
    public ResponseEntity<?> getFirstHealthUpdate(@PathVariable Long petId) throws BadRequestException {
        try {
            org.fergoeqs.coursework.models.Pet pet = petsService.findPetById(petId);
            org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
            SecurityUtils.checkResourceAccessThroughPet(currentUser, pet, false);
            return ResponseEntity.ok(healthUpdateMapper.toDTO(healthUpdateService.findFirstByPet(petId)));
        } catch (Exception e) {
            logger.error("Error while fetching first health update for pet with id: {}", petId, e);
            throw e;
        }
    }

    @GetMapping("/all/{petId}")
    public ResponseEntity<?> getAllHealthUpdates(@PathVariable Long petId) throws BadRequestException {
        try {
            org.fergoeqs.coursework.models.Pet pet = petsService.findPetById(petId);
            org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
            SecurityUtils.checkResourceAccessThroughPet(currentUser, pet, false);
            return ResponseEntity.ok(healthUpdateMapper.toDTOs(healthUpdateService.findAllByPet(petId)));
        } catch (Exception e) {
            logger.error("Error while fetching all health updates for pet with id: {}", petId, e);
            throw e;
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveHealthUpdate(@RequestBody HealthUpdateDTO healthUpdateDTO) {
        try {
            return ResponseEntity.ok(healthUpdateMapper.toDTO(healthUpdateService.save(healthUpdateDTO)));
        } catch (Exception e) {
            logger.error("Error while saving health update: {}", healthUpdateDTO, e);
            throw e;
        }
    }
}
