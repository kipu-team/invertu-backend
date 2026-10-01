package com.upc.invertu.configuracion;

import com.upc.invertu.entidades.Categoria;
import com.upc.invertu.entidades.Plan;
import com.upc.invertu.seguridad.entidades.Rol;
import com.upc.invertu.repositorios.CategoriaRepositorio;
import com.upc.invertu.repositorios.PlanRepositorio;
import com.upc.invertu.seguridad.repositorios.RolRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Carga inicial de datos: roles, planes y categorias predeterminadas.
 * Se ejecuta al iniciar la aplicacion y no duplica registros si ya existen,
 * asi funciona igual en local y en el servidor sin borrar datos.
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    public static final String ROLE_FREE = "ROLE_FREE";
    public static final String ROLE_PREMIUM = "ROLE_PREMIUM";

    // Pendiente de confirmar la lista final con el equipo (US-11 / US-15).
    private static final List<String> CATEGORIAS_PREDETERMINADAS = List.of(
            "Alimentación", "Transporte", "Educación", "Entretenimiento", "Salud",
            "Servicios", "Trabajo", "Mesada", "Otros");

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private PlanRepositorio planRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Override
    @Transactional
    public void run(String... args) {
        Rol free = obtenerOCrearRol(ROLE_FREE);
        Rol premium = obtenerOCrearRol(ROLE_PREMIUM);

        // Plan Free: S/ 0.00, hasta 3 metas activas y 3 recordatorios
        crearPlanSiNoExiste(free, new BigDecimal("0.00"), 3, 3);
        // Plan Premium: S/ 9.90 referencial, sin limites (null)
        crearPlanSiNoExiste(premium, new BigDecimal("9.90"), null, null);

        for (String nombre : CATEGORIAS_PREDETERMINADAS) {
            if (!categoriaRepositorio.existsByNombreAndEstudianteIsNull(nombre)) {
                Categoria categoria = new Categoria();
                categoria.setNombre(nombre);
                categoriaRepositorio.save(categoria);
            }
        }
        log.info("Datos iniciales verificados: roles, planes y categorias predeterminadas.");
    }

    private Rol obtenerOCrearRol(String nombre) {
        return rolRepositorio.findByNombre(nombre)
                .orElseGet(() -> rolRepositorio.save(new Rol(nombre)));
    }

    private void crearPlanSiNoExiste(Rol rol, BigDecimal precio, Integer maxMetas, Integer maxRecordatorios) {
        if (planRepositorio.existsByRolNombre(rol.getNombre())) {
            return;
        }
        Plan plan = new Plan();
        plan.setRol(rol);
        plan.setPrecioMensual(precio);
        plan.setMaxMetasActivas(maxMetas);
        plan.setMaxRecordatorios(maxRecordatorios);
        planRepositorio.save(plan);
    }
}
