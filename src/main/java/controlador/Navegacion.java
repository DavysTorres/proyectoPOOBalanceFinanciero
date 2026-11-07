package controlador;

import modelo.Usuario;

/**
 * La capa de vistas decide como construir y mostrar cada ventana.
 */
public interface Navegacion {
    void abrirMenu(Usuario usuario);

    void abrir(Pantalla pantalla);

    void volverAlLogin();
}
