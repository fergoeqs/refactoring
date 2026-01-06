# Политика безопасности и проверки прав доступа

## Обзор

Данный документ описывает политику проверки прав доступа в системе VetCare, определяя, какие проверки выполняются на уровне `HttpSecurity.requestMatchers`, а какие через `@PreAuthorize`.

## Принципы разделения проверок

### HttpSecurity.requestMatchers

Используется для **групповых проверок на уровне URL-путей**:
- Проверка аутентификации (authenticated/permitAll)
- Проверка ролей для целых групп эндпоинтов
- Публичные эндпоинты (Swagger, регистрация, логин)

**Примеры:**
```java
.requestMatchers("/api/users/register", "/api/users/login").permitAll()
.requestMatchers("/api/sectors/**").hasAnyRole("ADMIN", "VET")
.anyRequest().authenticated()
```

### @PreAuthorize

Используется для **детальных проверок на уровне методов**:
- Проверка конкретных ролей для отдельных операций
- Бизнес-логика, требующая проверки нескольких условий
- Операции, доступные нескольким ролям с разными правами

**Примеры:**
```java
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET') or hasRole('ROLE_OWNER')")
```

### Проверка прав на уровне ресурсов (SecurityUtils)

Используется для **проверки доступа к конкретным ресурсам по ID**:
- Проверка, является ли пользователь владельцем ресурса
- Проверка, привязан ли ветеринар к ресурсу
- Проверка прав на изменение/просмотр конкретного объекта

**Примеры:**
```java
SecurityUtils.checkPetAccess(currentUser, pet, false);
SecurityUtils.checkUserAccess(currentUser, targetUserId, false);
SecurityUtils.checkAppointmentAccess(currentUser, appointment, true);
```

## Текущая конфигурация SecurityConfig

### Публичные эндпоинты (permitAll):
- `/api/ws/**` - WebSocket соединения
- `/api/users/register`, `/api/users/login` - регистрация и логин
- `/api/rating-and-reviews/**` - рейтинги и отзывы (публичные)
- `/swagger-ui/**`, `/v3/api-docs/**` - Swagger документация

### Эндпоинты с проверкой ролей на уровне URL:
- `/api/sectors/**` - только для ADMIN и VET

### Все остальные эндпоинты:
- Требуют аутентификации (`.anyRequest().authenticated()`)
- Дополнительные проверки выполняются через `@PreAuthorize` и `SecurityUtils`

## Критичные методы с явной авторизацией

### SlotsController:
- `book-slot/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET') or hasRole('ROLE_OWNER')")`
- `release-slot/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `add-slot` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `delete-slot/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`

### DiagnosticAttachmentController:
- `save` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `getAttachment/{id}` - проверка через SecurityUtils (доступ через анамнез → питомец)
- `getAttachmentUrl/{id}` - проверка через SecurityUtils

### AnamnesisController:
- `save` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `getAnamnesis/{id}` - проверка через SecurityUtils (доступ через питомца)
- `getAllByPatient/{petId}` - проверка через SecurityUtils

### AppointmentsController:
- `cancel-appointment/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `update-appointment/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET') or hasRole('ROLE_OWNER')")`
- `getAppointment/{id}` - проверка через SecurityUtils

### UserController:
- `update-user-admin/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- `update-roles/{id}` - `@PreAuthorize("hasRole('ROLE_ADMIN')")`
- `getUserById/{id}` - проверка через SecurityUtils (пользователь может видеть только свой профиль)

### PetsController:
- `getPet/{petId}` - проверка через SecurityUtils
- `updatePet/{petId}` - проверка через SecurityUtils
- `bind/{petId}` - `@PreAuthorize("hasRole('ROLE_VET')")`
- `sector-place/{petId}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `unbind/{petId}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`
- `delete-pet/{petId}` - `@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")`

### HealthUpdateController:
- `getHealthUpdate/{id}` - проверка через SecurityUtils (доступ через питомца)
- `getAllHealthUpdates/{petId}` - проверка через SecurityUtils

## Правила доступа к ресурсам по ID

### Питомцы (Pet):
- **Владелец (OWNER)**: доступ только к своим питомцам
- **Ветеринар (VET)**: доступ к питомцам, к которым он привязан
- **Администратор (ADMIN)**: полный доступ ко всем питомцам

### Пользователи (User):
- **Пользователь**: доступ только к своему профилю
- **Администратор (ADMIN)**: полный доступ ко всем пользователям

### Записи на прием (Appointment):
- **Владелец (OWNER)**: доступ к записям своих питомцев
- **Ветеринар (VET)**: доступ к записям, где он назначен ветеринаром
- **Администратор (ADMIN)**: полный доступ ко всем записям

### Анамнезы, обновления здоровья, диагностические вложения:
- Доступ определяется через связанного питомца
- Используется `SecurityUtils.checkResourceAccessThroughPet()`

## Аутентификация

### Упрощенная модель:
- Используется только **JWT аутентификация**
- **DaoAuthenticationProvider** (стандартный Spring Security) + **BCrypt** для проверки паролей
- **UserDetailsServiceImpl** для загрузки пользователя
- Удален `CustomAuthenticationProvider` (небезопасный, избыточный)
- Удален `PasswordUtil` с SHA-384 (используется только BCrypt)

### Процесс аутентификации:
1. Пользователь отправляет credentials на `/api/users/login`
2. `AuthenticationService.authenticate()` использует `AuthenticationManager`
3. `DaoAuthenticationProvider` проверяет пароль через `PasswordEncoder` (BCrypt)
4. При успехе генерируется JWT токен
5. Токен используется для последующих запросов через `JwtAuthenticationFilter`

## Секреты и конфигурация

### Переменные окружения (.env):
Все секреты вынесены в переменные окружения:
- `JWT_SECRET_KEY` - секретный ключ для JWT
- `JWT_EXPIRATION` - время жизни токена
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` - настройки БД
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` - настройки почты
- `MINIO_URL`, `MINIO_ACCESS_NAME`, `MINIO_ACCESS_SECRET`, `MINIO_BUCKET_NAME` - настройки MinIO



