package dao;

import java.util.List;

/**
 * Interfaz genérica que define las operaciones básicas de persistencia.
 *
 * @param <T>  Tipo de la entidad gestionada
 * @param <ID> Tipo del identificador de la entidad
 */
public interface CrudRepository<T, ID> {

    /**
     * Persiste una nueva entidad en el almacenamiento.
     *
     * @param entidad Objeto a guardar
     * @return La entidad persistida con su ID asignado, o null si falló
     */
    T guardar(T entidad);

    /**
     * Busca una entidad por su identificador único.
     *
     * @param id Identificador a buscar
     * @return La entidad encontrada, o null si no existe
     */
    T buscarPorId(ID id);

    /**
     * Retorna todas las entidades del almacenamiento.
     *
     * @return Lista con todas las entidades; vacía si no hay registros
     */
    List<T> listarTodos();
}

