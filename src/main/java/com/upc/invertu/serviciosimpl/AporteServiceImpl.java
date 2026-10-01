package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.AporteResponseDTO;
import com.upc.invertu.entidades.Aporte;
import com.upc.invertu.excepciones.RecursoNoEncontradoException;
import com.upc.invertu.repositorios.AporteRepositorio;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.AporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Long idEstudiante = estudianteAutenticado.obtener().getIdEstudiante();

        // Antes de mostrar los aportes se valida que la meta sea del estudiante
        if (metaRepositorio.findByIdMetaAndEstudianteIdEstudiante(idMeta, idEstudiante).isEmpty()) {
            throw new RecursoNoEncontradoException("La meta no existe");
        }

        return aporteRepositorio.findByMetaIdMetaOrderByFechaAsc(idMeta).stream()
                .map(this::aDTO)
                .toList();
    }

    private AporteResponseDTO aDTO(Aporte aporte) {
        AporteResponseDTO dto = new AporteResponseDTO();
        dto.setIdAporte(aporte.getIdAporte());
        dto.setFecha(aporte.getFecha());
        dto.setMonto(aporte.getMonto());
        dto.setDescripcion(aporte.getDescripcion());
        return dto;
    }
}
