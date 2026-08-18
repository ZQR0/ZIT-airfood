# airfood — backend

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-migrations-2962FF)](https://www.liquibase.org/)
[![License](https://img.shields.io/badge/license-Academic-lightgrey)](#лицензия)

> **airfood** (компенсация/уход за формой) — сервис, который помогает авиакомпаниям
> соблюдать **ФАП-82** при задержках рейсов: начисляет пассажиру деньги на
> посадочный талон, и пассажир может их потратить в кафе/точках обслуживания
> аэропорта.

---

## Содержание

1. [Что это такое](#что-это-такое)
2. [Ключевые возможности](#ключевые-возможности)
3. [Архитектура](#архитектура)
4. [Стек](#стек)
5. [Структура репозитория](#структура-репозитория)
6. [Быстрый старт](#быстрый-старт)
7. [Тестовые аккаунты](#тестовые-аккаунты)
8. [Эндпоинты API](#эндпоинты-api)
9. [Swagger / OpenAPI](#swagger--openapi)
10. [Мониторинг](#мониторинг)
11. [Сборка и тесты](#сборка-и-тесты)
12. [Структура базы данных](#структура-базы-данных)
13. [Бизнес-инварианты](#бизнес-инварианты)
14. [Troubleshooting](#troubleshooting)
15. [Документация в проекте](#документация-в-проекте)
16. [Лицензия](#лицензия)

---

## Что это такое

Когда рейс задерживается, авиакомпания обязана компенсировать пассажиру расходы
на еду и напитки. Вместо бумажных ваучеров и наличных airfood даёт простой цифровой
конвейер:

1. **Сотрудник авиакомпании** в личном кабинете фиксирует задержку и нажимает
   «начислить» — деньги попадают на **все посадочные талоны рейса** мгновенно.
2. **Пассажир** приходит в кафе/ресторан аэропорта, показывает QR с номером
   талона.
3. **Кассир кафе** сканирует номер — деньги списываются с баланса талона в
   пользу точки обслуживания.

Все расчёты — в одной базе, история операций хранится для отчётности.

**Для кого:**

- **Авиакомпании** — личный кабинет с рейсами, талонами и кнопкой
  «начислить/откатить компенсацию».
- **Кафе/точки обслуживания** — терминал на кассе с двумя действиями:
  «проверить баланс» и «списать».
- **Пассажиры** — клиент, который не регистрируется в системе, но получает
  начисления и тратит их в кафе.

---

## Ключевые возможности

- 👤 **Два типа пользователей** — авиакомпании (с логином/паролем и личным
  кабинетом) и кафе (без аутентификации, для терминалов самообслуживания).
- 🔐 **Сессионная аутентификация** для авиакомпаний через cookie `JSESSIONID` +
  BCrypt-хеши паролей.
- 💸 **Начисление компенсации** на все талоны рейса одной операцией
  (`POST /flights/{id}/payment/processTopUp`).
- 🧾 **Списание в кафе** по публичному номеру талона (`POST /points/pay`).
- ↩️ **Откат начислений** как на целый рейс, так и на отдельный талон
  (`DELETE /flights/{id}/payment/restore` и
  `DELETE /ticket/{ticketNumber}/payment/restore`).
- 📑 **Swagger/OpenAPI** — автогенерируемая документация.
- 📈 **Prometheus + Grafana** — метрики JVM, HTTP, БД.
- ✅ **Интеграционные тесты** на JPA-репозиториях через Testcontainers.

---

## Архитектура

```
                   ┌────────────────────┐
                   │  Airline personal  │
                   │    cabinet (UI)    │
                   └────────┬───────────┘
                            │  /api/v1/airlines/*
                            │  (требует JSESSIONID)
                            ▼
   ┌────────────────────────────────────────────────┐
   │          Spring Boot 4.1.0 backend             │
   │  ┌──────────┐  ┌──────────┐  ┌──────────────┐  │
   │  │Security  │  │ Service  │  │   Liquibase  │  │
   │  │filter    │→ │ layer    │→ │   migrations │  │
   │  └──────────┘  └──────────┘  └──────┬───────┘  │
   │                                     │          │
   │  ┌──────────┐  ┌──────────┐         │          │
   │  │Spring    │  │ Spring   │         │          │
   │  │Data JPA  │←─┤ Data     │         │          │
   │  └────┬─────┘  └──────────┘         │          │
   └───────┼──────────────────────────────┘
           │  JDBC
           ▼
   ┌────────────────────┐
   │   PostgreSQL 18    │
   │ (airfood_db, тома) │
   └─────────▲──────────┘
             │
             │  /api/v1/points/*
             │  (открыто, без аутентификации)
   ┌─────────┴────────┐
   │   Cafe POS /     │
   │   кассовый       │
   │   терминал       │
   └──────────────────┘
```

Дополнительно в `docker-compose.yaml` поднимаются:

- **Prometheus** — собирает метрики с backend (`/actuator/prometheus`).
- **Grafana** — визуализация, поставляется с дефолтным datasource Prometheus.

---

## Стек

| Слой        | Технология                                          |
|-------------|-----------------------------------------------------|
| Язык        | Java 21 (toolchain)                                 |
| Фреймворк   | Spring Boot 4.1.0                                   |
| Web         | `spring-boot-starter-webmvc`                        |
| Persistence | Spring Data JPA + Hibernate                         |
| Миграции БД | Liquibase (YAML master + SQL children)              |
| БД          | PostgreSQL 18 (Alpine)                              |
| Безопасность| Spring Security (form-less, сессионная)             |
| Документация| springdoc-openapi + Swagger UI                      |
| Метрики     | Micrometer + `micrometer-registry-prometheus`       |
| Утилиты     | Lombok                                              |
| Сборка      | Gradle (wrapper)                                    |
| Тесты       | JUnit 5 + AssertJ + Testcontainers + Spring Test    |

---

## Структура репозитория

```
backend/
├── build.gradle                 # Зависимости и таски Gradle
├── settings.gradle
├── gradlew, gradlew.bat
├── docker-compose.yaml          # Postgres + Prometheus + Grafana
├── HELP.md                      # Дефолтный гайд от Spring Initializr
├── docs/
│   ├── API.md                   # Описание эндпоинтов
│   ├── AUTHTEST.md              # Сценарии ручной проверки auth
│   └── FIXES.md                 # Известные критические проблемы
└── src/
    ├── main/
    │   ├── java/zit/airfood/backend/
    │   │   ├── BackendApplication.java
    │   │   ├── controller/      # AirlinesController, ServicePointsController
    │   │   ├── service/         # Бизнес-логика
    │   │   ├── dto/             # Запросы/ответы REST
    │   │   ├── dao/
    │   │   │   ├── entity/      # JPA-сущности
    │   │   │   └── repository/  # Spring Data репозитории
    │   │   └── security/        # Конфигурация Spring Security, AuthController
    │   └── resources/
    │       ├── application.yaml
    │       └── db/changelog/
    │           ├── changelog.master.yaml        # Liquibase master
    │           ├── migrations/ddl-01.sql        # Схема
    │           └── release/                     # Сид-данные dml-01..07.sql
    └── test/
        ├── java/zit/airfood/backend/
        │   ├── BackendApplicationTests.java
        │   └── dao/repository/  # Интеграционные тесты на JPA
        └── resources/
```

---

## Быстрый старт

### Предусловия

- **Docker** + **Docker Compose** v2 (`docker compose`).
- **JDK 21** (`java -version` → `21.x`).
- **curl** для проверки.
- Опционально: **psql** или любой GUI-клиент к Postgres.

### 1. Поднять инфраструктуру (Postgres + Prometheus + Grafana)

```bash
docker compose up -d
```

Поднимутся:

| Сервис      | Хост:порт            | Назначение                                |
|-------------|----------------------|-------------------------------------------|
| `postgres`  | `localhost:5432`     | База `airfood_db`,user/password `postgres`|
| `prometheus`| `localhost:9090`     | Сбор метрик с backend                     |
| `grafana`   | `localhost:3000`     | Дашборды (admin/admin)                    |

> Имена контейнеров: `zit-postgres`, `zit-prometheus`, `zit-grafana`. Они
> соединяются через bridge-сеть `airfood-network`. Том `postgres_data`
> персистентен между перезапусками.

### 2. Запустить backend

```bash
./gradlew bootRun
```

При старте Liquibase в одном проходе накатит:

- `migrations/ddl-01.sql` — схема (таблицы `airlines`, `airports`, `flight`,
  `passenger`, `ticket`, `service_points`, `transaction`).
- `release/dml-01.sql` … `dml-07.sql` — сид-данные: 10 авиакомпаний, 14
  аэропортов, 20 рейсов, 20 пассажиров, 24 талона, 21 точка обслуживания, 28
  транзакций.

### 3. Проверить, что всё поднялось

```bash
# Health (должен быть 200 + status=UP)
curl -i localhost:8080/actuator/health

# Метрики Prometheus (первые 20 строк)
curl -s localhost:8080/actuator/prometheus | head -20
```

Если в ответе `{"status":"UP"}` — можно работать.

### 4. Залогиниться как авиакомпания

```bash
curl -i -c jar.txt -X POST localhost:8080/api/v1/airlines/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"aeroflot","password":"password"}'
```

В ответе — 200, `Set-Cookie: JSESSIONID=...` и JSON
`{"id":1,"name":"Аэрофлот","login":"aeroflot","authenticated":true}`.

Дальше все запросы к `/api/v1/airlines/*` (кроме `login`/`logout`) требуют
cookie из `jar.txt`:

```bash
curl -i -b jar.txt localhost:8080/api/v1/airlines/flights
```

### 5. Проверить баланс талона в кафе (без аутентификации)

```bash
curl -i 'localhost:8080/api/v1/points/checkBalance?ticketNumber=TKT202607140001'
# {"result":500.00} — стартовый баланс талона 1
```

### Остановить всё

```bash
# backend: Ctrl+C в терминале
# инфраструктура:
docker compose down         # остановить
docker compose down -v      # остановить и удалить тома (полный сброс БД)
```

---

## Тестовые аккаунты

Все 10 авиакомпаний заведены в `airlines` сид-данными `dml-01.sql` с **одинаковым
BCrypt-хешем пароля `password`**
(`$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`).

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

Пароль везде: `password`.

Подробные сценарии ручной проверки auth — в [`docs/AUTHTEST.md`](docs/AUTHTEST.md).

---

## Эндпоинты API

Базовый путь: **`/api/v1`**. Полная документация — в [`docs/API.md`](docs/API.md).

### Сотрудник авиакомпании (`/airlines/*`, требуется `JSESSIONID`)

| Метод | Endpoint                                          | Описание                                          |
|-------|---------------------------------------------------|---------------------------------------------------|
| GET   | `/airlines/flights`                               | Список рейсов                                     |
| GET   | `/airlines/tickets`                               | Список талонов                                    |
| GET   | `/airlines/flights/{id}`                          | Получить рейс по id                               |
| GET   | `/airlines/tickets/{ticketNumber}`                | Получить талон по публичному номеру               |
| GET   | `/airlines/flights/{id}/boardingPasses`           | Все талоны на рейс                                |
| GET   | `/airlines/reports`                               | Сводные показатели                                |
| POST  | `/airlines/flights/{id}/payment/processTopUp`     | Начислить сумму на все талоны рейса               |
| PUT   | `/airlines/validatePoint?pointId={id}`            | Одобрить заявку кафе на подключение               |
| DELETE| `/airlines/flights/{id}/payment/restore`          | Откатить все начисления на рейс                   |
| DELETE| `/airlines/ticket/{ticketNumber}/payment/restore` | Откатить начисление на талон                      |
| POST  | `/airlines/login`                                 | Вход (открыт)                                     |
| POST  | `/airlines/logout`                                | Выход (открыт)                                    |

### Кафе/точка обслуживания (`/points/*`, без аутентификации)

| Метод | Endpoint                                       | Описание                              |
|-------|------------------------------------------------|---------------------------------------|
| GET   | `/points/checkBalance?ticketNumber={number}`   | Узнать баланс по номеру талона        |
| POST  | `/points/pay`                                  | Списать сумму с баланса талона        |

Пример запроса в кафе:

```bash
curl -i -X POST localhost:8080/api/v1/points/pay \
  -H 'Content-Type: application/json' \
  -d '{"ticketNumber":"TKT202607140001","amount":150.00,"servicePointId":1}'
```

Ответ:

```json
{
  "success": true,
  "message": "Оплата прошла успешно",
  "ticketNumber": "TKT202607140001",
  "amount": 150.00,
  "servicePoint": 1,
  "newBalance": 350.00
}
```

---

## Swagger / OpenAPI

После старта приложения:

- **Swagger UI:** <http://localhost:8080/swagger-ui/index.html>
- **OpenAPI JSON:** <http://localhost:8080/v3/api-docs>

В UI доступны все эндпоинты с описанием (`@Operation`, `@ApiResponse`),
можно выполнять запросы прямо из браузера. Для airline-эндпоинтов Swagger
попросит `JSESSIONID` cookie — её можно получить через форму
`/api/v1/airlines/login` или вставить в Authorize.

> Если UI не открывается — проверьте, что в `build.gradle` подключён
> `org.springdoc:springdoc-openapi-starter-webmvc-ui` и совместим с текущей
> версией Spring Boot (см. также [FIXES.md п. 9](docs/FIXES.md)).

---

## Мониторинг

| Сервис        | URL                          | Назначение                                  |
|---------------|------------------------------|---------------------------------------------|
| Prometheus    | <http://localhost:9090>      | Сбор метрик с бэкенда                       |
| Grafana       | <http://localhost:3000>      | Дашборды (admin/admin)                       |
| Actuator UI   | <http://localhost:8080/actuator>         | Health, info, metrics, prometheus |
| Health        | <http://localhost:8080/actuator/health>   | `{"status":"UP"}` + статус БД               |
| Prometheus endpoint | <http://localhost:8080/actuator/prometheus> | Метрики для Prometheus-скрейпинга    |

Метрики JVM, HTTP-запросов, HikariCP (пул БД), Hibernate — всё
автоматически экспортируется Micrometer'ом в формате Prometheus.

Grafana при первом входе попросит сменить пароль. Datasource Prometheus
предустановлен (`http://prometheus:9090` внутри `airfood-network`). Импортируйте
готовые дашборды (например, JVM (Micrometer) — id `4701`) через
`+ → Import → 4701`.

---

## Сборка и тесты

```bash
# Полная сборка + тесты + jar
./gradlew build

# Только тесты
./gradlew test

# Запуск в dev-режиме (с автоперезапуском через DevTools)
./gradlew bootRun

# Только компиляция (этот шаг используется в CI CodeQL)
./gradlew compileJava

# JAR после сборки лежит в:
ls build/libs/backend-0.0.1.jar
java -jar build/libs/backend-0.0.1.jar
```

### Тесты

Интеграционные тесты на JPA-репозиториях написаны на JUnit 5 + AssertJ и
используют **Testcontainers** для поднятия PostgreSQL 18 в Docker:

- `src/test/java/zit/airfood/backend/dao/repository/AbstractRepositoryTest.java` —
  базовый класс (`@DataJpaTest` + `PostgreSQLContainer`).
- Тесты на каждый репозиторий: `AirlinesRepositoryTest`, `AirportsRepositoryTest`,
  `FlightRepositoryTest`, `PassengerRepositoryTest`, `TicketRepositoryTest`,
  `ServicePointRepositoryTest`, `TransactionRepositoryTest`.

Тесты не требуют, чтобы Postgres из `docker-compose` был поднят — каждая
`@SpringBootTest` сама поднимает контейнер.

---

## Структура базы данных

Семь таблиц. Упрощённая ER-диаграмма:

```
              ┌──────────┐
              │ airlines │  (id, name, login, password_hash)
              └─────┬────┘
                    │ 1
                    │
                    ▼ *
              ┌──────────┐  N ┌─────────────┐
              │  flight  │◄───┤  airports   │ (id, name, unique_code, town, address)
              └─────┬────┘  M └─────────────┘
                    │ 1
                    │
                    ▼ *
              ┌──────────┐  N ┌─────────────┐
              │  ticket  │◄───┤  passenger  │ (id, имя, паспорт)
              └─────┬────┘    └─────────────┘
                    │ 1
                    │
                    ▼ *
              ┌──────────────┐  N ┌──────────────┐
              │ transaction  │◄───┤ service_pts  │ (id, name, airport_id, phone, is_active)
              └──────────────┘  1 └──────────────┘
                                       │ N
                                       ▼ 1
                                 ┌─────────────┐
                                 │  airports   │
                                 └─────────────┘
```

**Особенности:**

- `ticket.ticket_number` — **публичный** 8-байтовый hex (16 символов), генерируется
  через `gen_random_bytes(8)`. Это **не** внутренний `id`. Именно его сканируют
  в кафе.
- `flight.delay_minutes` (INT, ≥ 0) — длительность задержки. `delay_minutes > 0`
  означает «рейс задержан» (флаг вычисляется из длительности).
- `transaction.type` — `topUp` / `purchase` / `reversal`.

---

## Бизнес-инварианты

1. **Баланс — кэш, истина — в `transaction`.**
   `ticket.balance` денормализован для скорости; реальная история хранится в
   таблице `transaction`. Чтобы не было расхождений, обновление `balance` и
   вставка `transaction` всегда происходят **в одной DB-транзакции** на уровне
   приложения.
2. **`CHECK (balance >= 0)`** — последняя линия защиты на уровне БД. Даже если
   приложение ошибётся в расчёте, база не даст уйти в минус.
3. **Атомарность компенсации.** Один вызов
   `POST /flights/{id}/payment/processTopUp` начисляет **одинаковую** сумму на
   все талоны рейса; либо всё, либо ничего (если БД-транзакция откатилась).
4. **Reversal ≠ purchase.** Откат начисления создаёт операцию типа
   `reversal` (положительная сумма, знак — в типе), а не отрицательный
   `purchase`. Это сохраняет корректность отчётов.
5. **Паспортные данные** — серия и номер паспорта разделены и валидируются
   регулярками (`^[0-9]{4}$` / `^[0-9]{6}$`). Уникальность — композитная
   `(series, number)`.
6. **IATA-код аэропорта** — 3 латинские буквы, проверяется на уровне БД
   (`CHECK (unique_code ~ '^[A-Z]{3}$')`).

---

## Troubleshooting

### «`Schema-validation: missing table [airlines]`»

PostgreSQL поднят, но Liquibase не отработал. Проверьте:

- В `application.yaml:23` путь: `classpath:db/changelog/changelog.master.yaml`.
- В `changelog.master.yaml` подключены `migrations/ddl-01.sql` и
  `release/dml-*.sql`.

### «401 на любой запрос в `/api/v1/airlines/*`»

Сессия не активна. Залогиньтесь заново и сохраните cookie:

```bash
curl -c jar.txt -X POST localhost:8080/api/v1/airlines/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"aeroflot","password":"password"}'

# затем во всех остальных запросах:
curl -b jar.txt ...
```

### «`psql: connection to server failed: Connection refused` на `localhost:5432`»

Контейнер `zit-postgres` не поднялся. Проверьте:

```bash
docker ps -a
docker logs zit-postgres
docker compose up -d postgres    # перезапустить только Postgres
```

### «`port 8080 is already in use`»

Поменяйте порт в `src/main/resources/application.yaml`:

```yaml
server:
  port: 8090
```

### «Все запросы в `/api/v1/airlines/*` возвращают 403 вместо 401»

В `WebSecurityConfig` не подключён `RestAuthenticationEntryPoint`. Проверьте,
что в `apiFilterChain` есть:

```java
.exceptionHandling(eh -> eh.authenticationEntryPoint(entryPoint))
```

### «Данные в БД исчезли после `docker compose down`»

`postgres_data` — это **именованный том**, он переживает `down`/`up`. Если
выполнили `docker compose down -v` — данные удалены. Поднимите заново и
перезапустите backend — Liquibase пересоздаст схему и накатит сид.

### «Grafana не показывает данные»

1. Откройте <http://localhost:3000> (admin/admin, попросит сменить пароль).
2. `Connections → Data sources → Prometheus` — проверьте URL
   `http://prometheus:9090` (внутри docker-сети).
3. `Explore → Prometheus` — выполните `up{}` или
   `http_server_requests_seconds_count` — если есть метрики, datasource
   работает.

---

## Документация в проекте

| Файл                                       | Содержание                                              |
|--------------------------------------------|---------------------------------------------------------|
| [`docs/API.md`](docs/API.md)               | Полный список REST-эндпоинтов, базовый путь `/api/v1`    |
| [`docs/AUTHTEST.md`](docs/AUTHTEST.md)     | Сценарии ручной проверки аутентификации (`curl`)        |
| [`docs/FIXES.md`](docs/FIXES.md)           | Известные критические проблемы и план их устранения     |
| [`HELP.md`](HELP.md)                       | Дефолтный гайд от Spring Initializr (общие ссылки)       |

---

## Лицензия

Учебный проект МИИТ, 2026. Используйте свободно в образовательных целях.
