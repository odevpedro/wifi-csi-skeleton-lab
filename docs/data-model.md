# Data Model — wifi-csi-skeleton-lab

> Documento vivo do modelo de dados. Atualizado sempre que uma entidade for criada, alterada ou removida.
> **Ultima atualizacao:** 2026-05-10

---

## Indice

- [Visao Geral](#visao-geral)
- [Diagrama de Registros](#diagrama-de-registros)
- [Records e Entidades](#records-e-entidades)
- [Enums e Dominio de Valores](#enums-e-dominio-de-valores)
- [Classificacao de Privacidade](#classificacao-de-privacidade)
- [Decisoes de Modelagem](#decisoes-de-modelagem)

---

## Visao Geral

O modelo de dados do MVP 0 e composto inteiramente por Java records imutaveis mantidos em memoria — sem banco de dados. O nucleo do dominio e formado por `CsiSample` (dado bruto), `CsiWindow` (janela temporal), `RoomBaseline` (referencia de ambiente) e `RoomStateEvent` (evento publicado via WebSocket).

**Banco de dados:** Nenhum no MVP 0 (estado em memoria)
**Persistencia:** ConcurrentHashMap e SampleBuffer (fila circular) em `CsiPipelineService` e `CalibrationService`
**Persistencia futura planejada:** PostgreSQL + TimescaleDB para historico de eventos

---

## Diagrama de Registros

```mermaid
flowchart TD
    A[CsiSample - entrada via HTTP POST] --> B[SampleBuffer\nfila circular em memoria]
    B --> C[CsiWindow\n~20 amostras / 1s]
    C --> D[MotionDetector]
    C --> E[PostureClassifier]
    D --> F[DetectionResult\nMotionState + SignalSummary]
    E --> G[PostureClassification\nPostureState + confidence]
    G --> H[SkeletonService]
    H --> I[SkeletonFrame\nList&lt;BodyKeypoint&gt;]
    F --> J[RoomStateEvent]
    I --> J
    J --> K[WebSocket\n/topic/rooms/{roomId}/events]
```

---

## Records e Entidades

---

### CsiSample

> Amostra bruta de Channel State Information recebida via POST /api/csi/samples.

**Localização:** `csi/model/CsiSample.java`
**Persistencia:** Nao — mantida temporariamente no SampleBuffer em memoria

| Campo        | Tipo Java | Obrigatorio | Descricao                                                              |
|--------------|-----------|-------------|------------------------------------------------------------------------|
| `timestamp`  | long      | Sim         | Timestamp Unix em ms do momento da captura                             |
| `deviceId`   | String    | Sim         | Identificador do dispositivo emissor (ex: `esp32-sim-01`)              |
| `roomId`     | String    | Sim         | Identificador da sala (ex: `bedroom`)                                  |
| `scenario`   | String    | Nao         | Cenario simulado — presente apenas no simulador, null em hardware real |
| `rssi`       | double    | Sim         | Received Signal Strength Indicator em dBm                              |
| `amplitudes` | double[]  | Sim         | Amplitudes das 30 subportadoras (MVP 0: exatamente 30 elementos)       |
| `phases`     | double[]  | Sim         | Fases das 30 subportadoras (MVP 0: exatamente 30 elementos)            |

**Restricoes de validacao:**
- `amplitudes.length == 30` e `phases.length == 30` (MVP 0)
- `amplitudes.length == phases.length`
- Nenhum elemento pode ser NaN ou Infinity
- `scenario` nunca causa rejeicao quando ausente ou null

---

### CsiWindow

> Janela temporal de amostras CSI de um mesmo deviceId/roomId para calculo de features.

**Localizacao:** `csi/model/CsiWindow.java`
**Persistencia:** Nao — gerada sob demanda a partir do SampleBuffer

| Campo            | Tipo Java        | Descricao                                     |
|------------------|------------------|-----------------------------------------------|
| `startTimestamp` | long             | Timestamp da amostra mais antiga na janela    |
| `endTimestamp`   | long             | Timestamp da amostra mais recente             |
| `deviceId`       | String           | Identificador do dispositivo                  |
| `roomId`         | String           | Identificador da sala                         |
| `samples`        | List<CsiSample>  | Lista de amostras da janela (~20 no MVP 0)    |

---

### RoomBaseline

> Referencia do estado do ambiente vazio usada pelo MotionDetector para detectar desvios.

**Localizacao:** `csi/model/RoomBaseline.java`
**Persistencia:** Nao — mantido em ConcurrentHashMap em CalibrationService

| Campo               | Tipo Java | Descricao                                                      |
|---------------------|-----------|----------------------------------------------------------------|
| `roomId`            | String    | Identificador da sala                                          |
| `deviceId`          | String    | Identificador do dispositivo que gerou o baseline              |
| `meanEnergy`        | double    | Energia RMS media do ambiente calibrado (padrao: 10.0)         |
| `standardDeviation` | double    | Desvio padrao da energia durante calibracao (padrao: 0.3)      |
| `noiseFloor`        | double    | Limiar de ruido base (padrao: 0.5)                             |
| `calibratedAt`      | long      | Timestamp Unix em ms da ultima calibracao                      |

---

### DetectionResult

> Resultado intermediario do MotionDetector — nao publicado diretamente, compoe o RoomStateEvent.

**Localizacao:** `detection/DetectionResult.java`

| Campo          | Tipo Java     | Descricao                              |
|----------------|---------------|----------------------------------------|
| `motionState`  | MotionState   | Estado de movimento classificado       |
| `signalSummary`| SignalSummary | Features calculadas da CsiWindow       |

---

### SignalSummary

> Features estatisticas da CsiWindow calculadas pelo MotionDetector.

**Localizacao:** `detection/SignalSummary.java`

| Campo                | Tipo Java | Descricao                                              |
|----------------------|-----------|--------------------------------------------------------|
| `rmsEnergy`          | double    | Energia RMS de todas as amplitudes da janela           |
| `variance`           | double    | Variancia em relacao a media RMS                       |
| `baselineDifference` | double    | Diferenca absoluta entre rmsEnergy e baseline.meanEnergy |

---

### PostureClassification

> Resultado do PostureClassifier — nao publicado diretamente, compoe o RoomStateEvent.

**Localizacao:** `classification/PostureClassification.java`

| Campo         | Tipo Java    | Descricao                                               |
|---------------|--------------|---------------------------------------------------------|
| `postureState`| PostureState | Estado de postura classificado                          |
| `confidence`  | double       | Confianca da classificacao (0.0 a 1.0)                 |

---

### BodyKeypoint

> Coordenada de uma articulacao do esqueleto no espaco SVG (640x560).

**Localizacao:** `skeleton/BodyKeypoint.java`

| Campo        | Tipo Java | Descricao                                                  |
|--------------|-----------|------------------------------------------------------------|
| `name`       | String    | Nome da articulacao (ex: `head`, `leftKnee`)              |
| `x`          | double    | Coordenada horizontal no viewBox SVG (0-640)               |
| `y`          | double    | Coordenada vertical no viewBox SVG (0-560)                 |
| `confidence` | double    | Confianca do keypoint (0.0 a 1.0) — controla opacidade SVG |

**Keypoints produzidos:** `head, neck, leftShoulder, rightShoulder, leftElbow, rightElbow, leftHand, rightHand, hip, leftKnee, rightKnee, leftFoot, rightFoot`

---

### SkeletonFrame

> Frame completo do esqueleto com todos os keypoints, publicado dentro do RoomStateEvent.

**Localizacao:** `skeleton/SkeletonFrame.java`

| Campo       | Tipo Java          | Descricao                                     |
|-------------|--------------------|-----------------------------------------------|
| `timestamp` | long               | Timestamp Unix em ms da geracao do frame      |
| `roomId`    | String             | Identificador da sala                         |
| `mode`      | SkeletonMode       | Modo de geracao do esqueleto                  |
| `keypoints` | List<BodyKeypoint> | Lista de 13 articulacoes do corpo             |

---

### RoomStateEvent

> Evento principal publicado via WebSocket apos cada amostra processada. E o contrato entre backend e frontend.

**Localizacao:** `websocket/RoomStateEvent.java`
**Canal WebSocket:** `/topic/rooms/{roomId}/events`

| Campo          | Tipo Java       | Descricao                                      |
|----------------|-----------------|------------------------------------------------|
| `timestamp`    | long            | Timestamp Unix em ms                           |
| `roomId`       | String          | Identificador da sala                          |
| `motionState`  | MotionState     | Estado de movimento detectado                  |
| `postureState` | PostureState    | Estado de postura classificado                 |
| `confidence`   | double          | Confianca global da classificacao (0.0 a 1.0) |
| `signal`       | SignalSummary   | Features do sinal da janela atual              |
| `skeleton`     | SkeletonFrame   | Frame do esqueleto com keypoints               |

---

### ManualState

> Payload do modo manual de debug. Substitui temporariamente o PostureState e MotionState por 10 segundos.

**Localizacao:** `ingestion/http/ManualState.java`
**Endpoint:** `POST /api/simulation/state`

| Campo          | Tipo Java    | Descricao                        |
|----------------|--------------|----------------------------------|
| `roomId`       | String       | Sala a ser sobrescrita           |
| `postureState` | PostureState | PostureState a ser forcado       |
| `motionState`  | MotionState  | MotionState a ser forcado        |

---

## Enums e Dominio de Valores

---

### MotionState

**Localizacao:** `detection/MotionState.java`

| Valor               | Significado                            |
|---------------------|----------------------------------------|
| `NO_PRESENCE`       | Nenhuma presenca detectada             |
| `PRESENCE_DETECTED` | Presenca sem movimento claro           |
| `NO_MOTION`         | Presenca confirmada, sem movimento     |
| `LIGHT_MOTION`      | Movimento leve                         |
| `MEDIUM_MOTION`     | Movimento moderado                     |
| `STRONG_MOTION`     | Movimento intenso                      |

---

### PostureState

**Localizacao:** `classification/PostureState.java`

| Valor        | Significado                                              |
|--------------|----------------------------------------------------------|
| `UNKNOWN`    | Nao foi possivel classificar (default sem cenario real)  |
| `STANDING`   | Pessoa em pe                                             |
| `SITTING`    | Pessoa sentada                                           |
| `LYING_DOWN` | Pessoa deitada                                           |
| `WALKING`    | Pessoa andando                                           |
| `ARMS_UP`    | Bracos levantados                                        |
| `CROUCHING`  | Pessoa agachada                                          |

---

### SkeletonMode

**Localizacao:** `skeleton/SkeletonMode.java`

| Valor          | Significado                                                |
|----------------|------------------------------------------------------------|
| `STATE_BASED`  | Keypoints definidos por tabela fixa para cada PostureState |
| `ML_KEYPOINTS` | Keypoints inferidos por modelo de IA (MVP 3 futuro)        |
| `SIMULATED`    | Keypoints gerados por simulacao (nao usado no MVP 0)       |

---

## Classificacao de Privacidade

> O MVP 0 nao coleta dados pessoais identificaveis. Os dados CSI sao anonimos por natureza.

| Campo             | Record         | Classificacao    | Justificativa                                          |
|-------------------|----------------|------------------|--------------------------------------------------------|
| `amplitudes`      | CsiSample      | Publico derivado | Dados de sinal sem identificacao de pessoa             |
| `phases`          | CsiSample      | Publico derivado | Dados de sinal sem identificacao de pessoa             |
| `rssi`            | CsiSample      | Publico derivado | Intensidade do sinal, sem identificacao                |
| `deviceId`        | CsiSample      | Interno          | Identificador de dispositivo, nao de pessoa            |
| `roomId`          | CsiSample      | Interno          | Identificador de ambiente, nao de pessoa               |
| `motionState`     | RoomStateEvent | Sensivel         | Detecta presenca e movimento de pessoas no ambiente    |
| `postureState`    | RoomStateEvent | Sensivel         | Infere postura corporal — exige consentimento do usuario |
| `keypoints`       | SkeletonFrame  | Sensivel         | Representacao corporal da pessoa presente              |

**Regra geral:** Este sistema deve ser usado apenas com consentimento explicito das pessoas presentes no ambiente monitorado.

---

## Decisoes de Modelagem

### ADR-DM-001 — Java records imutaveis como modelo de dados

| Campo | Detalhe |
|-------|---------|
| **Status** | Aceita |
| **Data** | 2026-05-10 |
| **Contexto** | O MVP 0 nao usa banco de dados. Os dados trafegam pelo pipeline em memoria sem necessidade de mutacao. |
| **Decisao** | Usar Java records para todos os modelos de dados, garantindo imutabilidade e serializacao JSON automatica pelo Spring. |
| **Alternativas consideradas** | Classes mutaveis com getters/setters (descartado — desnecessario e mais verboso); JPA entities (descartado — sem banco no MVP 0). |
| **Consequencias** | Codigo conciso e seguro para threads. Em MVPs futuros com banco, os records serao substituidos ou complementados por JPA entities. |

---

### ADR-DM-002 — Estado em memoria sem banco de dados

| Campo | Detalhe |
|-------|---------|
| **Status** | Aceita |
| **Data** | 2026-05-10 |
| **Contexto** | O objetivo do MVP 0 e validar o loop visual completo com o minimo de infraestrutura. |
| **Decisao** | Manter todo o estado (buffers, baseline, ultimo evento) em ConcurrentHashMap e filas em memoria. |
| **Alternativas consideradas** | PostgreSQL + TimescaleDB (planejado para MVP 2+). |
| **Consequencias** | Estado perdido ao reiniciar o backend. Suficiente para demonstracao, invalido para producao. |

---

### ADR-DM-003 — Campo scenario como String simples sem anotacao Nullable

| Campo | Detalhe |
|-------|---------|
| **Status** | Aceita |
| **Data** | 2026-05-10 |
| **Contexto** | O campo `scenario` e opcional no CsiSample. Era necessario decidir como representar a opcionalidade sem adicionar dependencias externas (Lombok, JetBrains Nullable etc.). |
| **Decisao** | Declarar `scenario` como `String` simples. A opcionalidade e garantida pela regra de validacao (ausencia nao causa rejeicao) e documentada em comentario no codigo. |
| **Consequencias** | Sem dependencias extras. O compilador nao avisa sobre null — desenvolvedores devem usar null-checks explicitamente ao ler o campo. |
