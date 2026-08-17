# Тестирование аутентификации авиакомпаний

Документ описывает, как вручную проверить работу login-пайплайна для личного кабинета авиакомпании: вход, доступ к защищённым эндпоинтам, отказ в доступе без сессии, выход и негативные сценарии.

## Что проверяем

1. `POST /api/v1/airlines/login` принимает логин/пароль и открывает сессию (cookie `JSESSIONID`).
2. Все эндпоинты `/api/v1/airlines/**` (кроме `login` и `logout`) без cookie возвращают `401`.
3. Те же эндпоинты с валидной cookie возвращают ожидаемые коды.
4. `POST /api/v1/airlines/logout` инвалидирует сессию, дальнейшие запросы — снова `401`.
5. `POST /api/v1/airlines/login` с неверным паролем возвращает `401` и JSON-ошибку.
6. `GET /actuator/health` остаётся открытым (Prometheus-цепочка не задета).
7. `GET /api/v1/points/checkBalance` остаётся открытым (service-points вне scope авторизации).

## Предусловия

- Запущена БД из `docker-compose.yaml`:

  ```bash
  docker compose up -d postgres
  ```

- В `airlines` уже есть 10 авиакомпаний из `dml-01.sql`. У всех одинаковый BCrypt-хеш пароля `"password"` (`$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`).

  | login       | авиакомпания        |
  |-------------|---------------------|
  | `aeroflot`  | Аэрофлот            |
  | `pobeda`    | Победа              |
  | `s7`        | S7 Airlines         |
  | `ural`      | Уральские авиалинии |
  | `rossiya`   | Россия              |
  | `nordwind`  | Nordwind Airlines   |
  | `azimuth`   | Азимут              |
  | `redwings`  | Red Wings           |
  | `utair`     | UTair               |
  | `smartavia` | Smartavia           |

- Бэкенд собран и запущен на `:8080`:

  ```bash
  ./gradlew bootRun
  ```

- Для тестов используется `curl` и cookie-jar `jar.txt`, чтобы cookie `JSESSIONID` сохранялась между запросами.

## Сценарии

Все команды ниже предполагают, что сервер слушает `localhost:8080`. Коды ответов указаны для happy-path; в негативных сценариях — ожидаемый код ошибки.

### 1. Успешный вход

```bash
curl -i -c jar.txt -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"aeroflot","password":"password"}'
```

Ожидаемо:

- HTTP `200 OK`.
- `Set-Cookie: JSESSIONID=...` в заголовках ответа.
- Тело ответа:

  ```json
  {"id":1,"name":"Аэрофлот","login":"aeroflot","authenticated":true}
  ```

Cookie сохранится в `jar.txt` и будет автоматически подставляться во все следующие `curl -b jar.txt ...`.

### 2. Доступ к защищённому эндпоинту после логина

```bash
curl -i -b jar.txt localhost:8080/api/v1/airlines/flights
```

Ожидаемо: HTTP `200` (тело пока `null` — контроллер ещё не реализован, это нормально; важно, что запрос прошёл аутентификацию). Аналогично проходят любые другие маршруты `AirlinesController`:

- `GET /api/v1/airlines/tickets`
- `GET /api/v1/airlines/flights/1`
- `GET /api/v1/airlines/tickets/<номер>`
- `GET /api/v1/airlines/flights/1/boardingPasses`
- `GET /api/v1/airlines/reports`
- `POST /api/v1/airlines/flights/1/payment/process`
- `PUT /api/v1/airlines/validatePoint?pointId=1`
- `DELETE /api/v1/airlines/flights/1/payment/restore`
- `DELETE /api/v1/airlines/ticket/<номер>/payment/restore`

### 3. Защита: запрос без cookie

```bash
curl -i localhost:8080/api/v1/airlines/flights
```

Ожидаемо: HTTP `401`, тело:

```json
{"error":"Authentication required"}
```

То же для любого другого маршрута `/api/v1/airlines/**`, кроме `POST /login` и `POST /logout`.

### 4. Неверный пароль

```bash
curl -i -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"aeroflot","password":"WRONG"}'
```

Ожидаемо: HTTP `401`, тело:

```json
{"error":"Invalid login or password"}
```

Cookie `JSESSIONID` в ответе отсутствует (или меняется на новую пустую сессию — оба варианта приемлемы).

### 5. Несуществующий логин

```bash
curl -i -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"nope","password":"password"}'
```

Ожидаемо: HTTP `401`, тело как в п. 4. Поведение одинаковое — мы намеренно не различаем «логин не найден» и «пароль не совпал», чтобы не подсказывать, какие логины существуют.

### 6. Пустое тело запроса на вход

```bash
curl -i -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"","password":""}'
```

Ожидаемо: HTTP `400` (Bean Validation на `@NotBlank` в `LoginRequest` отрабатывает до контроллера). Точное тело зависит от настроек `MethodArgumentNotValidException` — допустимо, если в теле есть `errors` или `message`.

### 7. Выход

```bash
curl -i -b jar.txt -c jar.txt -X POST localhost:8080/api/v1/airlines/logout
```

Ожидаемо: HTTP `204 No Content`, в ответе есть `Set-Cookie: JSESSIONID=...; Max-Age=0` (cookie удаляется), и `jar.txt` обновляется.

Сразу после:

```bash
curl -i -b jar.txt localhost:8080/api/v1/airlines/flights
```

Ожидаемо: HTTP `401` и `{"error":"Authentication required"}` — сессия инвалидирована, тот же файл `jar.txt` уже не пускает.

### 8. Выход без активной сессии

```bash
curl -i -X POST localhost:8080/api/v1/airlines/logout
```

Ожидаемо: HTTP `204` (эндпоинт `permitAll`, повторный логаут — no-op). В проде это обычно 401, но для симметрии с обычным UI мы оставляем `204` — пользователь, у которого сессия уже истекла, не должен видеть ошибку при нажатии «Выйти».

### 9. Prometheus-цепочка не задета

```bash
curl -i localhost:8080/actuator/health
```

Ожидаемо: HTTP `200`, тело со статусом БД. Это проверяет, что первая `SecurityFilterChain` (`/actuator/**`) осталась как была.

### 10. Service-points без авторизации

```bash
curl -i 'localhost:8080/api/v1/points/checkBalance?ticketNumber=any'
```

Ожидаемо: HTTP `200` (контроллер возвращает `null`, тело пустое). Эти эндпоинты предназначены для терминалов кафе и сознательно оставлены открытыми — в схеме БД у них нет пользователей.

## Проверка с разными ролями / аккаунтами

В текущей модели роль одна — `ROLE_AIRLINE`, и привязки логина к конкретной авиакомпании нет на уровне Security-контекста. `UserDetailsService` грузит запись по `login`, а `AuthController` отдельно подтягивает `AirlinesEntity` для ответа.

Чтобы проверить, что разные аккаунты дают разные сессии:

```bash
# сначала aeroflot
curl -s -c aeroflot.txt -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"aeroflot","password":"password"}'

# потом s7, не используя первый jar
curl -s -c s7.txt -X POST localhost:8080/api/v1/airlines/login \
    -H 'Content-Type: application/json' \
    -d '{"login":"s7","password":"password"}'
```

Оба ответа — `200`, `id` и `name` в теле соответствуют логину.

## Что ломается при ошибках в реализации

| Симптом                                                                  | Вероятная причина                                                                                     |
|--------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `curl /api/v1/airlines/login` без тела возвращает `415`                  | Забыли `Content-Type: application/json` (это нормально, не баг — фиксируется в клиенте).             |
| `curl /api/v1/airlines/login` валидный, но `Set-Cookie` нет              | `SecurityContextRepository.saveContext(...)` не вызван в `AuthController` — сессия не сохраняется.   |
| `/actuator/health` стал `401`                                            | Сломали `securityMatcher("/actuator/**")` в Prometheus-цепочке.                                       |
| `/api/v1/airlines/**` отдаёт `403` вместо `401`                          | Не подключён `RestAuthenticationEntryPoint` в `apiFilterChain`.                                      |
| Все запросы идут в Prometheus-цепочку (allowAll)                        | В `apiFilterChain` нет `securityMatcher("/api/**")`, или порядок `@Order` сломан.                    |
| Логин проходит, но `AuthController` падает с `LazyInitializationException` | Обращение к `airline.getFlights()`/`getId()` в DTO вне транзакции. В текущем коде используются скаляры, так что проблемы быть не должно. |

## Связанные файлы

- Конфигурация: `src/main/java/zit/airfood/backend/security/WebSecurityConfig.java`
- `UserDetailsService`: `src/main/java/zit/airfood/backend/security/AirlineUserDetailsService.java`
- `AuthenticationEntryPoint`: `src/main/java/zit/airfood/backend/security/RestAuthenticationEntryPoint.java`
- Контроллер входа: `src/main/java/zit/airfood/backend/security/AuthController.java`
- DTO: `src/main/java/zit/airfood/backend/dto/LoginRequest.java`, `src/main/java/zit/airfood/backend/dto/LoginResponse.java`
- Репозиторий: `src/main/java/zit/airfood/backend/dao/repository/AirlinesRepository.java`
- Сущность: `src/main/java/zit/airfood/backend/dao/entity/AirlinesEntity.java`
- Сид-данные: `src/main/resources/db/changelog/realese/dml-01.sql`
