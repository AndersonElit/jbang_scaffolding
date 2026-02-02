import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "MavenHexagonalScaffold", mixinStandardHelpOptions = true, version = "1.0",
        description = "Genera un proyecto base Spring Boot Reactivo multimódulo.")
public class MavenHexagonalScaffold implements Runnable {

    @Parameters(index = "0", description = "Nombre del microservicio", defaultValue = "mi-microservicio")
    private String projectName;

    public static void main(String... args) {
        int exitCode = new CommandLine(new MavenHexagonalScaffold()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            String safeProjectName = projectName.replace("-", "");
            Path rootPath = Paths.get(projectName);
            System.out.println("[INFO] Creando proyecto: " + projectName);

            List<String> modules = List.of(
                    "domain/model",
                    "application/use-cases",
                    "infrastructure/driven-adapters",
                    "infrastructure/entry-points"
            );

            for (String module : modules) {
                String moduleName = module.substring(module.lastIndexOf("/") + 1).replace("-", "");
                String basePackage = "com." + safeProjectName + "." + moduleName;
                String packagePath = "/src/main/java/" + basePackage.replace(".", "/");
                Files.createDirectories(rootPath.resolve(module + packagePath));

                String modulePom = getModulePomTemplate(projectName, safeProjectName, module);
                Files.writeString(rootPath.resolve(module + "/pom.xml"), modulePom);

                // Si es el módulo de entry-points, creamos el controlador
                if (module.equals("infrastructure/entry-points")) {
                    String helloController = """
                package %s;

                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RestController;
                import reactor.core.publisher.Mono;

                @RestController
                public class HelloController {
                    @GetMapping("/hello")
                    public Mono<String> sayHello() {
                        return Mono.just("¡Hola desde el scaffold Hexagonal Reactivo!");
                    }
                }
                """.formatted(basePackage, projectName);
                    Files.writeString(rootPath.resolve(module + packagePath + "/HelloController.java"), helloController);

                    String mainPackage = "com." + safeProjectName;
                    String mainClassPath = "/src/main/java/" + mainPackage.replace(".", "/");
                    Files.createDirectories(rootPath.resolve(module + mainClassPath));

                    // También necesitamos una clase Main para que Spring Boot arranque
                    String mainClass = """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class MainApplication {
                    public static void main(String[] args) {
                        SpringApplication.run(MainApplication.class, args);
                    }
                }
                """.formatted(mainPackage);
                    Files.writeString(rootPath.resolve(module + mainClassPath + "/MainApplication.java"), mainClass);

                    // 1. Crear el directorio de recursos
                    Path resourcesPath = rootPath.resolve(module + "/src/main/resources");
                    Files.createDirectories(resourcesPath);

                    // 2. Definir el contenido del application.properties
                    String propertiesContent = """
                            spring.r2dbc.url=r2dbc:postgresql://localhost:5432/mydb
                            spring.r2dbc.username=postgres
                            spring.r2dbc.password=password
                            spring.sql.init.mode=always
                            server.port=8080
                            """;

                    // 3. Escribir el archivo
                    Files.writeString(resourcesPath.resolve("application.properties"), propertiesContent);
                }
            }

            // 3. Crear .gitignore
            String gitIgnore = """
                               target/
                               !.mvn/wrapper/maven-wrapper.jar
                               *.class
                               *.log
                               *.ctxt
                               .mtj.tmp/
                               *.jar
                               *.war
                               *.ear
                               *.zip
                               *.tar.gz
                               *.rar
                               hs_err_pid*
                               .idea/
                               *.iml
                               .classpath
                               .project
                               .settings/
                               bin/
                               .vscode/
                               """;
            Files.writeString(rootPath.resolve(".gitignore"), gitIgnore);

            String pomContent = getRootPomTemplate(projectName);
            Files.writeString(rootPath.resolve("pom.xml"), pomContent);

            System.out.println("[SUCCESS] Proyecto creado en: " + rootPath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo crear el proyecto: " + e.getMessage());
        }
    }

    private String getRootPomTemplate(String name) {
        return """
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0" 
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
            <modelVersion>4.0.0</modelVersion>
            <groupId>com.%s</groupId>
            <artifactId>%s</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <packaging>pom</packaging>
            <parent>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-parent</artifactId>
                <version>3.4.1</version>
            </parent>
            <properties>
                <java.version>21</java.version>
            </properties>
            <modules>
                <module>domain/model</module>
                <module>application/use-cases</module>
                <module>infrastructure/driven-adapters</module>
                <module>infrastructure/entry-points</module>
            </modules>
            <dependencies>
                <dependency>
                    <groupId>io.projectreactor</groupId>
                    <artifactId>reactor-core</artifactId>
                </dependency>
                <dependency>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <optional>true</optional>
                </dependency>
                <dependency>
                    <groupId>io.projectreactor</groupId>
                    <artifactId>reactor-test</artifactId>
                    <scope>test</scope>
                </dependency>
            </dependencies>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-maven-plugin</artifactId>
                    </plugin>
                </plugins>
            </build>
        </project>
        """.formatted(name.replace("-", ""), name);
    }

    private String getModulePomTemplate(String parentArtifactId, String safeProjectName, String modulePath) {
        String moduleArtifactId = modulePath.replace("/", "-");
        String modulePackageName = moduleArtifactId.substring(moduleArtifactId.lastIndexOf("-") + 1).replace("-", "");
        StringBuilder sb = new StringBuilder();

        sb.append("""
        <?xml version="1.0" encoding="UTF-8"?>
        <project xmlns="http://maven.apache.org/POM/4.0.0">
            <modelVersion>4.0.0</modelVersion>
            <parent>
                <groupId>com.%s</groupId>
                <artifactId>%s</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <relativePath>../../pom.xml</relativePath>
            </parent>
            <groupId>com.%s.%s</groupId>
            <artifactId>%s</artifactId>
            <dependencies>
        """.formatted(safeProjectName, parentArtifactId, safeProjectName, modulePackageName, moduleArtifactId));

        // Inyección de dependencias específicas por módulo
        if (modulePath.equals("infrastructure/driven-adapters")) {
            sb.append("""
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
                </dependency>
                <dependency>
                    <groupId>org.postgresql</groupId>
                    <artifactId>r2dbc-postgresql</artifactId>
                    <scope>runtime</scope>
                </dependency>
            """);
        } else if (modulePath.equals("infrastructure/entry-points")) {
            sb.append("""
                <dependency>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-webflux</artifactId>
                </dependency>
            """);
        }

        sb.append("""
            </dependencies>
        </project>
        """);
        return sb.toString();
    }
}