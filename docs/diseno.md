# Diseno del proyecto

Estos diagramas representan el codigo actual. La conexion se conserva en conexion.conexionDB para evitar duplicar el Singleton existente.

## Capas y responsabilidades

```mermaid
flowchart LR
    M[Main y AplicacionDesktop] --> V[Vistas Swing]
    M --> C[AplicacionControladores]
    V --> C
    C --> S[ServiciosAplicacion]
    S --> D[DAO]
    D --> X[conexionDB]
    X --> B[(MySQL)]
    V -. presenta .-> O[Modelo]
    C -. transporta .-> O
    S -. valida y calcula .-> O
    D -. reconstruye .-> O
```

Main es el inicio. AplicacionDesktop construye y enlaza objetos. Esa composicion no es otra conexion ni otra capa de reglas financieras.

## Relaciones del modelo

```mermaid
classDiagram
    class Usuario {
        <<abstract>>
        -int id
        -String nombre
        -String username
        -String passwordHash
        -Rol rol
        +obtenerPermisos()
        +mostrarMenu()
        +tienePermiso()
    }
    Usuario <|-- Administrador
    Usuario <|-- Jefe
    Usuario <|-- Cajero
    class Rol {
        <<enumeration>>
        ADMINISTRADOR
        JEFE
        CAJERO
    }
    class Permiso {
        <<enumeration>>
    }
    Usuario --> Rol
    Usuario --> Permiso
    Usuario "1" <-- "*" Venta : cajero
    Usuario "1" <-- "*" Compra : responsable
    Venta "1" *-- "*" DetalleVenta
    Compra "1" *-- "*" DetalleCompra
    DetalleVenta "*" --> "1" Producto
    DetalleCompra "*" --> "1" Producto
    class Gasto {
        <<abstract>>
        +calcularMontoDiario()
    }
    Gasto <|-- GastoFijo
    Gasto <|-- GastoVariable
    Gasto "*" --> "1" Usuario
    RegistroCambio "*" --> "1" Venta
    RegistroCambio "*" --> "1" Usuario
    CierreDiario "*" --> "1" Usuario
    class RegistroCambio {
        <<final>>
    }
    class BalanceDia {
        <<final>>
    }
    class Negocio
    class CostoFijo
```

Los detalles copian los precios y costos historicos. RegistroCambio congela sus valores, fecha y motivo; sus referencias a venta y usuario son asociaciones. BalanceDia transporta cifras e indicadores de meta calculable o estimada.

## Relacion de las tablas

```mermaid
erDiagram
    USUARIOS ||--o{ VENTAS : registra
    USUARIOS ||--o{ COMPRAS : registra
    USUARIOS ||--o{ GASTOS : registra
    USUARIOS ||--o{ CIERRES_DIARIOS : cierra
    USUARIOS ||--o{ HISTORIAL_CAMBIOS : modifica
    VENTAS ||--|{ DETALLE_VENTA : contiene
    PRODUCTOS ||--o{ DETALLE_VENTA : aparece
    COMPRAS ||--|{ DETALLE_COMPRA : contiene
    PRODUCTOS ||--o{ DETALLE_COMPRA : aparece
    VENTAS ||--o{ HISTORIAL_CAMBIOS : tiene
    COSTOS_FIJOS {
        int id PK
        string concepto
        decimal monto_mensual
        boolean activo
    }
    CONFIGURACION {
        string clave PK
        string valor
    }
```

La estructura concreta esta en sql/esquema.sql. Los detalles de una venta son unicos por venta y producto. username y fecha de cierre tienen restricciones unicas.

## Conceptos de POO que debe explicar el grupo

| Concepto | Ejemplo del codigo |
| --- | --- |
| Encapsulacion | Atributos privados y validaciones de Producto y Usuario. |
| Herencia | Cajero, Jefe y Administrador heredan de Usuario. |
| Abstraccion | Usuario y Gasto no se instancian directamente. |
| Polimorfismo | Cada Gasto calcula su importe diario de forma distinta; los estados del semaforo tienen mensajes distintos. |
| Interfaces | CrudDAO, EstadoNegocio, Exportable y los contratos de vista. |
| Composicion | ServiciosAplicacion y AplicacionControladores reciben y comparten objetos. |
| Excepciones | Stock insuficiente, permiso denegado y dia cerrado comunican fallos de negocio. |
| Genericos | CrudDAO<T> y DAOBase<T> reutilizan operaciones para distintas entidades. |
| Singleton | Sesion y conexionDB tienen propositos distintos; hay un solo administrador de conexion. |

## Cuatro ayudas de Java que conviene estudiar

BigDecimal evita errores de dinero; no es una libreria externa. Las expresiones con -> son acciones cortas que se entregan a botones o tareas. La cola de EjecutorSwing evita bloquear la interfaz y usar JDBC simultaneamente. PBKDF2 es el mecanismo del JDK para guardar hashes de claves: el grupo utiliza la API, no inventa criptografia.

Estas ayudas estan aisladas y comentadas. El resto del trabajo se expresa mediante objetos, metodos, listas, condicionales, ciclos y JDBC.
