package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.MovimientoRequestDTO;
import com.upc.invertu.dtos.response.*;
import com.upc.invertu.entidades.Categoria;
import com.upc.invertu.entidades.Movimiento;
import com.upc.invertu.entidades.Suscripcion;
import com.upc.invertu.entidades.enums.Clasificacion;
import com.upc.invertu.entidades.enums.EstadoSuscripcion;
import com.upc.invertu.entidades.enums.MedioPago;
import com.upc.invertu.entidades.enums.TipoMovimiento;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
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

import java.time.YearMonth;
import java.util.List;

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

    /** END-TRX-01: movimientos del mes con busqueda y filtros opcionales (US-10 y US-14) */
    @Override
    @Transactional(readOnly = true)
    public MovimientoListaResponseDTO listarDelMes(int anio, int mes, String busqueda, TipoMovimiento tipo,
                                                   Clasificacion clasificacion, Long idCategoria,
                                                   MedioPago medioPago) {
        if (mes < 1 || mes > 12) {
            throw new ReglaNegocioException("Datos inválidos");
        }
        Estudiante estudiante = estudianteAutenticado.obtener();

        // Sin busqueda se envia "" para que el LIKE '%%' coincida con todo
        String texto = (busqueda == null) ? "" : busqueda.trim();

        // YearMonth calcula el primer y ultimo dia del mes (28, 29, 30 o 31)
        YearMonth periodo = YearMonth.of(anio, mes);
        List<MovimientoItemResponseDTO> movimientos = movimientoRepositorio
                .buscar(estudiante.getIdEstudiante(), periodo.atDay(1), periodo.atEndOfMonth(),
                        texto, tipo, clasificacion, idCategoria, medioPago)
                .stream()
                .map(this::aItemDTO)
                .toList();

        MovimientoListaResponseDTO respuesta = new MovimientoListaResponseDTO();
        respuesta.setTotal(movimientos.size());
        respuesta.setMovimientos(movimientos); // lista vacia si no hay coincidencias
        return respuesta;
    }

    /** END-TRX-04: detalle de un movimiento propio */
    @Override
    @Transactional(readOnly = true)
    public MovimientoDetalleResponseDTO obtenerDetalle(Long idMovimiento) {
        Movimiento movimiento = buscarPropio(idMovimiento);

        MovimientoDetalleResponseDTO dto = new MovimientoDetalleResponseDTO();
        dto.setIdMovimiento(movimiento.getIdMovimiento());
        dto.setTipo(movimiento.getTipo());
        dto.setClasificacion(movimiento.getClasificacion());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setFecha(movimiento.getFecha());
        dto.setMonto(movimiento.getMonto());
        dto.setCategoria(movimiento.getCategoria().getNombre());
        dto.setMedioPago(movimiento.getMedioPago());
        dto.setSuscripcion(nombreSuscripcion(movimiento));
        // Pendiente T-47: generar el enlace temporal (5 min) cuando existan comprobantes
        dto.setUrlComprobante(null);
        return dto;
    }

    /** END-TRX-05: edita un movimiento propio con las mismas reglas del registro */
    @Override
    @Transactional
    public MovimientoResponseDTO actualizar(Long idMovimiento, MovimientoRequestDTO dto) {
        Movimiento movimiento = buscarPropio(idMovimiento); // 404 si no existe o es de otro estudiante

        // Un ingreso no puede ser pago de una suscripcion: si el tipo es INGRESO, se quita
        if (dto.getTipo() == TipoMovimiento.INGRESO) {
            dto.setIdSuscripcion(null);
        }

        aplicarDatos(movimiento, dto, movimiento.getEstudiante().getIdEstudiante());
        // No se modifica urlComprobante: el movimiento conserva su comprobante
        return aDTO(movimientoRepositorio.save(movimiento));
    }

    /** END-TRX-06: elimina un movimiento propio */
    @Override
    @Transactional
    public MensajeResponseDTO eliminar(Long idMovimiento) {
        Movimiento movimiento = buscarPropio(idMovimiento, "El movimiento no existe o ya fue eliminado");

        // Pendiente T-47: si tiene comprobante (urlComprobante != null), borrar tambien su archivo
        movimientoRepositorio.delete(movimiento);
        return new MensajeResponseDTO("Movimiento eliminado");
    }
    /**
     * Valida y copia los datos del request al movimiento.
     * Se usa en el registro (END-TRX-02) y en la edicion (END-TRX-05), que tienen las mismas reglas.
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
        dto.setSuscripcion(nombreSuscripcion(movimiento));
        dto.setTieneComprobante(movimiento.getUrlComprobante() != null);
        return dto;
    }
    /**
     * Busca un movimiento solo si es del estudiante autenticado; si no, 404.
     * Se usa en el detalle y la edicion.
     */
    private Movimiento buscarPropio(Long idMovimiento) {
        return buscarPropio(idMovimiento, "El movimiento no existe");
    }

    // Misma busqueda, pero con un mensaje de error propio (la eliminacion usa otro texto)
    private Movimiento buscarPropio(Long idMovimiento, String mensajeError) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        return movimientoRepositorio.findByIdMovimientoAndEstudianteIdEstudiante(idMovimiento, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException(mensajeError));
    }

    private MovimientoItemResponseDTO aItemDTO(Movimiento movimiento) {
        MovimientoItemResponseDTO dto = new MovimientoItemResponseDTO();
        dto.setIdMovimiento(movimiento.getIdMovimiento());
        dto.setFecha(movimiento.getFecha());
        dto.setDescripcion(movimiento.getDescripcion());
        dto.setCategoria(movimiento.getCategoria().getNombre());
        dto.setTipo(movimiento.getTipo());
        dto.setClasificacion(movimiento.getClasificacion());
        dto.setMonto(movimiento.getMonto());
        dto.setMedioPago(movimiento.getMedioPago());
        dto.setSuscripcion(nombreSuscripcion(movimiento));
        dto.setTieneComprobante(movimiento.getUrlComprobante() != null);
        return dto;
    }

    // Nombre del servicio si el movimiento es pago de una suscripcion; si no, null
    private String nombreSuscripcion(Movimiento movimiento) {
        return movimiento.getSuscripcion() != null
                ? movimiento.getSuscripcion().getNombreServicio()
                : null;
    }

}
