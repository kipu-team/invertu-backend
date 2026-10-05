package com.upc.invertu.integraciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upc.invertu.excepciones.ServicioExternoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Llamadas a la API externa de IA: chatbot (END-CHAT-01) y lectura de comprobantes (END-TRX-03).
 * Proveedor: DeepSeek (API compatible con OpenAI). La clave se lee de la variable de entorno
 * IA_API_KEY (propiedad ia.api-key). Si el servicio no responde, lanza ServicioExternoException (503).
 * No se registra en logs el contenido de los mensajes ni de las respuestas.
 */
@Slf4j
@Component
public class IaCliente {

    public static final String MENSAJE_NO_DISPONIBLE =
            "El asistente financiero no está disponible en este momento. Intenta nuevamente más tarde";

    private static final String URL = "https://api.deepseek.com/chat/completions";
    private static final String MODELO = "deepseek-chat";
    private static final int MAX_TOKENS = 500;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    @Value("${ia.api-key:}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .requestFactory(fabricaConTimeout())
            .build();

    /**
     * Envia las instrucciones de sistema y el mensaje del usuario; devuelve el texto de la respuesta.
     * Error HTTP, timeout o respuesta vacia -> ServicioExternoException (503).
     */
    public String generarRespuesta(String instruccionesSistema, String mensajeUsuario) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No se configuro la clave de la IA (IA_API_KEY)");
            throw new ServicioExternoException(MENSAJE_NO_DISPONIBLE);
        }
        Map<String, Object> cuerpo = Map.of(
                "model", MODELO,
                "max_tokens", MAX_TOKENS,
                "messages", List.of(
                        Map.of("role", "system", "content", instruccionesSistema),
                        Map.of("role", "user", "content", mensajeUsuario)));
        RespuestaIa respuesta;
        try {
            respuesta = restClient.post()
                    .uri(URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(RespuestaIa.class);
        } catch (RestClientException ex) {
            // Solo el tipo de error: el mensaje podria incluir el contenido de la respuesta
            log.warn("La IA no respondio correctamente: {}", ex.getClass().getSimpleName());
            throw new ServicioExternoException(MENSAJE_NO_DISPONIBLE);
        }
        String contenido = extraerContenido(respuesta);
        if (contenido == null || contenido.isBlank()) {
            log.warn("La IA devolvio una respuesta vacia");
            throw new ServicioExternoException(MENSAJE_NO_DISPONIBLE);
        }
        return contenido.trim();
    }

    private String extraerContenido(RespuestaIa respuesta) {
        if (respuesta == null || respuesta.choices() == null || respuesta.choices().isEmpty()) {
            return null;
        }
        Opcion opcion = respuesta.choices().get(0);
        return opcion == null || opcion.message() == null ? null : opcion.message().content();
    }

    // 15 s para conectar y 15 s para recibir la respuesta
    private static JdkClientHttpRequestFactory fabricaConTimeout() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
        fabrica.setReadTimeout(TIMEOUT);
        return fabrica;
    }

    // Formato de respuesta de la API compatible con OpenAI (solo los campos que se usan)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaIa(List<Opcion> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Opcion(MensajeIa message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MensajeIa(String content) {}
}
