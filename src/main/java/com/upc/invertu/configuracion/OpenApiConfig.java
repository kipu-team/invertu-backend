package com.upc.invertu.configuracion;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger con boton "Authorize" para pegar el token JWT y probar los endpoints protegidos.
 * URL local: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI invertuOpenApi() {
        String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("InvertU API")
                        .description("API REST de InvertU - Kipu")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
