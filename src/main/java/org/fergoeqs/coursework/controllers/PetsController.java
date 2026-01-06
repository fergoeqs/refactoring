package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.AppUserDTO;
import org.fergoeqs.coursework.dto.PetDTO;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.services.PetsService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.fergoeqs.coursework.utils.Mappers.PetMapper;
import org.fergoeqs.coursework.utils.SecurityUtils;
import org.springframework.http.HttpStatus;
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

    public PetsController(PetMapper petMapper, PetsService petsService, UserService userService,
                           AppUserMapper appUserMapper) {
        this.petMapper = petMapper;
        this.petsService = petsService;
        this.userService = userService;
        this.appUserMapper = appUserMapper;
    }

    @Operation(summary = "Получить всех питомцев", description = "Возвращает список всех питомцев в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список питомцев успешно получен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-pets")
    public List<PetDTO> getAllPets() throws BadRequestException {
        List<Pet> pets = petsService.findAllPets();
        return petMapper.petsToPetDTOs(pets);
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
    public PetDTO getPet(@Parameter(description = "ID питомца") @PathVariable("petId") Long petId) throws BadRequestException {
        Pet pet = petsService.findPetById(petId);
        AppUser currentUser = userService.getAuthenticatedUser();
        SecurityUtils.checkPetAccess(currentUser, pet, false);
        return petMapper.petToPetDTO(pet);
    }

    @Operation(summary = "Получить питомцев текущего пользователя", description = "Возвращает список питомцев текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список питомцев успешно получен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user-pets")
    public List<PetDTO> getUserPets() throws BadRequestException {
        return petMapper.petsToPetDTOs(
                petsService.findPetsByOwner(userService.getAuthenticatedUser().getId()));
    }

    @Operation(summary = "Получить питомцев ветеринара", description = "Возвращает список питомцев, привязанных к ветеринару")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список питомцев успешно получен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/doctor-pets/{vetId}")
    public List<PetDTO> getDoctorPets(@Parameter(description = "ID ветеринара") @PathVariable Long vetId) throws BadRequestException {
        return petMapper.petsToPetDTOs(petsService.findPetsByVet(vetId));
    }

    @Operation(summary = "Получить владельца питомца", description = "Возвращает информацию о владельце питомца")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Владелец найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Питомец не найден")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}/owner")
    public AppUserDTO getOwner(@Parameter(description = "ID питомца") @PathVariable("id") Long id) throws BadRequestException {
        Pet pet = petsService.findPetById(id);
        return appUserMapper.toDTO(pet.getOwner());
    }

    @Operation(summary = "Создать нового питомца", description = "Создает нового питомца для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Питомец успешно создан",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/new-pet")
    @ResponseStatus(HttpStatus.CREATED)
    public PetDTO createPet(@Valid @RequestBody PetDTO petDTO) throws BadRequestException {
        Pet createdPet = petsService.addPet(petDTO, userService.getAuthenticatedUser());
        return petMapper.petToPetDTO(createdPet);
    }

    @Operation(summary = "Обновить питомца", description = "Обновляет информацию о питомце")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Питомец успешно обновлен",
                    content = @Content(schema = @Schema(implementation = PetDTO.class))),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-pet/{petId}")
    public PetDTO updatePet(@Valid @RequestBody PetDTO petDTO,
                            @Parameter(description = "ID питомца") @PathVariable("petId") Long petId) throws BadRequestException {
        Pet pet = petsService.findPetById(petId);
        AppUser currentUser = userService.getAuthenticatedUser();
        SecurityUtils.checkPetAccess(currentUser, pet, true);
        Pet updatedPet = petsService.updatePet(petId, currentUser, petDTO);
        return petMapper.petToPetDTO(updatedPet);
    }

    @Operation(summary = "Привязать питомца к ветеринару", description = "Привязывает питомца к текущему ветеринару")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Питомец успешно привязан"),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_VET')")
    @PutMapping("/{id}/bind")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bindPet(@Parameter(description = "ID питомца") @PathVariable("id") Long id) throws BadRequestException {
        petsService.bindPet(id, userService.getAuthenticatedUser());
    }

    @Operation(summary = "Поместить питомца в сектор", description = "Помещает питомца в указанный сектор")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Питомец успешно помещен в сектор"),
            @ApiResponse(responseCode = "404", description = "Питомец или сектор не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PutMapping("/{id}/sector")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setSectorPlace(@Parameter(description = "ID питомца") @PathVariable("id") Long id,
                               @Parameter(description = "ID сектора") @RequestParam Long sectorId) throws BadRequestException {
        petsService.placeInSector(id, sectorId);
    }

    @Operation(summary = "Отвязать питомца от ветеринара", description = "Отвязывает питомца от ветеринара")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Питомец успешно отвязан"),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PutMapping("/{id}/unbind")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unbindPet(@Parameter(description = "ID питомца") @PathVariable("id") Long id) throws BadRequestException {
        petsService.unbindPet(id);
    }

    @Operation(summary = "Обновить аватар питомца", description = "Обновляет фотографию питомца")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Аватар успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "400", description = "Некорректный файл")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-avatar/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePetAvatar(@Parameter(description = "ID питомца") @PathVariable("petId") Long petId,
                                @RequestParam("avatar") MultipartFile avatar) throws IOException, BadRequestException {
        Pet pet = petsService.findPetById(petId);
        AppUser currentUser = userService.getAuthenticatedUser();
        SecurityUtils.checkPetAccess(currentUser, pet, true);
        petsService.updatePetAvatar(petId, avatar);
    }

    @Operation(summary = "Удалить питомца", description = "Удаляет питомца из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Питомец успешно удален"),
            @ApiResponse(responseCode = "404", description = "Питомец не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @DeleteMapping("/delete-pet/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePet(@Parameter(description = "ID питомца") @PathVariable("petId") Long petId) throws BadRequestException {
        petsService.deletePet(petId, userService.getAuthenticatedUser());
    }



}
