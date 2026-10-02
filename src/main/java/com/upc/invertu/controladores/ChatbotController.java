package com.upc.invertu.controladores;

import com.upc.invertu.dtos.request.ConsultaChatbotRequestDTO;
import com.upc.invertu.dtos.response.ConsultaChatbotResponseDTO;
import com.upc.invertu.servicios.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** END-CHAT-01 */
@RestController
@RequestMapping("/api/v1/chatbot")
public class ChatbotController {
    @Autowired
    private ChatbotService chatbotService;

    /** END-CHAT-01: consulta al chatbot financiero (solo Premium). FREE -> 403 */
    @PostMapping("/consultas")
    @PreAuthorize("hasRole('PREMIUM')")
    public ResponseEntity<ConsultaChatbotResponseDTO> consultar(@Valid @RequestBody ConsultaChatbotRequestDTO dto) {
        // @Valid aplica las validaciones del DTO -> 400 si fallan
        return ResponseEntity.ok(chatbotService.consultar(dto));
    }
}
