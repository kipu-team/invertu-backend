package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.request.AporteRequestDTO;
import com.upc.invertu.dtos.response.AporteResponseDTO;
import com.upc.invertu.entidades.Aporte;
import com.upc.invertu.entidades.Meta;
import com.upc.invertu.entidades.enums.EstadoMeta;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.excepciones.ReglaNegocioException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.AporteService;
import com.upc.invertu.utilidades.CalculosMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AporteServiceImpl implements AporteService {

    @Autowired
    private AporteRepositorio aporteRepositorio;

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-GOAL-06: historial de aportes de una meta propia, del mas antiguo al mas reciente */
    @Override
    @Transactional(readOnly = true)
    public List<AporteResponseDTO> listar(Long idMeta) {
        buscarMetaPropia(idMeta); // 404 si la meta no existe o es de otro estudiante

        return aporteRepositorio.findByMetaIdMetaOrderByFechaAsc(idMeta).stream()
                .map(this::aDTO)
                .toList();
    }

    /**
     * END-GOAL-12: registra un aporte a una meta activa.
     * Si con este aporte se alcanza el objetivo, la meta pasa a CUMPLIDA.
     */
    @Override
    @Transactional
    public AporteResponseDTO registrar(Long idMeta, AporteRequestDTO dto) {
        Meta meta = buscarMetaPropia(idMeta);
        if (meta.getEstado() != EstadoMeta.ACTIVA) {
            throw new ReglaNegocioException("Solo se puede aportar a metas activas");
        }

        // El aporte no puede superar lo que falta para llegar al objetivo
        BigDecimal aportadoAntes = sumarAportes(idMeta);
        BigDecimal restante = meta.getMontoObjetivo().subtract(aportadoAntes);
        if (dto.getMonto().compareTo(restante) > 0) {
            throw new ReglaNegocioException("El aporte debe ser mayor a S/ 0.00 y no superar el monto restante");
        }

        Aporte aporte = new Aporte();
        aporte.setMeta(meta);
        aporte.setMonto(dto.getMonto());
        aporte.setFecha(dto.getFecha());
        aporte.setDescripcion(limpiarTexto(dto.getDescripcion()));
        aporte = aporteRepositorio.save(aporte);

        BigDecimal aportadoAhora = aportadoAntes.add(dto.getMonto());
        if (aportadoAhora.compareTo(meta.getMontoObjetivo()) >= 0) {
            // Se alcanzo el objetivo: la meta se cumple y libera su cupo del plan Free
            meta.setEstado(EstadoMeta.CUMPLIDA);
            meta.setFechaCumplimiento(LocalDate.now());
            meta.setProximaFechaAporte(null);
        } else {
            meta.setProximaFechaAporte(
                    CalculosMeta.proximaFechaAporte(meta.getFrecuenciaAporte(), meta.getFechaObjetivo()));
        }
        metaRepositorio.save(meta);

        AporteResponseDTO respuesta = estadoDeLaMeta(meta, aportadoAhora);
        respuesta.setIdAporte(aporte.getIdAporte());
        respuesta.setEstadoMeta(meta.getEstado());
        return respuesta;
    }

    /** END-GOAL-13: elimina un aporte de una meta activa */
    @Override
    @Transactional
    public AporteResponseDTO eliminar(Long idMeta, Long idAporte) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        Aporte aporte = aporteRepositorio
                .findByIdAporteAndMetaIdMetaAndMetaEstudianteIdEstudiante(idAporte, idMeta, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("El aporte no existe"));

        Meta meta = aporte.getMeta();
        if (meta.getEstado() != EstadoMeta.ACTIVA) {
            throw new ReglaNegocioException("Solo se pueden eliminar aportes de metas activas");
        }

        aporteRepositorio.delete(aporte);
        return estadoDeLaMeta(meta, sumarAportes(idMeta));
    }

    /** Busca una meta solo si es del estudiante autenticado; si no, 404. */
    private Meta buscarMetaPropia(Long idMeta) {
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();
        return metaRepositorio.findByIdMetaAndEstudianteIdEstudiante(idMeta, idEstudiante)
                .orElseThrow(() -> new RecursoNoEncontradoException("La meta no existe"));
    }

    // SUM devuelve null cuando la meta no tiene aportes: en ese caso el total es 0
    private BigDecimal sumarAportes(Long idMeta) {
        BigDecimal suma = aporteRepositorio.sumarAportes(idMeta);
        return suma != null ? suma : BigDecimal.ZERO;
    }

    // Texto opcional: si viene vacio o con solo espacios se guarda null
    private String limpiarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    // Respuesta de END-GOAL-12 y 13: como queda la meta despues del cambio
    private AporteResponseDTO estadoDeLaMeta(Meta meta, BigDecimal aportado) {
        AporteResponseDTO dto = new AporteResponseDTO();
        dto.setMontoAportado(aportado);
        dto.setPorcentaje(CalculosMeta.porcentaje(aportado, meta.getMontoObjetivo()));
        return dto;
    }

    // Respuesta de END-GOAL-06: cada aporte del historial
    private AporteResponseDTO aDTO(Aporte aporte) {
        AporteResponseDTO dto = new AporteResponseDTO();
        dto.setIdAporte(aporte.getIdAporte());
        dto.setFecha(aporte.getFecha());
        dto.setMonto(aporte.getMonto());
        dto.setDescripcion(aporte.getDescripcion());
        return dto;
    }
}