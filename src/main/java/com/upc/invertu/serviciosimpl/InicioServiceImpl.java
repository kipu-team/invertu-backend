package com.upc.invertu.serviciosimpl;

import com.upc.invertu.dtos.response.OrientacionResponseDTO;
import com.upc.invertu.repositorios.MetaRepositorio;
import com.upc.invertu.repositorios.MovimientoRepositorio;
import com.upc.invertu.seguridad.entidades.Estudiante;
import com.upc.invertu.seguridad.utilidades.EstudianteAutenticado;
import com.upc.invertu.servicios.InicioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InicioServiceImpl implements InicioService {

    @Autowired
    private MovimientoRepositorio movimientoRepositorio;

    @Autowired
    private MetaRepositorio metaRepositorio;

    @Autowired
    private EstudianteAutenticado estudianteAutenticado;

    /** END-DASH-01: la orientacion se muestra hasta que registre un movimiento y cree una meta */
    @Override
    @Transactional(readOnly = true)
    public OrientacionResponseDTO obtenerOrientacion() {
        Estudiante estudiante = estudianteAutenticado.obtener();
        Long idEstudiante = estudiante.getIdEstudiante();

        boolean movimientoRegistrado = movimientoRepositorio.existsByEstudianteIdEstudiante(idEstudiante);
        boolean metaRegistrada = metaRepositorio.existsByEstudianteIdEstudiante(idEstudiante);
        boolean mostrarOrientacion = !(movimientoRegistrado && metaRegistrada);

        return new OrientacionResponseDTO(estudiante.getNombres(), movimientoRegistrado,
                metaRegistrada, mostrarOrientacion);
    }
}
