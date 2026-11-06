package DAO_clases_principales;

import java.util.List;

public interface CrudDAO<T> {
    void guardar(T entidad);
    
    void actualizar(T entidad);
    
    T buscarPorId(int id);
    
    List<T> listarTodos();
}
