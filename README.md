# 🍎 peel-web

É um framework web leve e minimalista feito para Java, ideal para aplicações embarcadas ou com recursos limitados.

Fornece uma camada portátil e expressiva para a criação de aplicações web e servidores HTTP rápidos, sem depender de grandes servidores ou containers.

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
