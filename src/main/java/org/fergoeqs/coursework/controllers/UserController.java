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
import org.fergoeqs.coursework.dto.*;
import org.fergoeqs.coursework.exception.ForbiddenException;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.jwt.JwtService;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.enums.RoleType;
import org.fergoeqs.coursework.services.AuthenticationService;
import org.fergoeqs.coursework.services.AvatarService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.AppUserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Tag(name = "Users", description = "API для управления пользователями")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final AppUserMapper appUserMapper;
    private final AvatarService avatarService;

    public UserController(UserService userService, JwtService jwtService, AuthenticationService authenticationService,
                          AppUserMapper appUserMapper, AvatarService avatarService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.appUserMapper = appUserMapper;
        this.avatarService = avatarService;
    }

    @Operation(summary = "Получить всех владельцев", description = "Возвращает список всех пользователей с ролью OWNER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список владельцев успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-owners")
    public List<AppUserDTO> getAllOwners() throws BadRequestException {
        return appUserMapper.toDTOs(userService.findByRole(RoleType.ROLE_OWNER));
    }

    @Operation(summary = "Получить всех ветеринаров", description = "Возвращает список всех пользователей с ролью VET")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список ветеринаров успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class)))
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/all-vets")
    public List<AppUserDTO> getAllVets() throws BadRequestException {
        return appUserMapper.toDTOs(userService.findByRole(RoleType.ROLE_VET));
    }

    @Operation(summary = "Регистрация нового пользователя", description = "Создает нового пользователя и возвращает JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AuthenticationSucceedDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким именем или email уже существует")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationSucceedDto register(@Valid @RequestBody RegisterUserDTO user) {
        AppUser createdUser = authenticationService.signup(user);
        String jwtToken = jwtService.generateToken(createdUser);
        return new AuthenticationSucceedDto(jwtToken, jwtService.getExpirationTime());
    }


    @Operation(summary = "Аутентификация пользователя", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аутентификация успешна",
                    content = @Content(schema = @Schema(implementation = AuthenticationSucceedDto.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/login")
    public AuthenticationSucceedDto login(@Valid @RequestBody LoginUserDTO loginUserDto) {
        AppUser authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        return new AuthenticationSucceedDto(jwtToken, jwtService.getExpirationTime());
    }

    @Operation(summary = "Обновить аватар пользователя", description = "Загружает новый аватар для текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Аватар успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректный файл"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserAvatar(
            @Parameter(description = "Файл изображения аватара") @RequestParam("avatar") MultipartFile avatar) throws IOException, BadRequestException {
        AppUser user = userService.getAuthenticatedUser();
        avatarService.updateUserAvatar(user, avatar);
    }

    @Operation(summary = "Обновить информацию о пользователе", description = "Обновляет данные текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/update-user/")
    public AppUserDTO updateUser(@Valid @RequestBody AppUserDTO userDTO) throws BadRequestException {
        AppUser updatedUser = userService.updateUser(userService.getAuthenticatedUser(), userDTO);
        return appUserMapper.toDTO(updatedUser);
    }

    @Operation(summary = "Обновить пользователя (админ)", description = "Обновляет данные пользователя (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update-user-admin/{id:\\d+}")
    public AppUserDTO updateUserForAdmin(
            @Parameter(description = "ID пользователя") @PathVariable("id") Long id,
            @Valid @RequestBody AppUserDTO userDTO) throws BadRequestException {
        AppUser updatedUser = userService.updateUserForAdmin(id, userDTO);
        return appUserMapper.toDTO(updatedUser);
    }

    @Operation(summary = "Получить текущего пользователя", description = "Возвращает информацию о текущем аутентифицированном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public AppUserDTO getCurrentUser() throws BadRequestException {
        AppUser user = userService.getAuthenticatedUser();
        return appUserMapper.toDTO(user);
    }

    @Operation(summary = "Получить информацию о текущем пользователе", description = "Возвращает ID и роль текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация успешно получена",
                    content = @Content(schema = @Schema(implementation = UserInfoDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/current-user-info")
    public UserInfoDTO getCurrentUserInfo() throws BadRequestException {
        AppUser user = userService.getAuthenticatedUser();
        String role = user.getPrimaryRole()
                .map(Enum::name)
                .orElse("USER");
        return new UserInfoDTO(user.getId(), role);
    }

    @Operation(summary = "Получить DTO пользователя по ID", description = "Возвращает DTO пользователя по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/user-info/{id:\\d+}")
    public AppUserDTO getUserInfoById(@Parameter(description = "ID пользователя") @PathVariable("id") Long id) throws BadRequestException {
        AppUser user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return appUserMapper.toDTO(user);
    }

    @Operation(summary = "Обновить роли пользователя", description = "Обновляет роли пользователя (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роли успешно обновлены",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректная роль"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id:\\d+}/roles")
    public AppUserDTO updateRoles(
            @Parameter(description = "ID пользователя") @PathVariable("id") Long id,
            @RequestBody RoleType role) throws BadRequestException {
        AppUser updatedUser = userService.updateUserRoles(id, role);
        return appUserMapper.toDTO(updatedUser);
    }

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public List<AppUserDTO> getAllUsers() {
        return appUserMapper.toDTOs(userService.findAllUsers());
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает информацию о пользователе по его ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(schema = @Schema(implementation = AppUserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id:\\d+}")
    public AppUserDTO getUserById(@Parameter(description = "ID пользователя") @PathVariable("id") Long id) throws BadRequestException {
        AppUser user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        AppUser currentUser = userService.getAuthenticatedUser();
        if (!currentUser.getId().equals(id) && !currentUser.getRoles().contains(RoleType.ROLE_ADMIN)) {
            throw new ForbiddenException("Access denied");
        }
        
        return appUserMapper.toDTO(user);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Parameter(description = "ID пользователя") @PathVariable("id") Long id) throws BadRequestException {
        userService.deleteUser(id);
    }


}