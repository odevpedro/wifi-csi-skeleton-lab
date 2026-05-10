# Backlog — wifi-csi-skeleton-lab

> Registro vivo do progresso do projeto. Atualizado a cada mudança de estado de uma funcionalidade.
> **Ultima atualizacao:** 2026-05-10

---

## Sobre o Projeto

Laboratorio experimental de Wi-Fi sensing usando CSI para detectar presenca, movimento e estimar postura humana 2D sem camera.

**Versao atual:** `0.1.0-SNAPSHOT` (MVP 0)
**Stack principal:** Java 21 + Spring Boot 3.2 + React 18 + TypeScript + Docker Compose

---

## Legenda

| Simbolo | Significado         |
|---------|---------------------|
| `[ ]`   | Pendente            |
| `[~]`   | Em andamento        |
| `[x]`   | Concluido           |
| `P0`    | Critico             |
| `P1`    | Alta prioridade     |
| `P2`    | Media prioridade    |
| `P3`    | Melhoria / nice-to-have |
| `XS` `S` `M` `L` `XL` | Estimativa de complexidade |

---

## Em Andamento

> Nenhum item em andamento no momento.

---

## Pendentes

### MVP 1 — Classificacao por features do sinal

| Status | Prioridade | Complexidade | Feature |
|--------|------------|--------------|---------|
| `[ ]`  | P1         | L            | RuleBasedPostureClassifier inferindo PostureState a partir de energia, variancia e periodicidade da CsiWindow |
| `[ ]`  | P1         | M            | Testes unitarios para RuleBasedPostureClassifier com janelas sinteticas de cada postura |
| `[ ]`  | P2         | S            | Interface plugavel PostureClassifier selecionada via configuracao (spring profile ou yml) |

### MVP 2 — Hardware real

| Status | Prioridade | Complexidade | Feature |
|--------|------------|--------------|---------|
| `[ ]`  | P1         | XL           | Ingestao de CSI real via ESP32 (HTTP inicial, depois UDP/MQTT) |
| `[ ]`  | P1         | L            | Calibracao robusta: multiplas rodadas, descarte de outliers, persistencia em arquivo |
| `[ ]`  | P2         | M            | Parametros de subportadoras configurados por dispositivo (30 / 52 / 56 / 64) |
| `[ ]`  | P2         | S            | Endpoint para listar dispositivos ativos e seu ultimo timestamp |

### MVP 3 — Inteligencia artificial

| Status | Prioridade | Complexidade | Feature |
|--------|------------|--------------|---------|
| `[ ]`  | P2         | XL           | Pipeline de coleta de dataset com rotulagem manual |
| `[ ]`  | P2         | XL           | Treinamento de modelo em Python/PyTorch para estimativa de keypoints |
| `[ ]`  | P2         | L            | Exportacao ONNX e integracao com OnnxSkeletonInferenceModel no backend |
| `[ ]`  | P3         | M            | Sincronizacao opcional com camera para rotulagem supervisionada |

### Infraestrutura e qualidade

| Status | Prioridade | Complexidade | Feature |
|--------|------------|--------------|---------|
| `[ ]`  | P2         | M            | Persistencia em PostgreSQL / TimescaleDB para historico de eventos |
| `[ ]`  | P2         | M            | Testes de integracao do endpoint POST /api/csi/samples com MockMvc |
| `[ ]`  | P2         | S            | CI/CD com GitHub Actions (build + test no push) |
| `[ ]`  | P3         | S            | Documentacao completa em docs/01-overview.md ... docs/10-future-ml-pipeline.md |
| `[ ]`  | P3         | S            | Suporte multiusuario e autenticacao JWT |
| `[ ]`  | P3         | M            | Deploy em producao (VPS ou cloud) |

---

## Concluidas

| Data       | Feature                                                                                   |
|------------|-------------------------------------------------------------------------------------------|
| 2026-05-10 | Backend Spring Boot com CORS e WebSocket configurados                                     |
| 2026-05-10 | Record CsiSample com campo scenario opcional e validacao de 30 subportadoras              |
| 2026-05-10 | Endpoint POST /api/csi/samples com rejeicao HTTP 400 para payloads invalidos              |
| 2026-05-10 | Baseline padrao em memoria (meanEnergy=10.0, stdDev=0.3, noiseFloor=0.5)                 |
| 2026-05-10 | Simulador CSI com 7 cenarios e dados sinteticos coerentes com padroes fisicos             |
| 2026-05-10 | Fila circular SampleBuffer e processamento por CsiWindow (1s / ~20 amostras)             |
| 2026-05-10 | MotionDetector producindo MotionState e SignalSummary via RMS, variancia e baseline       |
| 2026-05-10 | ScenarioBasedPostureClassifier: scenario -> PostureState com mapeamento completo          |
| 2026-05-10 | SkeletonService com 6 geradores de pose + UnknownSkeletonGenerator com fallbacks          |
| 2026-05-10 | WebSocket emitindo RoomStateEvent completo                                                |
| 2026-05-10 | Endpoints de debug: /api/debug/latest-samples, /latest-event, /simulator-status          |
| 2026-05-10 | Modo manual com expiracao automatica de 10 segundos                                      |
| 2026-05-10 | Calibracao manual via POST /api/rooms/{roomId}/calibration/start e /finish               |
| 2026-05-10 | Frontend React + TypeScript: dashboard, grafico Recharts, boneco SVG, log de eventos     |
| 2026-05-10 | 43 testes unitarios passando (mvn test)                                                   |
| 2026-05-10 | Docker Compose com healthcheck e dependencia entre servicos                               |
| 2026-05-10 | README, backlog, system-feature-flows e data-model no padrao do projeto                  |

---

## Bugs Conhecidos

| ID  | Descricao                                                                                        | Severidade | Reportado em |
|-----|--------------------------------------------------------------------------------------------------|------------|--------------|
| B01 | Modo manual nao expira automaticamente no frontend (indicador [MODO MANUAL ATIVO] fica ativo ate recarregar) | Baixa | 2026-05-10 |

---

## Notas & Decisoes Pendentes

- Definir se MVP 1 usara spring profile (`rule-based`) ou feature flag no `application.yml` para trocar o PostureClassifier ativo
- Avaliar se o numero de subportadoras (30) deve ser configuravel por `application.yml` antes do MVP 2

---

## Historico de Versoes

| Versao    | Data       | Principais entregas                                             |
|-----------|------------|-----------------------------------------------------------------|
| `0.1.0`   | 2026-05-10 | MVP 0 completo: loop visual simulador -> boneco 2D via WebSocket |
