package servicio;

import dao.UsuarioDAO;
import excepciones.*;
import modelo.Permiso;
import modelo.Usuario;

import java.security.*;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.time.*;
import java.util.*;

/**
 * Valida credenciales y aplica bloqueo temporal en la instancia compartida por la aplicacion.
 */
public class ServicioAutenticacion extends ServicioBase {
    private static final int ITERACIONES = 600000;
    private final UsuarioDAO usuarioDAO;
    private final Map<String, Integer> intentosFallidos = new HashMap<>();
    private final Map<String, Instant> bloqueadosHasta = new HashMap<>();
    private final int maxIntentos = 3;
    private final Duration tiempoBloqueo = Duration.ofMinutes(5);

    // El constructor normal no consulta MySQL hasta que se intenta autenticar.
    public ServicioAutenticacion() {
        this(new UsuarioDAO(), Clock.systemDefaultZone());
    }

    public ServicioAutenticacion(UsuarioDAO usuarioDAO, Clock reloj) {
        super(reloj);
        this.usuarioDAO = Objects.requireNonNull(usuarioDAO);
    }

    // Limpia una sesion anterior antes de comprobar una nueva identidad.
    public Usuario iniciarSesion(String username, String clave) {
        sesion.cerrar();
        String nombre = texto(username, "El usuario");
        Usuario u = usuarioDAO.buscarPorUsername(nombre);
        // Usa el nombre canonico guardado para que mayusculas o acentos no evadan el bloqueo.
        String identificador = (u == null ? nombre : u.getUsername()).toLowerCase(Locale.ROOT);
        Instant ahora = reloj.instant(), limite = bloqueadosHasta.get(identificador);
        if (limite != null && ahora.isBefore(limite))
            throw new UsuarioBloqueadoException("Usuario bloqueado por cinco minutos");
        if (limite != null) {
            bloqueadosHasta.remove(identificador);
            intentosFallidos.remove(identificador);
        }
        if (u == null || !u.isActivo() || !verificarClave(clave, u.getPasswordHash())) {
            int intentos = intentosFallidos.getOrDefault(identificador, 0) + 1;
            intentosFallidos.put(identificador, intentos);
            if (intentos >= maxIntentos) {
                bloqueadosHasta.put(identificador, ahora.plus(tiempoBloqueo));
                throw new UsuarioBloqueadoException("Usuario bloqueado por cinco minutos");
            }
            throw new CredencialesInvalidasException("Usuario o clave incorrectos");
        }
        if (esHashAnterior(u.getPasswordHash())) {
            String actualizado = generarHash(clave);
            usuarioDAO.actualizarPassword(u.getId(), actualizado);
            u.setPasswordHash(actualizado);
        }
        intentosFallidos.remove(identificador);
        bloqueadosHasta.remove(identificador);
        sesion.iniciar(u);
        return u;
    }

    // La instancia debe conservarse: crear otra en cada clic perderia los contadores.
    public void cerrarSesion() {
        sesion.cerrar();
    }

    // Aunque la guia diga cifrar, se guarda un hash con sal, no una clave reversible.
    public String cifrarClave(String clave) {
        validarClaveNueva(clave);
        return generarHash(clave);
    }

    // La migracion conserva claves antiguas, incluso las de menos de ocho caracteres.
    private String generarHash(String clave) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] hash = derivar(clave, sal);
        return "pbkdf2-sha256$" + ITERACIONES + "$" + Base64.getEncoder().encodeToString(sal)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    // SHA-256 hexadecimal solo para cuentas antiguas; se migra tras autenticar.
    public boolean verificarClave(String clave, String almacenado) {
        if (clave == null || clave.length() > 256 || almacenado == null) return false;
        if (esHashAnterior(almacenado)) {
            try {
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8));
                return MessageDigest.isEqual(HexFormat.of().parseHex(almacenado), hash);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 no disponible", e);
            }
        }
        String[] partes = almacenado.split("\\$", -1);
        if (partes.length != 4 || !partes[0].equals("pbkdf2-sha256")
                || !partes[1].equals(Integer.toString(ITERACIONES))) return false;
        try {
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hash = Base64.getDecoder().decode(partes[3]);
            if (sal.length != 16 || hash.length != 32) return false;
            return MessageDigest.isEqual(hash, derivar(clave, sal));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean esHashAnterior(String hash) {
        return hash != null && hash.matches("[0-9a-fA-F]{64}");
    }

    // Solo el administrador puede cambiar claves; el usuario debe existir.
    public void restablecerClave(int usuarioId, String nueva) {
        sesion.exigirPermiso(Permiso.GESTIONAR_USUARIOS);
        if (usuarioDAO.buscarPorId(usuarioId) == null) throw new DatosInvalidosException("Usuario inexistente");
        usuarioDAO.actualizarPassword(usuarioId, cifrarClave(nueva));
    }

    // Limite sencillo de entrada para este proyecto; no se recortan espacios de una clave.
    private void validarClaveNueva(String clave) {
        if (clave == null || clave.isBlank() || clave.length() < 8 || clave.length() > 256)
            throw new DatosInvalidosException("La clave debe tener entre 8 y 256 caracteres");
    }

    // PBKDF2 pertenece al JDK; no se inventa un algoritmo de seguridad propio.
    private byte[] derivar(String clave, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, ITERACIONES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo generar el hash", e);
        } finally {
            spec.clearPassword();
        }
    }
}
