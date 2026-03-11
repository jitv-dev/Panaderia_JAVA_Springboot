# 🍞 Panadería Rodrigo — SpringEduManager

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Maven](https://img.shields.io/badge/Maven-3.9-red?logo=apachemaven)
![H2](https://img.shields.io/badge/Database-H2%20%2F%20MySQL-blue)
![Spring Security](https://img.shields.io/badge/Security-Spring%20Security-darkgreen?logo=springsecurity)
![License](https://img.shields.io/badge/License-MIT-yellow)

> Aplicación web educativa desarrollada con el ecosistema de Spring Framework para gestionar productos, clientes y pedidos de la **Panadería Rodrigo**. Proyecto correspondiente al **Módulo 6** del bootcamp de FullStack Java Trainne. Docente Sabina Romero

---

## 📋 Índice

- [Descripción](#-descripción)
- [Tecnologías utilizadas](#-tecnologías-utilizadas)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Credenciales de acceso](#-credenciales-de-acceso)
- [Endpoints REST](#-endpoints-rest)
- [Lecciones del módulo](#-lecciones-del-módulo)
- [Capturas de pantalla](#-capturas-de-pantalla)
- [Autor](#-autor)

---

## 📝 Descripción

**Panadería Rodrigo** es una aplicación web interna desarrollada con Spring Boot que permite gestionar las operaciones diarias de una panadería:

- Registrar y visualizar **productos** (pan, facturas, tortas, etc.)
- Gestionar **clientes** con sus datos de contacto
- Administrar **pedidos** vinculando clientes y productos
- Control de acceso por **roles** (ADMIN / EMPLEADO)
- **API REST** para integración con sistemas externos

---

## 🛠 Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.x | Framework base |
| Spring MVC | 6.x | Arquitectura MVC y controladores web |
| Spring Data JPA | 3.2.x | Acceso a datos y repositorios |
| Spring Security | 6.x | Autenticación y autorización |
| Thymeleaf | 3.1.x | Motor de plantillas HTML |
| H2 Database | 2.x | Base de datos embebida (desarrollo) |
| MySQL | 8.x | Base de datos (producción, opcional) |
| Maven | 3.9.x | Gestor de dependencias |
| Lombok | 1.18.x | Reducción de código boilerplate |

---

## 📁 Estructura del proyecto

```
PanaderiaRodrigo/
│
├── src/
│   ├── main/
│   │   ├── java/com/panaderia/rodrigo/
│   │   │   ├── PanaderiaRodrigoApplication.java   ← Clase principal
│   │   │   │
│   │   │   ├── model/                             ← Entidades JPA
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Cliente.java
│   │   │   │   └── Pedido.java
│   │   │   │
│   │   │   ├── repository/                        ← Repositorios JPA
│   │   │   │   ├── ProductoRepository.java
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   └── PedidoRepository.java
│   │   │   │
│   │   │   ├── service/                           ← Lógica de negocio
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── ClienteService.java
│   │   │   │   └── PedidoService.java
│   │   │   │
│   │   │   ├── controller/                        ← Controladores MVC
│   │   │   │   ├── ProductoController.java
│   │   │   │   ├── ClienteController.java
│   │   │   │   └── PedidoController.java
│   │   │   │
│   │   │   ├── rest/                              ← Controladores REST
│   │   │   │   ├── ProductoRestController.java
│   │   │   │   ├── ClienteRestController.java
│   │   │   │   └── PedidoRestController.java
│   │   │   │
│   │   │   ├── config/                            ← Configuración
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   └── loader/
│   │   │       └── DataLoader.java                ← Datos de prueba
│   │   │
│   │   └── resources/
│   │       ├── templates/                         ← Vistas Thymeleaf
│   │       │   ├── layout/
│   │       │   │   └── base.html
│   │       │   ├── productos/
│   │       │   │   ├── lista.html
│   │       │   │   └── formulario.html
│   │       │   ├── clientes/
│   │       │   │   ├── lista.html
│   │       │   │   └── formulario.html
│   │       │   ├── pedidos/
│   │       │   │   └── lista.html
│   │       │   ├── login.html
│   │       │   └── index.html
│   │       │
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── panaderia.css              ← Estilos personalizados
│   │       │   └── img/
│   │       │       └── logo.png
│   │       │
│   │       └── application.properties             ← Configuración de la app
│   │
│   └── test/
│       └── java/com/panaderia/rodrigo/
│           └── PanaderiaRodrigoApplicationTests.java
│
├── pom.xml                                        ← Dependencias Maven
└── README.md
```

---

## 🚀 Instalación y ejecución

### Prerrequisitos

- Java 17 o superior instalado
- Maven 3.9 o superior instalado
- Git instalado

### Pasos

**1. Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/panaderia-rodrigo.git
cd panaderia-rodrigo
```

**2. Compilar el proyecto con Maven**
```bash
mvn clean install
```

**3. Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

**4. Abrir en el navegador**
```
http://localhost:8080
```

**5. (Opcional) Acceder a la consola H2**
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:panaderiadb
Usuario: sa
Contraseña: (dejar vacío)
```

### Comandos Maven del ciclo de vida

```bash
mvn clean          # Limpia archivos compilados
mvn compile        # Compila el código fuente
mvn test           # Ejecuta los tests
mvn package        # Genera el archivo .jar en /target
mvn install        # Instala el artefacto en repositorio local
```

---

## 🔐 Credenciales de acceso

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `rodrigo` | `admin123` | ADMIN | Acceso total: productos, clientes, pedidos |
| `empleado` | `emp123` | EMPLEADO | Ver productos, gestionar pedidos |

> **Rutas protegidas:**
> - `/productos/nuevo` — Solo ADMIN
> - `/clientes/**` — ADMIN y EMPLEADO
> - `/` y `/productos` — Público

---

## 🌐 Endpoints REST

Base URL: `http://localhost:8080/api`

### Productos

| Método | Endpoint | Descripción | Rol requerido |
|---|---|---|---|
| GET | `/api/productos` | Lista todos los productos | Público |
| GET | `/api/productos/{id}` | Obtiene un producto por ID | Público |
| POST | `/api/productos` | Crea un nuevo producto | ADMIN |
| PUT | `/api/productos/{id}` | Actualiza un producto | ADMIN |
| DELETE | `/api/productos/{id}` | Elimina un producto | ADMIN |

### Clientes

| Método | Endpoint | Descripción | Rol requerido |
|---|---|---|---|
| GET | `/api/clientes` | Lista todos los clientes | ADMIN, EMPLEADO |
| GET | `/api/clientes/{id}` | Obtiene un cliente por ID | ADMIN, EMPLEADO |
| POST | `/api/clientes` | Crea un nuevo cliente | ADMIN |
| DELETE | `/api/clientes/{id}` | Elimina un cliente | ADMIN |

### Ejemplo de request — Crear producto (POST)

```json
POST /api/productos
Content-Type: application/json

{
  "nombre": "Medialunas",
  "categoria": "Factura",
  "precio": 350.00,
  "stock": 50
}
```

### Ejemplo de respuesta exitosa

```json
HTTP 201 Created

{
  "id": 1,
  "nombre": "Medialunas",
  "categoria": "Factura",
  "precio": 350.00,
  "stock": 50
}
```

---

## 📚 Lecciones del módulo

El proyecto fue desarrollado de forma progresiva en 5 etapas:

### Lección 1 — Gestor de proyectos Maven
- ✅ Proyecto creado desde [start.spring.io](https://start.spring.io)
- ✅ `pom.xml` configurado con todas las dependencias
- ✅ Ciclo de vida Maven verificado (`clean`, `install`, `package`)
- ✅ Proyecto subido a GitHub

### Lección 2 — Spring MVC
- ✅ Entidades `Producto`, `Cliente` y `Pedido` creadas
- ✅ Controladores con `@Controller`, `@GetMapping`, `@PostMapping`
- ✅ Vistas Thymeleaf con formularios y listados
- ✅ Navegación básica entre pantallas

### Lección 3 — Acceso a Datos con JPA
- ✅ Repositorios `ProductoRepository`, `ClienteRepository` extendiendo `JpaRepository`
- ✅ Base de datos H2 embebida configurada
- ✅ Capa `@Service` con lógica de negocio
- ✅ Datos de prueba cargados con `DataLoader`

### Lección 4 — Spring Security
- ✅ Dependencia de Spring Security integrada
- ✅ Roles ADMIN y EMPLEADO configurados
- ✅ Rutas protegidas según rol con `@PreAuthorize`
- ✅ Formulario de login y logout funcional
- ✅ Vista de carga de productos protegida (solo ADMIN)

### Lección 5 — API REST
- ✅ `@RestController` para productos y clientes
- ✅ Operaciones CRUD completas (GET, POST, PUT, DELETE)
- ✅ Respuestas en formato JSON
- ✅ Endpoints validados con Postman

---

## 📸 Capturas de pantalla

> *(Agregar capturas luego de ejecutar la aplicación)*

| Pantalla | Descripción |
|---|---|
| `screenshot-login.png` | Formulario de inicio de sesión |
| `screenshot-productos.png` | Listado de productos de la panadería |
| `screenshot-nuevo-producto.png` | Formulario para agregar producto (ADMIN) |
| `screenshot-clientes.png` | Gestión de clientes |
| `screenshot-api-postman.png` | Prueba de endpoints REST en Postman |

---

## 👤 Autor

**Tu nombre aquí, solo para estudiantes, recuerden que siempre puede mejorar el proyecto**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- LinkedIn: [tu-perfil](https://linkedin.com/in/tu-perfil)

---

## 📄 Licencia

Este proyecto fue desarrollado con fines educativos para el Módulo 6 del bootcamp de programación de **Fullstack Java Trainne 2026**.

---

*🍞 Panadería Rodrigo — Hecho con Spring Boot y mucho café ☕*