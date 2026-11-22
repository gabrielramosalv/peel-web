# 🍎 peel-web

É um framework web leve e minimalista feito para Java, ideal para aplicações embarcadas ou com recursos limitados.

Fornece uma camada portátil para a criação de aplicações web e servidores HTTP rápidos, sem depender de grandes servidores ou containers.

- ⚡ **Leve rápido**: sem dependência de servidores pesados.
- 📦 **Portável**: roda em qualquer dispositivo com Java, de embarcados a desktops.
- 🔌 **Simplicidade na configuração**: inicialização simples via `main()`.
- 🎯 **Enxuto**: inspirado em frameworks minimalistas como Express.js.

---

### 🧠 Acrônimo – PEEL

> **P**ortable • **E**mbedded • **E**xpress • **L**ayer

- **Portable** – Fácil de portar entre diferentes plataformas.
- **Embedded** – Foco em aplicações para sistemas embarcados.
- **Express** – Minimalista e com desempenho ágil.
- **Layer** – Camada de abstração para comunicação web.

---

### 🚀 Inicialização via `public static void main`

```java
public class Application {
    public static void main(String[] args) {
        PeelApp app = PeelAppBuilder.run(builder -> builder
                .context("/peel")
                .port(8080)
                .staticContentPath("src/main/resources/net/peelweb/static")
                .addController(new MyController())
        );
        app.start();
    }
}
```

### 🚀 Criação de uma `controladora`

```java
@Controller("/test")
public class MyController {

    @Mapping("/process/{orderId}/{productId}")
    public Response process(Request request) {
        // Obter corpo da requisição como objeto customizado.
        MyBody body = request.getBodyAs(MyBody.class);

        // Obter parâmetros de consulta.
        String user = request.getParameter("user");
        Integer count = request.getParameterAsInteger("count");

        // Obter variáveis de caminho de URI.
        String orderId = request.getPathVariable("orderId");
        Integer productId = request.getPathVariableAsInteger("productId");

        // Obter arquivo enviado.
        FileEntry file = request.getFileEntry("file");

        // Construir resposta simples com os dados coletados
        String msg = String.format(
            "User: %s, Count: %d, OrderId: %s, ProductId: %d, Body name: %s, File name: %s",
            user,
            count,
            orderId,
            productId,
            body != null ? body.getName() : "null",
            file != null ? file.getFileName() : "no file"
        );

        return Responses.ok(Arrays.asList(msg));
    }

    @Mapping("/hello-world/static")
    public Response index(Request request) {
        // Mapeando conteúdo estático.
        return Responses.page("index.html");
    }

}
```

---

## Estrutura

```text
peel-web
├── peel-web-core
├── peel-web-demo
├── .github/workflows/ci.yml
├── Dockerfile
├── render.yaml
└── pom.xml
```

- `peel-web-core`: biblioteca reutilizável.
- `peel-web-demo`: aplicação web de demonstração.
- `ci.yml`: pipeline de testes, SonarQube, integração e Docker.
- `render.yaml`: Blueprint de publicação da demonstração.

## Requisitos

- Java 17
- Maven 3.8 ou superior
- Docker

## Build e testes

```bash
mvn clean verify
```

Somente testes unitários:

```bash
mvn clean test
```

Somente testes de integração:

```bash
mvn clean verify -DskipUnitTests=true
```

## Execução local

```bash
mvn clean package
java -jar peel-web-demo/target/peel-web-demo.jar
```

A porta padrão é `8080`. A variável `PORT` permite escolher outra porta.

```bash
PORT=9090 java -jar peel-web-demo/target/peel-web-demo.jar
```

## Endpoints da demonstração

| Método | Endpoint | Resultado |
| --- | --- | --- |
| GET | `/peel/health` | Estado da aplicação |
| GET | `/peel/demo/hello` | Resposta JSON de exemplo |
| GET | `/peel/demo/static` | Página estática empacotada |

## Docker

```bash
docker build -t peel-web-demo .
docker run --rm -p 8080:8080 peel-web-demo
```

O projeto possui uma biblioteca e uma única aplicação executável. Por isso, a imagem da demonstração é suficiente e não há múltiplos serviços de aplicação que exijam Docker Compose.

## CI/CD

O GitHub Actions executa os estágios na seguinte ordem:

1. testes unitários com JUnit 5;
2. análise estática em uma instância SonarQube Community temporária;
3. testes de integração com servidor HTTP real;
4. build e teste da imagem Docker.

A instância SonarQube nasce dentro do próprio runner. O pipeline cria um token temporário durante a execução e não depende de secrets cadastrados no repositório.

## Git Flow

- `main`: releases estáveis.
- `develop`: integração das próximas alterações.
- `feature/*`: novas funcionalidades.
- `bugfix/*`: correções isoladas.
- `release/*`: preparação de versões.

Cada branch de feature, bugfix ou release é incorporada por Pull Request.

## Publicação no Render

O `render.yaml` cria um Web Service Docker a partir da branch `main`. O serviço usa `/peel/health` como health check e recebe a variável `PORT` automaticamente do Render.

## Uso da biblioteca

```java
PeelApp app = PeelAppBuilder.run(builder -> builder
        .context("/api")
        .port(8080)
        .staticContentPath("static")
        .addController(new MyController())
);

app.start();
```

## Licença

Este projeto é distribuído sob a licença presente em `LICENSE`.

## Novidades da 1.1.0

- Cabeçalhos de requisição (`Request.getHeader`) e de resposta (`Response.header`), com `ETag` e `304 Not Modified`.
- Respostas de erro com corpo `{"error": "..."}` em `Responses` (`badRequest`, `notFound`, `gone`, `error`), incluindo `410 Gone` e `413 Payload Too Large`.
- `ServerOptions` no `PeelAppBuilder`: `host` (padrão `127.0.0.1`), `allowedOrigin` (sem CORS por padrão), `maxThreads` e `maxBodyBytes`.
- `405 Method Not Allowed` quando o caminho existe com outro método; parâmetros de consulta decodificados.
- Conversor JSON com records, enums pelo nome, `Optional`, datas, escape conforme a RFC 8259 e sem campos estáticos ou transientes.
