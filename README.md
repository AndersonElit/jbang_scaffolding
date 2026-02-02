# jbang_scaffolding

Scaffold ligero y rápido para generar microservicios Java basados en **Spring Boot**, **Maven** y **Arquitectura Hexagonal**, usando **JBang**.

Este proyecto te permite crear en segundos la estructura base de un microservicio listo para evolucionar, con separación clara de responsabilidades y soporte para diferentes bases de datos.

---

## 🚀 Características

* ⚙️ Generación automática de proyectos Maven
* 🧱 Arquitectura Hexagonal (domain, application, infrastructure)
* ☕ Java + Spring Boot
* 📦 Soporte inicial para **PostgreSQL** y **MongoDB**
* 🧩 Fácil de extender y personalizar

---

## 📋 Requisitos

* Java 17 o superior
* curl
* Linux o macOS (Windows vía WSL)

---

## ⚡ Instalación rápida de JBang (Linux / macOS)

Si no tienes JBang instalado:

```bash
curl -Ls https://sh.jbang.dev | bash -s - app setup
```

Verifica la instalación:

```bash
jbang --version
```

---

## 🏗️ Generar un proyecto

Utiliza el script `MavenHexagonalScaffold.java` para crear la estructura base de tu microservicio.

Reemplaza `{nombre-microservicio}` por el nombre de tu proyecto.

### 🐘 PostgreSQL (por defecto)

```bash
jbang MavenHexagonalScaffold.java --service-name={nombre-microservicio}
```

### 🍃 MongoDB

```bash
jbang MavenHexagonalScaffold.java --service-name={nombre-microservicio} --database=mongo
```

---

## 📁 Estructura generada

La estructura generada sigue una **Arquitectura Hexagonal reactiva**, organizada en módulos Maven anidados:

```text
{nombre-microservicio}
├── pom.xml
├── domain
│   └── model
│       ├── pom.xml
│       └── src/main/java/com/{nombre}/model
├── application
│   └── use-cases
│       ├── pom.xml
│       └── src/main/java/com/{nombre}/usecases
├── infrastructure
│   ├── driven-adapters
│   │   ├── pom.xml
│   │   └── src/main/java/com/{nombre}/drivenadapters
│   └── entry-points
│       ├── pom.xml
│       ├── src/main/java/com/{nombre}
│       │   ├── MainApplication.java
│       │   └── entrypoints/HelloController.java
│       └── src/main/resources
│           └── application.properties
└── .gitignore
```

### 📦 Responsabilidad de cada módulo

* **domain/model**
  Contiene el modelo de dominio y reglas de negocio puras. No depende de frameworks.

* **application/use-cases**
  Define los casos de uso y la orquestación de la lógica de negocio.

* **infrastructure/driven-adapters**
  Implementaciones técnicas como persistencia, clientes externos, mensajería, etc.

* **infrastructure/entry-points**
  Puntos de entrada al sistema (REST controllers, listeners, etc.) y la clase `MainApplication`.

---

## 🧠 Filosofía

Este scaffold prioriza:

* Bajo acoplamiento
* Alta cohesión
* Testabilidad
* Evolución sencilla hacia microservicios o sistemas event-driven

Ideal para proyectos reales, pruebas técnicas o como base para estandarizar desarrollos en equipo.

✨ Happy coding!
