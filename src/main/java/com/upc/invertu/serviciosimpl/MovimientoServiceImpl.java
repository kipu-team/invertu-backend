package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.MovimientoResponseDTO;
import com.upc.invertu.entidades.Categoria;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.CategoriaRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.repositorios.SuscripcionRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.MovimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovimientoServiceImpl implements MovimientoService {
    private static final String SUSCRIPCION_NO_VALIDA = "La suscripción seleccionada no es válida para este movimiento";

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private SuscripcionRepositorio suscripcionRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-TRX-02: registra un ingreso o gasto del estudiante autenticado */
    @Override
    @Transactional
    public MovimientoResponseDTO registrar(MovimientoRequestDTO dto) {
        Estudiante estudiante = estudianteAutenticado.obtener();

        Movimiento movimiento = new Movimiento();
        movimiento.setEstudiante(estudiante); // el movimiento siempre es del estudiante del token
        aplicarDatos(movimiento, dto, estudiante.getIdEstudiante());

        return aDTO(movimientoRepositorio.save(movimiento));
    }

    /**
     * Valida y copia los datos del request al movimiento.
     * Se reutilizara en la edicion (T-16, END-TRX-05), que tiene las mismas reglas.
     */
    private void aplicarDatos(Movimiento movimiento, MovimientoRequestDTO dto, Long idEstudiante) {
        // La categoria debe estar activa y ser predeterminada o del propio estudiante
        Categoria categoria = categoriaRepositorio.findDisponible(dto.getIdCategoria(), idEstudiante)
                .orElseThrow(() -> new ReglaNegocioException("Selecciona una categoría válida"));

        // La suscripcion es opcional, solo en gastos, del estudiante y ACTIVA
        Suscripcion suscripcion = null;
        if (dto.getIdSuscripcion() != null) {
            if (dto.getTipo() != TipoMovimiento.GASTO) {
                throw new ReglaNegocioException(SUSCRIPCION_NO_VALIDA);
            }
            suscripcion = suscripcionRepositorio
                    .findByIdSuscripcionAndEstudianteIdEstudianteAndEstado(
                            dto.getIdSuscripcion(), idEstudiante, EstadoSuscripcion.ACTIVA)
                    .orElseThrow(() -> new ReglaNegocioException(SUSCRIPCION_NO_VALIDA));
        }

        movimiento.setTipo(dto.getTipo());
        movimiento.setClasificacion(dto.getClasificacion());
        movimiento.setDescripcion(dto.getDescripcion().trim());
        movimiento.setFecha(dto.getFecha());
        movimiento.setMonto(dto.getMonto());
        movimiento.setCategoria(categoria);
        movimiento.setMedioPago(dto.getMedioPago());
        movimiento.setSuscripcion(suscripcion);
    }

    private MovimientoResponseDTO aDTO(Movimiento movimiento) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setIdMovimiento(movimiento.getIdMovimiento());
        dto.setTipo(movimiento.getTipo());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setFecha(movimiento.getFecha());
        dto.setMonto(movimiento.getMonto());
        dto.setCategoria(movimiento.getCategoria().getNombre());
        // Se envia el nombre del servicio, o null si el movimiento no es pago de una suscripcion
        dto.setSuscripcion(movimiento.getSuscripcion() != null
                ? movimiento.getSuscripcion().getNombreServicio()
                : null);
        dto.setTieneComprobante(movimiento.getUrlComprobante() != null);
        return dto;
    }
}
