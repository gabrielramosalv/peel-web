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

## Uso como dependência

O `peel-web-core` é publicado pelo [JitPack](https://jitpack.io) a cada tag `vX.Y.Z`. Requer Java 21.

Gradle:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.gabrielramosalv.peel-web:peel-web-core:v1.1.1'
}
```

Maven:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.gabrielramosalv.peel-web</groupId>
    <artifactId>peel-web-core</artifactId>
    <version>v1.1.1</version>
</dependency>
```

## Threads

Cada requisição roda numa virtual thread. Uma requisição que espera (long polling, streaming) não prende thread do sistema. `maxThreads(n)` limita a `n` as requisições atendidas ao mesmo tempo; o padrão, 0, não limita.

## Estrutura

```text
peel-web
├── peel-web-core        # a biblioteca; único artefato publicado
├── examples/hello       # aplicação de exemplo; não é publicada
├── .github/workflows    # ci.yml: mvn verify em push na main e em pull request
├── jitpack.yml          # build do JitPack (Java 21, só o core)
└── CHANGELOG.md
```

## Build

```bash
mvn clean verify                      # testes unitários e de integração
mvn clean verify -DskipUnitTests=true # só os de integração
```

## Exemplo

```bash
mvn clean package
java -jar examples/hello/target/peel-web-demo.jar
PORT=9090 java -jar examples/hello/target/peel-web-demo.jar
```

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/peel/demo/hello` | Resposta JSON de exemplo |
| GET | `/peel/demo/static` | Página estática empacotada |

## Publicar uma versão

1. Suba a versão nos `pom.xml` e anote no `CHANGELOG.md`.
2. Crie a tag `vX.Y.Z` e envie (`git push origin vX.Y.Z`).
3. O JitPack constrói na primeira vez que alguém pede a versão; o log fica em `https://jitpack.io/#gabrielramosalv/peel-web`.

## Licença

Este projeto é distribuído sob a licença presente em `LICENSE`.
