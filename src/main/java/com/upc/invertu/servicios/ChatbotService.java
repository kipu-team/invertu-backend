package com.upc.invertu.servicios;

import com.upc.invertu.dtos.request.ConsultaChatbotRequestDTO;
import com.upc.invertu.dtos.response.ConsultaChatbotResponseDTO;

/** EP-02: chatbot financiero (solo Premium) */
public interface ChatbotService {

    /** END-CHAT-01: responde una consulta financiera con los datos del estudiante autenticado */
    ConsultaChatbotResponseDTO consultar(ConsultaChatbotRequestDTO dto);
}
