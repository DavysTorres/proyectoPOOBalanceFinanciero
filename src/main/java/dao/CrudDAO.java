package dao;

import java.util.List;

<<<<<<< HEAD
public interface CrudDAO<T> {
    
    void guardar(T entidad);
    
    void actualizar(T entidad);
    
    T buscarPorId(int id);
    
    List<T> listarTodos();    
=======
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
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
}
