package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.*;
import org.fergoeqs.coursework.exception.ForbiddenException;
import org.fergoeqs.coursework.exception.InternalServerErrorException;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.exception.UnauthorizedAccessException;
import org.fergoeqs.coursework.jwt.JwtService;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.enums.RoleType;
import org.fergoeqs.coursework.services.AuthenticationService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Tag(name = "Users", description = "API для управления пользователями")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final AppUserMapper appUserMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    public UserController(UserService userService, JwtService jwtService, AuthenticationService authenticationService,
                          AppUserMapper appUserMapper) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.appUserMapper = appUserMapper;
    }

    @Operation(summary = "Получить всех владельцев", description = "Возвращает список всех пользователей с ролью OWNER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список владельцев успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-owners")
    public ResponseEntity<?> getAllOwners() {
        try {
            return ResponseEntity.ok(appUserMapper.toDTOs(userService.findByRole(RoleType.ROLE_OWNER)));
        } catch (Exception e) {
            logger.error("Error getting owners: {}", e.getMessage());
            throw new InternalServerErrorException("Error getting owners");
        }
    }

    @Operation(summary = "Получить всех ветеринаров", description = "Возвращает список всех пользователей с ролью VET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ветеринаров успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-vets")
    public ResponseEntity<?> getAllVets() {
        try {
            return ResponseEntity.ok(appUserMapper.toDTOs(userService.findByRole(RoleType.ROLE_VET)));
        } catch (Exception e) {
            logger.error("Error getting vets: {}", e.getMessage());
            throw new InternalServerErrorException("Error getting owners");
        }
    }

    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя и возвращает JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AuthenticationSucceedDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody RegisterUserDTO user) {

        if (user.password() == null || user.password().isBlank()) {
            throw new ValidationException("Password cannot be blank");
        }
        if (user.phoneNumber() == null || user.phoneNumber().isBlank()) {
            throw new ValidationException("Phone number cannot be blank");
        }
        if (user.email() == null || user.email().isBlank()) {
            throw new ValidationException("Email cannot be blank");
        }

        try {
            AppUser createdUser = authenticationService.signup(user);
            String jwtToken = jwtService.generateToken(createdUser);
            AuthenticationSucceedDto succeedDto = new AuthenticationSucceedDto(jwtToken, jwtService.getExpirationTime());
            return ResponseEntity.ok(succeedDto);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage());
            throw new InternalServerErrorException("Registration failed");
        }
    }


    @Operation(summary = "Аутентификация пользователя", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аутентификация успешна",
                    content = @Content(schema = @Schema(implementation = AuthenticationSucceedDto.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationSucceedDto> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        try {
            AppUser authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            AuthenticationSucceedDto authenticationSucceedDto = new AuthenticationSucceedDto(jwtToken, jwtService.getExpirationTime());
            return ResponseEntity.ok(authenticationSucceedDto);
        } catch (Exception e) {
            logger.error("Authentication failed: {}", e.getMessage());
            throw new UnauthorizedAccessException("Authentication failed");
        }
    }

    @Operation(summary = "Обновить аватар пользователя", description = "Загружает новый аватар для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-avatar")
    public ResponseEntity<?> updateUserAvatar(
            @Parameter(description = "Файл изображения аватара") @RequestParam("avatar") MultipartFile avatar) {
        try {
            AppUser user = userService.getAuthenticatedUser();
            userService.updateUserAvatar(user, avatar);
            return ResponseEntity.ok("Avatar updated");
        } catch (Exception e) {
            logger.error("Error updating avatar: {}", e.getMessage());
            throw new InternalServerErrorException("Error updating avatar");
        }
    }

    @Operation(summary = "Обновить информацию о пользователе", description = "Обновляет данные текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-user/")
    public ResponseEntity<?> updateUser(@RequestBody AppUserDTO userDTO) {
        try {
            AppUser updatedUser = userService.updateUser(userService.getAuthenticatedUser(), userDTO);
            return ResponseEntity.ok(appUserMapper.toDTO(updatedUser));
        } catch (Exception e) {
            logger.error("Error updating personal info: {}", e.getMessage());
            throw new InternalServerErrorException("Error updating user");
        }
    }

    @Operation(summary = "Обновить пользователя (админ)", description = "Обновляет данные пользователя (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update-user-admin/{id}")
    public ResponseEntity<?> updateUserForAdmin(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @RequestBody AppUserDTO userDTO) {
        try {
            AppUser updatedUser = userService.updateUserForAdmin(id, userDTO);
            return ResponseEntity.ok(appUserMapper.toDTO(updatedUser));
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new InternalServerErrorException("Error updating user");
        }
    }

    @Operation(summary = "Обновить роли пользователя", description = "Обновляет роли пользователя (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роли успешно обновлены",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update-roles/{id}")
    public ResponseEntity<?> updateRoles(
            @Parameter(description = "ID пользователя") @PathVariable Long id,
            @RequestBody RoleType userDTO) {
        try {
            AppUser updatedUser = userService.updateUserRoles(id, userDTO);
            return ResponseEntity.ok(appUserMapper.toDTO(updatedUser));
        } catch (Exception e) {
            logger.error("Error updating roles: {}", e.getMessage());
            throw new InternalServerErrorException("Error updating roles");
        }
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает информацию о пользователе по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@Parameter(description = "ID пользователя") @PathVariable Long id) throws BadRequestException {
        try {
            AppUser user = userService.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
            
            AppUser currentUser = userService.getAuthenticatedUser();
            if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(RoleType.ROLE_ADMIN)) {
                throw new ForbiddenException("Access denied");
            }
            
            return ResponseEntity.ok(appUserMapper.toDTO(user));
        } catch (Exception e) {
            logger.error("Error getting user by id: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public void deleteUser(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/get-users")
    public ResponseEntity<?> getAllUsers() throws BadRequestException {
        AppUser user = userService.getAuthenticatedUser();
        logger.info("Getting users for user: {}", user.getUsername());

        return ResponseEntity.ok(userService.findAllUsers());
    }

    @Operation(summary = "Получить информацию о текущем пользователе", description = "Возвращает ID и роль текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация успешно получена",
                    content = @Content(schema = @Schema(implementation = UserInfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/current-user-info")
    public ResponseEntity<UserInfoDTO> getCurrentUserInfo() throws BadRequestException {
        AppUser user = userService.getAuthenticatedUser();
        logger.info("Fetching ID and role for authenticated user: {}", user.getUsername());

        String role = user.getPrimaryRole().stream()
                .findFirst()
                .map(Enum::name)
                .orElse("USER");

        UserInfoDTO userInfo = new UserInfoDTO(user.getId(), role);
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "Получить DTO пользователя по ID", description = "Возвращает DTO пользователя по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user-info/{id}")
    public ResponseEntity<?> getUserDtoById(@Parameter(description = "ID пользователя") @PathVariable Long id) throws BadRequestException {
        try {
            AppUser user = userService.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
            return ResponseEntity.ok(appUserMapper.toDTO(user));
        } catch (Exception e) {
            logger.error("Error during user fetching: {}", e.getMessage());
            throw e;
        }
    }


}