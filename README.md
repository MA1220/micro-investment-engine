# Micro-Investment Engine

Production-grade financial micro-investment engine. Java 17, Spring Boot. Timeline sweep, q/p rules, k-period aggregation, NPS & Index returns.

---

## Prerequisites

- **Java 17** (OpenJDK or Eclipse Temurin)
- **Maven 3.6+** (to build the project)
- **Docker** (optional, for containerized run)

---

## Setup & Build

1. **Clone the repository**
   ```bash
   git clone https://github.com/MA1220/micro-investment-engine.git
   cd micro-investment-engine
   ```

2. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```
   Or run tests:
   ```bash
   mvn clean package
   ```
   The runnable JAR is produced at: `target/micro-investment-engine-1.0.0.jar`

---

## Run

### Option 1: Run with Java

```bash
java -jar target/micro-investment-engine-1.0.0.jar
```

The application starts on **port 5477**.

### Option 2: Run with Docker

Build the image (from the project root, after building the JAR):

```bash
mvn clean package -DskipTests
docker build -t micro-investment-engine .
docker run -p 5477:5477 micro-investment-engine
```

---

## API

- **Endpoint:** `POST http://localhost:5477/api/v1/engine/process`
- **Content-Type:** `application/json`
- **Timestamp format:** `yyyy-MM-dd HH:mm:ss` (e.g. `2024-01-15 09:30:00`)

### Request body

| Field           | Type     | Description                                      |
|----------------|----------|--------------------------------------------------|
| transactions   | array    | List of `{ id, timestamp, amount }`              |
| qRules         | array    | Optional. Override rules: `{ id, startTime, ceilingOverride }` |
| pRules         | array    | Optional. Additive rules: `{ id, startTime, delta }` |
| kPeriods       | array    | Periods: `{ id, startInclusive, endExclusive }`   |
| defaultCeiling | number   | Default contribution ceiling                     |
| inflationRate  | number   | Inflation rate (e.g. 0.02)                       |
| taxCap         | number   | Tax benefit cap for NPS                          |

### Example request

See `sample-request.json`. Quick test with curl:

```bash
curl -X POST http://localhost:5477/api/v1/engine/process \
  -H "Content-Type: application/json" \
  -d @sample-request.json
```

### Response

JSON with:

- `periodSummaries` — per-period contribution, remanent, p additive
- `npsReturns` — NPS returns (tax cap applied, inflation-adjusted)
- `indexReturns` — Index returns (no tax benefit, inflation-adjusted)

---

## Tech Stack

- Java 17  
- Spring Boot 3.2  
- Maven  

---

## License

See repository license.
