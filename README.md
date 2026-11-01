# Balance financiero

## Configurar la base de datos

1. Copia `.env.example` como `.env` en la raiz del proyecto, junto a `pom.xml`.
2. Completa `DB_URL`, `DB_USER` y `DB_PASSWORD` con tus datos de MySQL.
3. Recarga el proyecto Maven en IntelliJ para descargar las dependencias.
4. Configura el directorio de trabajo de la ejecucion como la raiz del proyecto
   (`$PROJECT_DIR$` en IntelliJ).
5. Ejecuta `conexion.PruebaConexion` para comprobar el acceso.

La aplicacion carga `.env` con dotenv-java. Las variables del entorno del sistema
tienen prioridad sobre el archivo; elimina valores antiguos de la configuracion
de ejecucion si quieres usar los de `.env`. Reinicia la aplicacion tras cambiarlos.

Cada colaborador configura su propio `.env`, preferiblemente con un usuario de
base de datos individual. Comparte las credenciales por un canal privado.
No subas `.env` ni archivos compilados a Git; `.env.example` no contiene secretos
y si debe compartirse.

Si una contrasena estuvo en un commit, cambiala en Aiven y elimina el secreto de
los commits afectados antes de volver a subirlos. `.gitignore` no elimina archivos
que Git ya estaba siguiendo.
