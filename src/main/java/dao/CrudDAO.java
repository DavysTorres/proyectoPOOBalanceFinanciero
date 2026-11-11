package dao;

import java.util.List;

/**
 * Contrato generico para entidades editables; cierres e historial usan contratos de solo registro.
 */
public interface CrudDAO<T> {
    // Inserta una entidad nueva y le asigna el identificador generado.
    void guardar(T entidad);

    // Modifica una entidad existente; las bajas son logicas, no DELETE.
    void actualizar(T entidad);

    // Devuelve null cuando no existe el identificador solicitado.
    T buscarPorId(int id);

    // Devuelve una lista vacia cuando todavia no hay registros.
    List<T> listarTodos();
}
