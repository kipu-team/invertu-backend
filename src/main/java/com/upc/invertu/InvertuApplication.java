package com.upc.invertu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling // procesos diarios de recordatorios y alertas (programados/)
public class InvertuApplication {

    public static void main(String[] args) {
        // Fechas del sistema en hora de Peru (reglas de "fecha no futura", procesos diarios).
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        SpringApplication.run(InvertuApplication.class, args);
    }
}
