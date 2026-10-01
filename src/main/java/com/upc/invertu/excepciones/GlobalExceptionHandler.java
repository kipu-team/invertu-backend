package com.upc.invertu.excepciones;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Convierte las excepciones en respuestas { "error": "..." } con el codigo HTTP
 * que indican las User Stories (400, 401, 403, 404, 409, 422, 429, 503).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Excepciones propias ----------

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> reglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponseDTO> conflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LimitePlanException.class)
    public ResponseEntity<ErrorResponseDTO> limitePlan(LimitePlanException ex) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ArchivoNoProcesableException.class)
    public ResponseEntity<ErrorResponseDTO> archivoNoProcesable(ArchivoNoProcesableException ex) {
        return respuesta(HttpStatus.valueOf(422), ex.getMessage());
    }

    @ExceptionHandler(LimiteIntentosException.class)
    public ResponseEntity<ErrorResponseDTO> limiteIntentos(LimiteIntentosException ex) {
        return respuesta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponseDTO> servicioExterno(ServicioExternoException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // ---------- Seguridad ----------

    /** Login con correo o contrasena incorrectos. Mensaje generico: no revela si el correo existe. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> credencialesInvalidas(BadCredentialsException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
    }

    /**
     * @PreAuthorize denegado (por ejemplo, un estudiante FREE llama a un endpoint PREMIUM).
     * Sin este handler, la excepcion caeria en el handler generico y responderia 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> accesoDenegado(AccessDeniedException ex) {
        return respuesta(HttpStatus.FORBIDDEN, "Esta función es exclusiva del plan Premium");
    }

    // ---------- Validaciones y formato ----------

    /** @Valid en los DTO: devuelve el primer mensaje de validacion. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponseDTO> formatoInvalido(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "Datos inválidos");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> rutaNoExiste(NoResourceFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "Recurso no encontrado");
    }

    // ---------- Cualquier otro error ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> generico(Exception ex) {
        log.error("Error inesperado", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado");
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponseDTO(mensaje));
    }
}
