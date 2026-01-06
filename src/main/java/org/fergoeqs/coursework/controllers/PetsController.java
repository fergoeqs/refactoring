package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.PetDTO;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.services.PetsService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.fergoeqs.coursework.utils.Mappers.PetMapper;
import org.fergoeqs.coursework.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Pets", description = "API для управления питомцами")
@RestController
@RequestMapping("/api/pets")
public class PetsController {
    private final PetMapper petMapper;
    private final PetsService petsService;
    private final UserService userService;
    private final AppUserMapper appUserMapper;
    private static final Logger logger = LoggerFactory.getLogger(PetsController.class);

    public PetsController (PetMapper petMapper, PetsService petsService, UserService userService,
                           AppUserMapper appUserMapper) {
        this.petMapper = petMapper;
        this.petsService = petsService;
        this.userService = userService;
        this.appUserMapper = appUserMapper;
    }

    @Operation(summary = "Получить всех питомцев", description = "Возвращает список всех питомцев в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список питомцев успешно получен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-pets")
    public ResponseEntity<?> getAllPets(){
        try {
            List<Pet> pets = petsService.findAllPets();
            return ResponseEntity.ok(petMapper.petsToPetDTOs(pets));

        } catch (Exception e) {
            logger.error("Pets fetching failed: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Получить питомца по ID", description = "Возвращает информацию о питомце по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Питомец найден",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/pet/{petId}")
    public ResponseEntity<?> getPet(@Parameter(description = "ID питомца") @PathVariable Long petId) throws BadRequestException {
        try {
            Pet pet = petsService.findPetById(petId);
            AppUser currentUser = userService.getAuthenticatedUser();
            SecurityUtils.checkPetAccess(currentUser, pet, false);
            return ResponseEntity.ok(petMapper.petToPetDTO(pet));
        } catch (Exception e) {
            logger.error("Pet fetching failed: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Получить питомцев текущего пользователя", description = "Возвращает список питомцев текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список питомцев успешно получен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user-pets")
    public ResponseEntity<?> getUserPets() throws BadRequestException {
        try {
            return ResponseEntity.ok(petMapper.petsToPetDTOs(
                    petsService.findPetsByOwner(userService.getAuthenticatedUser().getId())));
        } catch (Exception e) {
            logger.error("User pets fetching failed: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/doctor-pets/{vetId}")
    public ResponseEntity<?> getDoctorPets(@PathVariable Long vetId) {
        try {
            return ResponseEntity.ok(petMapper.petsToPetDTOs(
                    petsService.findPetsByVet(vetId)));
        } catch (Exception e) {
            logger.error("Doctor pets fetching failed: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/owner/{petId}")
    public ResponseEntity<?> getOwner(@PathVariable Long petId) {
        try {
            return ResponseEntity.ok(appUserMapper.toDTO(petsService.findPetById(petId).getOwner()));
        } catch (Exception e) {
            logger.error("Owner fetching failed: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Создать нового питомца", description = "Создает нового питомца для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Питомец успешно создан",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/new-pet")
    public ResponseEntity<?> createPet(@RequestBody PetDTO petDTO) throws BadRequestException {
        try {
            return ResponseEntity.ok(petMapper.petToPetDTO(petsService.addPet(petDTO, userService.getAuthenticatedUser())));
        } catch (Exception e) {
            logger.error("Pet creation failed: {}", e.getMessage());
            throw e; //TODO: переписать catch, когда будут валидаторы.
        }
    }

    @Operation(summary = "Обновить питомца", description = "Обновляет информацию о питомце")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Питомец успешно обновлен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-pet/{petId}")
    public ResponseEntity<?> updatePet(@RequestBody PetDTO petDTO,
                                       @Parameter(description = "ID питомца") @PathVariable Long petId) throws BadRequestException {
        try {
            Pet pet = petsService.findPetById(petId);
            AppUser currentUser = userService.getAuthenticatedUser();
            SecurityUtils.checkPetAccess(currentUser, pet, true);
            Pet updatedPet = petsService.updatePet(petId, currentUser, petDTO);
            return ResponseEntity.ok(petMapper.petToPetDTO(updatedPet));
        } catch (Exception e) {
            logger.error("Pet updating failed: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasRole('ROLE_VET')")
    @PutMapping("/bind/{petId}")
    public ResponseEntity<?> bindPet(@PathVariable Long petId) throws BadRequestException {
        try {
            petsService.bindPet(petId, userService.getAuthenticatedUser());
            return ResponseEntity.ok("Pet " + petId + " bound");
        } catch (Exception e) {
            logger.error("Pet binding failed: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PutMapping("/sector-place/{petId}")
    public ResponseEntity<?> setSectorPlace(@PathVariable Long petId, @RequestParam Long sectorId) {
        try {
            petsService.placeInSector(petId, sectorId);
            return ResponseEntity.ok("Pet" + petId + " set to sector " + sectorId);
        } catch (Exception e) {
            logger.error("Pet sector setting failed: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PutMapping("/unbind/{petId}")
    public ResponseEntity<?> unbindPet(@PathVariable Long petId) throws BadRequestException {
        try {
            petsService.unbindPet(petId);
            return ResponseEntity.ok("Pet" + petId + " unbound");
        } catch (Exception e) {
            logger.error("Pet unbinding failed: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/update-avatar/{petId}")
    public ResponseEntity<?> updatePetAvatar(@PathVariable Long petId, @RequestParam("avatar") MultipartFile avatar) throws IOException {
        try {
            petsService.updatePetAvatar(petId, avatar);
            return ResponseEntity.ok("Pet " + petId + " avatar updated");
        } catch (Exception e) {
            logger.error("Pet avatar updating failed: {}", e.getMessage());
            throw e; //TODO: проверять, что может делать только врач или овнер
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @DeleteMapping("/delete-pet/{petId}")
    public ResponseEntity<?> deletePet(@PathVariable Long petId) throws BadRequestException {
        try {
            petsService.deletePet(petId, userService.getAuthenticatedUser());
            return ResponseEntity.ok("Pet" + petId + " deleted");
        } catch (Exception e) {
            logger.error("Pet deleting failed: {}", e.getMessage());
            throw e;
        }
    }



}
