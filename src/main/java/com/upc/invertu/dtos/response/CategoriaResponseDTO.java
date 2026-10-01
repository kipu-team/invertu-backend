package com.upc.invertu.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** END-CAT-01, END-CAT-02 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {
    // TODO: atributos segun la tabla de endpoints del informe (seccion 2.3)
    private Long idCategoria;
    private String nombre;
    private boolean personalizada;
}
