# wifi-csi-skeleton-lab

> Laboratorio experimental de Wi-Fi sensing que usa CSI (Channel State Information) para detectar presenca, movimento e estimar postura humana 2D em formato de boneco/esqueleto — sem camera, sem hardware dedicado no MVP 0.

---

## Sobre o Projeto

O projeto explora como variacoes no sinal Wi-Fi causadas pela presenca e movimento de pessoas podem ser extraidas do CSI para classificar estados corporais. O backend Java processa janelas temporais de amostras CSI, detecta o nivel de movimento (MotionState) e estima a postura (PostureState), transmitindo o resultado em tempo real para um dashboard React que renderiza um boneco 2D em SVG.

No MVP 0, um simulador envia dados sinteticos que reproduzem os padroes fisicos esperados de cada cenario (caminhada, sentado, deitado etc.), permitindo validar o loop visual completo sem hardware real.

> Aviso: No MVP 0, o PostureState e derivado do campo `scenario` enviado pelo simulador — nao e inferencia real de postura por Wi-Fi. O MotionState e calculado a partir de features do sinal sintetico (energia RMS, variancia, diferenca de baseline).

---

## Stack & Arquitetura

| Camada       | Tecnologia                                      |
|--------------|-------------------------------------------------|
| Backend      | Java 21 + Spring Boot 3.2                       |
| WebSocket    | Spring WebSocket + STOMP                        |
| Frontend     | React 18 + TypeScript + Vite 5                  |
| Graficos     | Recharts                                        |
| Simulador    | Java 21 + Spring Boot 3.2 (servico autonomo)    |
| Infra        | Docker + Docker Compose                         |
| Testes       | JUnit 5 + Maven Surefire                        |
| Persistencia | Estado em memoria (sem banco no MVP 0)          |

Padrao arquitetural do backend: pipeline em camadas `ingestion -> processing -> detection -> classification -> skeleton -> websocket`.

---

## Estrutura de Pastas

```
wifi-csi-skeleton-lab/
├── backend/
│   ├── src/main/java/com/example/wificsiskeleton/
│   │   ├── api/              # Controllers REST
│   │   ├── calibration/      # Servico de baseline
│   │   ├── classification/   # PostureClassifier e implementacoes
│   │   ├── config/           # CORS e WebSocket
│   │   ├── csi/model/        # Records CsiSample, CsiWindow, RoomBaseline
│   │   ├── csi/validation/   # CsiSampleValidator
│   │   ├── debug/            # Endpoints e StatusHolder
│   │   ├── detection/        # MotionDetector, MotionState, SignalSummary
│   │   ├── ingestion/http/   # CsiPipelineService, ManualState
│   │   ├── processing/window/# SampleBuffer (fila circular)
│   │   ├── skeleton/         # Geradores de SkeletonFrame por PostureState
│   │   └── websocket/        # RoomStateEvent, RoomStatePublisher
│   └── src/test/             # 43 testes unitarios
├── frontend/
│   └── src/
│       ├── components/       # SkeletonViewer, SignalChart, ManualControl, EventLog
│       ├── hooks/            # useWebSocket
│       └── types/            # Tipos TypeScript dos contratos
├── simulator/
│   └── src/main/java/com/example/simulator/
│       ├── CsiSimulator.java      # Scheduler que envia amostras
│       └── ScenarioGenerator.java # Geracao sintetica por cenario
├── docs/
│   ├── system-feature-flows.md
│   └── data-model.md
├── docker-compose.yml
├── backlog.md
└── README.md
```

---

## Como Rodar Localmente

### Pre-requisitos

- Docker 24+
- Docker Compose v2

### Setup

```bash
# Clone o repositorio
git clone <url> && cd wifi-csi-skeleton-lab

# Suba todos os servicos
docker compose up --build
```

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec (JSON): `http://localhost:8080/v3/api-docs`

### Variaveis de ambiente do simulador (docker-compose.yml)

| Variavel       | Padrao           | Descricao                     |
|----------------|------------------|-------------------------------|
| `BACKEND_URL`  | `http://backend:8080` | URL do backend            |
| `ROOM_ID`      | `bedroom`        | ID da sala simulada           |
| `DEVICE_ID`    | `esp32-sim-01`   | ID do dispositivo simulado    |
| `SCENARIO`     | `person_walking` | Cenario ativo                 |
| `FREQUENCY_HZ` | `20`             | Frequencia de envio (Hz)      |

---

## Testes

```bash
# Requer Java 21 JDK e Maven 3.9+
cd backend
./mvnw test
```

43 testes unitarios cobrindo: validacao de payload, detector de movimento, classificador de postura, gerador de skeleton, buffer de janela temporal.

---

## API — Endpoints Principais

| Metodo | Rota                                    | Descricao                        | Auth |
|--------|-----------------------------------------|----------------------------------|------|
| GET    | `/api/health`                           | Status do backend                | Nao  |
| POST   | `/api/csi/samples`                      | Ingerir amostra CSI              | Nao  |
| GET    | `/api/rooms/{roomId}/state`             | Estado atual da sala             | Nao  |
| POST   | `/api/rooms/{roomId}/calibration/start` | Iniciar calibracao               | Nao  |
| POST   | `/api/rooms/{roomId}/calibration/finish`| Finalizar calibracao             | Nao  |
| GET    | `/api/rooms/{roomId}/baseline`          | Ver baseline atual               | Nao  |
| POST   | `/api/simulation/state`                 | Ativar modo manual (10s)         | Nao  |
| DELETE | `/api/simulation/state`                 | Desativar modo manual            | Nao  |
| GET    | `/api/debug/latest-samples`             | Ultimas amostras CSI por sala    | Nao  |
| GET    | `/api/debug/latest-event`               | Ultimo RoomStateEvent emitido    | Nao  |
| GET    | `/api/debug/simulator-status`           | Cenario ativo e stats            | Nao  |

WebSocket: `ws://localhost:8080/ws` — topico `/topic/rooms/{roomId}/events`

Documentacao interativa (Swagger UI): `http://localhost:8080/swagger-ui.html`

---

## Cenarios Simulados

| Cenario             | MotionState esperado | PostureState esperado |
|---------------------|----------------------|-----------------------|
| `empty_room`        | NO_PRESENCE / NO_MOTION | UNKNOWN            |
| `person_standing`   | PRESENCE_DETECTED / LIGHT_MOTION | STANDING  |
| `person_walking`    | MEDIUM_MOTION        | WALKING               |
| `person_sitting`    | LIGHT_MOTION         | SITTING               |
| `person_lying_down` | NO_MOTION            | LYING_DOWN            |
| `strong_motion`     | STRONG_MOTION        | UNKNOWN               |
| `noise_interference`| variavel             | UNKNOWN               |

Para trocar o cenario, edite `SCENARIO` no `docker-compose.yml` e reinicie o simulador.

---

## Documentacao Tecnica

| Documento                                                    | Descricao                              |
|--------------------------------------------------------------|----------------------------------------|
| [Fluxos de Funcionalidades](./docs/system-feature-flows.md) | Fluxo interno de cada feature do MVP 0 |
| [Modelo de Dados](./docs/data-model.md)                     | Records, enums e contratos de dados    |
| [Backlog](./backlog.md)                                      | Status de desenvolvimento do projeto   |

---

## Status do Projeto

```
[x] MVP 0 — loop visual completo: simulador -> backend -> WebSocket -> frontend -> boneco 2D
[ ] MVP 1 — RuleBasedPostureClassifier inferindo PostureState a partir de features do sinal
[ ] MVP 2 — CSI real (ESP32) + calibracao robusta
[ ] MVP 3 — dataset proprio + IA (PyTorch/ONNX) -> keypoints 2D aproximados
[ ] MVP 4 — multiplos receptores + modelo treinado -> pose 2D mais fiel
```

---

## Limitacoes do MVP 0

- Sem hardware real (ESP32 ou similar)
- Sem banco de dados (estado em memoria, perdido ao reiniciar)
- PostureState derivado do campo `scenario` do simulador, nao inferido do sinal
- Boneco 2D baseado em estado, nao em reconstrucao corporal real
- Calibracao e opcional (baseline padrao: meanEnergy=10.0, stdDev=0.3, noiseFloor=0.5)
- Ingestao apenas via HTTP (UDP/MQTT/Serial sao fases futuras)
- Sem autenticacao

---

## Consideracoes de Privacidade

Este projeto deve ser usado apenas em ambientes proprios ou com consentimento explicito das pessoas presentes. Wi-Fi sensing detecta presenca e movimento sem camera, o que exige responsabilidade etica. Nao usar para vigilancia nao autorizada.

---

## Licenca

Distribuido sob a licenca MIT.

---

<p align="center">
  Feito com foco em qualidade por <a href="https://github.com/odevpedro">@odevpedro</a>
</p>
