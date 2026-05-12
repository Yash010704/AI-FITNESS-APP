# 🏋️ FitnessApp — Microservices Fitness Tracking Platform

A full-stack fitness tracking application built with a **Spring Boot microservices** backend and a **React** frontend. Users can log workouts, track activity history, and receive **AI-powered fitness recommendations** via Google Gemini.

---

## 📐 Architecture Overview

```
                        ┌────────────────┐
                        │   React UI     │
                        │  (Vite + MUI)  │
                        └───────┬────────┘
                                │ HTTP (port 5173)
                        ┌───────▼────────┐
                        │  API Gateway   │  ← Keycloak JWT auth + CORS
                        │  (port 8080)   │
                        └───┬───────┬────┘
                 ┌──────────┘       └──────────┐
        ┌────────▼────────┐          ┌─────────▼────────┐
        │ Activity Service│          │   User Service    │
        │  (port dynamic) │          │  (port dynamic)   │
        └────────┬────────┘          └──────────────────┘
                 │ Kafka
        ┌────────▼────────┐
        │   AI Service    │  ← Google Gemini API
        │  (port dynamic) │
        └─────────────────┘

   ┌──────────────────┐   ┌──────────────────┐
   │  Eureka Server   │   │  Config Server   │
   │  (port 8761)     │   │  (port 8888)     │
   └──────────────────┘   └──────────────────┘

   Databases: MongoDB (activities, recommendations) · Keycloak (identity)
   Messaging: Apache Kafka
```

---

## 🧩 Services

| Service | Description | Tech |
|---|---|---|
| **Gateway** | Single entry point — JWT auth, CORS, user sync | Spring Cloud Gateway, Keycloak OAuth2 |
| **Activity Service** | Track and query workouts | Spring Boot, MongoDB, Kafka Producer |
| **AI Service** | AI-generated fitness recommendations | Spring Boot, Kafka Consumer, Google Gemini |
| **Config Server** | Centralised configuration for all services | Spring Cloud Config |
| **Eureka Server** | Service discovery & registry | Spring Cloud Netflix Eureka |
| **Fitness UI** | Frontend SPA | React 19, Vite, MUI, Redux Toolkit |

---

## ✨ Features

- **Log workouts** — running, walking, cycling, swimming with duration, calories, and custom metrics
- **Activity history** — view all past activities per user
- **AI recommendations** — each logged activity triggers a Kafka event; the AI Service consumes it and calls Google Gemini to generate personalised fitness advice
- **Secure auth** — Keycloak-backed OAuth2 / OIDC with JWT tokens validated at the gateway
- **Service discovery** — all microservices register with Eureka; no hardcoded URLs between services
- **Centralised config** — all service configs managed in one place via Spring Cloud Config Server

---

## 🛠️ Tech Stack

**Backend**
- Java 21, Spring Boot 3.4.5
- Spring Cloud 2024.0.1 (Config, Gateway, Eureka)
- Spring Security (OAuth2 Resource Server / JWT)
- Apache Kafka (event streaming)
- MongoDB (document storage)
- Lombok, WebFlux (WebClient)

**Frontend**
- React 19, Vite 8
- Material UI (MUI) 9
- Redux Toolkit + React-Redux
- Axios, React Router 7
- `react-oauth2-code-pkce` (PKCE auth flow)

**Infrastructure**
- Keycloak (identity provider)
- Google Gemini API (AI recommendations)

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+, npm
- Docker & Docker Compose (recommended for Kafka, MongoDB, Keycloak)
- A Google Gemini API key

### 1. Start Infrastructure

Spin up MongoDB, Kafka, and Keycloak via Docker Compose (add your own `docker-compose.yml`):

```bash
docker-compose up -d
```

### 2. Start Backend Services (in order)

```bash
# 1. Config Server
cd configserver && ./mvnw spring-boot:run

# 2. Eureka Server
cd eureka && ./mvnw spring-boot:run

# 3. API Gateway
cd gateway && ./mvnw spring-boot:run

# 4. Activity Service
cd activity && ./mvnw spring-boot:run

# 5. AI Service
cd aiservice && ./mvnw spring-boot:run
```

### 3. Start the Frontend

```bash
cd fitnessappui/fitness-project
npm install
npm run dev
```

The UI runs at `http://localhost:5173`.

---

## ⚙️ Configuration

Each service fetches its configuration from the **Config Server** at startup. Override environment-specific values in `configserver/src/main/resources/config/`.

Key properties to set:

| Property | Where | Description |
|---|---|---|
| `gemini.api.key` | `ai-service.yml` | Your Google Gemini API key |
| `gemini.api.url` | `ai-service.yml` | Gemini endpoint URL |
| `spring.data.mongodb.uri` | per service config | MongoDB connection string |
| `spring.kafka.bootstrap-servers` | per service config | Kafka broker address |
| Keycloak realm/client settings | `gateway-service.yml` | OAuth2 resource server JWT config |

---

## 📡 API Reference

All requests go through the **API Gateway** (`http://localhost:8080`). A valid Bearer token (obtained via Keycloak) is required on every request.

### Activity Service — `/api/activities`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/activities` | Log a new activity |
| `GET` | `/api/activities` | Get all activities for the authenticated user |
| `GET` | `/api/activities/{id}` | Get a specific activity by ID |

**Example — Log Activity:**
```json
POST /api/activities
Authorization: Bearer <token>

{
  "type": "RUNNING",
  "duration": 30,
  "caloriesBurned": 300,
  "startTime": "2025-05-10T07:00:00",
  "additionalMetrics": {
    "distance_km": 5,
    "avg_pace_min_per_km": 6
  }
}
```

**Activity Types:** `RUNNING`, `WALKING`, `CYCLING`, `SWIMMING`

### AI Service — `/api/recommendations`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/recommendations/{activityId}` | Get AI recommendation for a specific activity |

> Recommendations are generated automatically when an activity is logged. The Activity Service publishes a Kafka event, and the AI Service consumes it and stores the recommendation.

---

## 📂 Project Structure

```
fitnessappp/
├── activity/          # Activity tracking microservice
├── aiservice/         # AI recommendation microservice
├── configserver/      # Spring Cloud Config Server
├── eureka/            # Service discovery (Eureka)
├── gateway/           # API Gateway + security
└── fitnessappui/
    └── fitness-project/   # React frontend
        ├── src/
        │   ├── components/    # ActivityForm, ActivityList, ActivityDetail
        │   ├── services/      # Axios API client
        │   ├── store/         # Redux store + auth slice
        │   └── App.jsx
        └── package.json
```

---

## 🔐 Authentication Flow

1. User logs in via the React app using PKCE OAuth2 flow (Keycloak)
2. Keycloak issues a JWT access token
3. The gateway validates the JWT on every request
4. A `KeycloakUserSyncFilter` extracts the user ID and injects it as the `X-USER-ID` header for downstream services
5. Downstream services use `X-USER-ID` — no direct Keycloak dependency needed

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open source. Feel free to use and modify it.
