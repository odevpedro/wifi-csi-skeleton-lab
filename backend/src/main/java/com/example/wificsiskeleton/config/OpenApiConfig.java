package com.example.wificsiskeleton.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wi-Fi CSI Skeleton Lab API")
                        .description("""
                                API REST do laboratorio experimental de Wi-Fi sensing.
                                Recebe amostras CSI, detecta movimento (MotionState), estima postura (PostureState)
                                e publica eventos em tempo real via WebSocket STOMP.

                                **WebSocket:** `ws://localhost:8080/ws` — topico `/topic/rooms/{roomId}/events`

                                **Aviso MVP 0:** o PostureState e derivado do campo `scenario` do simulador,
                                nao inferido do sinal Wi-Fi real.
                                """)
                        .version("0.1.0")
                        .license(new License().name("MIT")))
                .tags(List.of(
                        new Tag().name("CSI").description("Ingestao de amostras CSI"),
                        new Tag().name("Rooms").description("Estado e calibracao por sala"),
                        new Tag().name("Simulation").description("Modo manual de debug"),
                        new Tag().name("Debug").description("Inspecao interna do pipeline"),
                        new Tag().name("Health").description("Status do servico")
                ));
    }
}
