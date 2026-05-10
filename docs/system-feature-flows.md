# System Feature Flows — wifi-csi-skeleton-lab

> Registro historico e incremental dos fluxos internos de cada funcionalidade.
> Este documento cresce a cada nova feature implementada e nunca tem secoes removidas.

---

## Indice

- [Visao Geral da Arquitetura](#visao-geral-da-arquitetura)
- [Convencoes deste Documento](#convencoes-deste-documento)
- [Feature: Ingestao de CsiSample via HTTP](#feature-ingestao-de-csisample-via-http)
- [Feature: Pipeline de Processamento CSI](#feature-pipeline-de-processamento-csi)
- [Feature: Deteccao de MotionState](#feature-deteccao-de-motionstate)
- [Feature: Classificacao de PostureState (ScenarioBased)](#feature-classificacao-de-posturestate-scenariobased)
- [Feature: Geracao de SkeletonFrame](#feature-geracao-de-skeletonframe)
- [Feature: Emissao de RoomStateEvent via WebSocket](#feature-emissao-de-roomstateevent-via-websocket)
- [Feature: Modo Manual de Debug](#feature-modo-manual-de-debug)
- [Feature: Calibracao de Baseline](#feature-calibracao-de-baseline)
- [Feature: Simulador CSI](#feature-simulador-csi)

---

## Visao Geral da Arquitetura

Padrao de pipeline em camadas sequenciais. Cada amostra CSI percorre o caminho abaixo antes de virar um evento WebSocket:

```
HTTP POST /api/csi/samples
    └── CsiController (api/)
            └── CsiSampleValidator (csi/validation/)
                    └── CsiPipelineService (ingestion/http/)
                            ├── SampleBuffer (processing/window/)
                            ├── MotionDetector (detection/)
                            ├── ScenarioBasedPostureClassifier (classification/)
                            ├── SkeletonService (skeleton/)
                            └── RoomStatePublisher (websocket/)
                                        └── /topic/rooms/{roomId}/events
```

| Camada          | Pacote               | Responsabilidade                                    |
|-----------------|----------------------|-----------------------------------------------------|
| `api`           | api/                 | Receber requisicoes, validar, chamar pipeline       |
| `ingestion`     | ingestion/http/      | Orquestrar o pipeline completo por amostra          |
| `processing`    | processing/window/   | Manter fila circular e produzir CsiWindow           |
| `detection`     | detection/           | Calcular features e classificar MotionState         |
| `classification`| classification/      | Classificar PostureState                            |
| `skeleton`      | skeleton/            | Gerar SkeletonFrame com keypoints por PostureState  |
| `websocket`     | websocket/           | Publicar RoomStateEvent no topico STOMP             |
| `calibration`   | calibration/         | Manter e calcular RoomBaseline por sala             |
| `debug`         | debug/               | Endpoints de inspecao sem autenticacao              |

---

## Convencoes deste Documento

- Erros de validacao retornam HTTP 400 com lista de erros em JSON
- Estado e mantido em memoria: perdido ao reiniciar o backend
- O campo `scenario` e opcional e nunca causa rejeicao quando ausente
- Todas as operacoes de leitura/escrita nos buffers sao sincronizadas

---

# Feature: Ingestao de CsiSample via HTTP

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Recebe amostras CSI de qualquer cliente (simulador ou hardware real) via HTTP POST. Valida o payload e o encaminha ao pipeline de processamento.

**Motivacao:** Ponto de entrada unico e simples para ingestao de dados CSI no MVP 0, sem dependencia de protocolo especializado (UDP, MQTT, Serial).
**Resultado:** O backend aceita amostras sinteticas do simulador e rejeita payloads invalidos com HTTP 400 descritivo.

---

## Fluxo Principal

### 1. Ponto de Entrada

- **Tipo:** HTTP REST
- **Arquivo:** `api/CsiController.java`
- **Rota:** `POST /api/csi/samples`
- **Autenticacao:** Publica (sem auth no MVP 0)

O controller deserializa o corpo JSON para o record `CsiSample`.

---

### 2. Validacao de Entrada

- **Arquivo:** `csi/validation/CsiSampleValidator.java`

| Campo        | Tipo     | Obrigatorio | Regra                                          |
|--------------|----------|-------------|------------------------------------------------|
| `timestamp`  | long     | Sim         | Presente no JSON                               |
| `deviceId`   | String   | Sim         | Nao nulo e nao vazio                           |
| `roomId`     | String   | Sim         | Nao nulo e nao vazio                           |
| `scenario`   | String   | Nao         | Pode ser null ou ausente sem causar rejeicao   |
| `rssi`       | double   | Sim         | Presente                                       |
| `amplitudes` | double[] | Sim         | Exatamente 30 elementos, sem NaN ou Infinity   |
| `phases`     | double[] | Sim         | Exatamente 30 elementos, sem NaN ou Infinity   |

**Falha de validacao:** retorna HTTP 400 com `{ "errors": ["..."] }`.

---

### 3. Orquestração

- **Arquivo:** `ingestion/http/CsiPipelineService.java`

1. Adiciona a amostra ao buffer da sala/dispositivo
2. Atualiza as ultimas amostras para debug
3. Produz CsiWindow a partir do buffer
4. Chama MotionDetector
5. Chama PostureClassifier (ou usa estado manual se ativo)
6. Chama SkeletonService
7. Monta RoomStateEvent e publica via WebSocket

---

### 4. Resposta Final

**Sucesso — 200:**

```json
{ "status": "accepted" }
```

**Falha — 400:**

```json
{ "errors": ["amplitudes must have exactly 30 elements", "phases must not contain NaN or Infinity"] }
```

---

## Fluxos Alternativos e Erros

| Cenario                          | HTTP Status | Mensagem                               |
|----------------------------------|-------------|----------------------------------------|
| amplitudes ausentes              | 400         | amplitudes is required                 |
| amplitudes.length != 30          | 400         | amplitudes must have exactly 30 elements |
| amplitudes.length != phases.length | 400       | amplitudes.length must equal phases.length |
| NaN ou Infinity em amplitudes    | 400         | amplitudes must not contain NaN or Infinity |
| scenario ausente                 | 200         | Aceito normalmente                     |

---

# Feature: Pipeline de Processamento CSI

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Agrega amostras CSI em janelas temporais de 1 segundo (~20 amostras) usando uma fila circular por roomId/deviceId.

**Motivacao:** Features de movimento so sao detectaveis ao longo de uma janela temporal — uma amostra isolada nao tem informacao suficiente.
**Resultado:** Cada amostra recebida atualiza a CsiWindow ativa e dispara o pipeline de deteccao.

---

## Fluxo Principal

### 1. SampleBuffer

- **Arquivo:** `processing/window/SampleBuffer.java`
- Fila circular de tamanho `samplesPerWindow * 2` (padrao: 40 slots)
- Ao atingir capacidade maxima, remove a amostra mais antiga
- Thread-safe via `synchronized`

### 2. Producao da CsiWindow

```
SampleBuffer.add(sample)
    -> SampleBuffer.toWindow()
        -> CsiWindow(startTimestamp, endTimestamp, deviceId, roomId, samples)
```

---

### 3. Configuracao

Parametros em `application.yml`:

```yaml
csi:
  window:
    size-seconds: 1
    samples-per-window: 20
    update-interval-ms: 500
```

---

# Feature: Deteccao de MotionState

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Calcula features estatisticas da CsiWindow e as compara ao RoomBaseline para classificar o nivel de movimento em 6 estados.

**Motivacao:** Distinguir presenca estatica de movimento leve, moderado e intenso permite que o boneco 2D reaja de forma gradual.
**Resultado:** Cada CsiWindow produz um MotionState e um SignalSummary com os valores calculados.

---

## Fluxo Principal

- **Arquivo:** `detection/MotionDetector.java`

1. Calcula `rmsEnergy` de todos os valores de amplitude da janela
2. Calcula `variance` em relacao a media RMS
3. Calcula `baselineDifference = |rmsEnergy - baseline.meanEnergy|`
4. Classifica MotionState por limiares sobre `baselineDifference` e `noiseFloor`

### Regras de classificacao

| Condicao                                          | MotionState       |
|---------------------------------------------------|-------------------|
| `rmsEnergy < baseline.meanEnergy - stdDev*2`      | NO_PRESENCE       |
| `baselineDiff < noiseFloor`                       | NO_MOTION         |
| `baselineDiff < noiseFloor + 2.0`                 | PRESENCE_DETECTED |
| `baselineDiff < 3.0`                              | LIGHT_MOTION      |
| `baselineDiff < 8.0`                              | MEDIUM_MOTION     |
| `baselineDiff >= 8.0`                             | STRONG_MOTION     |

### Valores de MotionState

| Valor              | Significado                          |
|--------------------|--------------------------------------|
| `NO_PRESENCE`      | Nenhuma presenca detectada           |
| `PRESENCE_DETECTED`| Presenca sem movimento claro         |
| `NO_MOTION`        | Presenca confirmada, parado          |
| `LIGHT_MOTION`     | Movimento leve                       |
| `MEDIUM_MOTION`    | Movimento moderado                   |
| `STRONG_MOTION`    | Movimento intenso                    |

---

# Feature: Classificacao de PostureState (ScenarioBased)

> **Versao:** 1.0.0 (MVP 0)
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Deriva PostureState a partir do campo opcional `scenario` presente nas amostras do simulador. Quando `scenario` e ausente (hardware real), retorna UNKNOWN.

**Motivacao:** Permitir a demonstracao visual do boneco 2D no MVP 0 sem exigir inferencia real do sinal.
**Resultado:** O boneco assume poses corretas (STANDING, WALKING, SITTING, LYING_DOWN) durante a simulacao.

---

## Fluxo Principal

- **Arquivo:** `classification/ScenarioBasedPostureClassifier.java`

1. Varre as amostras da CsiWindow de tras para frente procurando o primeiro `scenario` nao nulo
2. Mapeia o valor do `scenario` para PostureState
3. Retorna PostureClassification com confidence=0.9 (cenario presente) ou 0.3 (ausente -> UNKNOWN)

### Mapeamento

| scenario             | PostureState |
|----------------------|--------------|
| `person_standing`    | STANDING     |
| `person_walking`     | WALKING      |
| `person_sitting`     | SITTING      |
| `person_lying_down`  | LYING_DOWN   |
| `empty_room`         | UNKNOWN      |
| `strong_motion`      | UNKNOWN      |
| `noise_interference` | UNKNOWN      |
| null / ausente       | UNKNOWN      |
| qualquer outro valor | UNKNOWN      |

---

### Decisao Tecnica

#### ADR-001 — ScenarioBased como implementacao inicial do PostureClassifier

| Campo | Detalhe |
|-------|---------|
| **Status** | Aceita |
| **Data** | 2026-05-10 |
| **Contexto** | Inferir PostureState a partir de features do sinal requer dataset e algoritmo (MVP 1). Para demonstrar o loop visual no MVP 0, precisavamos de uma implementacao imediata. |
| **Decisao** | Usar o campo `scenario` do simulador como fonte de verdade para PostureState no MVP 0, via ScenarioBasedPostureClassifier que implementa a interface PostureClassifier. |
| **Consequencias** | O boneco responde corretamente durante a simulacao. Em hardware real, `scenario` nao existe e o classificador retorna UNKNOWN para tudo ate o MVP 1. |

---

# Feature: Geracao de SkeletonFrame

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Converte PostureClassification + MotionState em um SkeletonFrame com 13 keypoints corporais em coordenadas SVG (640x560).

**Motivacao:** O frontend precisa de coordenadas concretas para renderizar o boneco — nao apenas um estado enum.
**Resultado:** Cada RoomStateEvent contem keypoints prontos para renderizacao SVG com opacidade controlada por `confidence`.

---

## Fluxo Principal

- **Arquivo:** `skeleton/SkeletonService.java`

SkeletonService roteia para o gerador correto por PostureState:

| PostureState | Gerador                      |
|--------------|------------------------------|
| STANDING     | StandingSkeletonGenerator    |
| SITTING      | SittingSkeletonGenerator     |
| WALKING      | WalkingSkeletonGenerator (alterna pose a cada chamada) |
| LYING_DOWN   | LyingDownSkeletonGenerator   |
| ARMS_UP      | ArmsUpSkeletonGenerator      |
| CROUCHING    | SittingSkeletonGenerator (reutilizado) |
| UNKNOWN      | UnknownSkeletonGenerator (varia por MotionState) |

### Fallbacks do UnknownSkeletonGenerator

| MotionState       | Comportamento                                    |
|-------------------|--------------------------------------------------|
| NO_PRESENCE       | Pose neutra com confidence=0.1 (quase invisivel) |
| STRONG_MOTION     | Pose neutra com confidence=0.4                   |
| demais            | Pose neutra com confidence=0.3                   |

### Keypoints produzidos (coordenadas SVG 640x560)

`head, neck, leftShoulder, rightShoulder, leftElbow, rightElbow, leftHand, rightHand, hip, leftKnee, rightKnee, leftFoot, rightFoot`

---

# Feature: Emissao de RoomStateEvent via WebSocket

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Publica RoomStateEvent no topico STOMP `/topic/rooms/{roomId}/events` apos cada amostra processada.

**Resultado:** O frontend React recebe atualizacoes em tempo real sem polling.

---

## Fluxo Principal

- **Arquivo:** `websocket/RoomStatePublisher.java`
- Usa `SimpMessagingTemplate.convertAndSend`
- Topico: `/topic/rooms/{roomId}/events`
- Conexao do cliente: `ws://localhost:8080/ws` com SockJS + STOMP

### Payload do evento

```json
{
  "timestamp": 1710000000000,
  "roomId": "bedroom",
  "motionState": "MEDIUM_MOTION",
  "postureState": "WALKING",
  "confidence": 0.9,
  "signal": { "rmsEnergy": 18.4, "variance": 3.2, "baselineDifference": 7.9 },
  "skeleton": {
    "mode": "STATE_BASED",
    "keypoints": [ { "name": "head", "x": 320, "y": 90, "confidence": 0.9 }, "..." ]
  }
}
```

---

# Feature: Modo Manual de Debug

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Permite ao usuario sobrescrever temporariamente o PostureState e MotionState via API, com expiracao automatica de 10 segundos.

**Motivacao:** Facilitar testes visuais no frontend sem precisar mudar o cenario do simulador.

---

## Fluxo Principal

- **Endpoint:** `POST /api/simulation/state`
- **Arquivo:** `api/SimulationController.java`

1. Recebe `ManualState { roomId, postureState, motionState }`
2. Armazena em `CsiPipelineService.manualState` com `expiresAt = now + 10s`
3. Em cada chamada ao `resolvePosture()`, verifica se modo manual esta ativo e usa PostureState manual
4. Apos 10 segundos, o modo expira automaticamente na proxima amostra recebida

**Desativar manualmente:** `DELETE /api/simulation/state`

---

# Feature: Calibracao de Baseline

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Permite capturar um baseline do ambiente vazio para calibrar o detector de movimento. Sem calibracao, usa valores padrao sinteticos.

**Resultado:** O MotionDetector funciona imediatamente com o baseline padrao; calibracao melhora a precisao.

---

## Fluxo Principal

- **Arquivo:** `calibration/CalibrationService.java`

1. `POST /api/rooms/{roomId}/calibration/start` -> abre buffer de calibracao
2. Durante a calibracao, todas as amostras recebidas sao adicionadas ao buffer
3. `POST /api/rooms/{roomId}/calibration/finish` -> calcula `meanEnergy`, `stdDev`, `noiseFloor` e substitui o baseline da sala

**Baseline padrao (sem calibracao):**

| Campo             | Valor |
|-------------------|-------|
| `meanEnergy`      | 10.0  |
| `standardDeviation`| 0.3  |
| `noiseFloor`      | 0.5   |

---

# Feature: Simulador CSI

> **Versao:** 1.0.0
> **Implementada em:** 2026-05-10
> **Status:** Concluida

---

## Resumo

Servico autonomo que gera amostras CSI sinteticas e as envia ao backend via HTTP a 20 Hz, simulando todos os cenarios fisicos esperados.

**Motivacao:** Validar o loop visual completo sem hardware real.

---

## Fluxo Principal

- **Arquivo:** `simulator/CsiSimulator.java`
- Schedulado a `1000 / frequencyHz` ms (padrao: 50ms = 20 Hz)
- Chama `ScenarioGenerator.generate(scenario)` para obter amplitudes e fases sinteticas
- Envia via `RestClient` para `POST {BACKEND_URL}/api/csi/samples`
- Inclui o campo `scenario` no payload para o ScenarioBasedPostureClassifier

### Caracteristicas sinteticas por cenario

| Cenario             | RMS esperado | Variancia | Periodicidade |
|---------------------|--------------|-----------|---------------|
| `empty_room`        | ~10.0        | < 0.5     | Nenhuma       |
| `person_standing`   | ~11.0        | 0.5-1.5   | Nenhuma       |
| `person_walking`    | 12.0-16.0    | 2.0-5.0   | ~0.8s         |
| `person_sitting`    | ~10.5        | < 1.0     | Nenhuma       |
| `person_lying_down` | ~10.2        | < 0.4     | Nenhuma       |
| `strong_motion`     | 20.0-30.0    | > 8.0     | Nenhuma       |
| `noise_interference`| variavel     | alta      | Nenhuma       |

> Valores sinteticos — nao refletem medicoes reais de hardware CSI.
