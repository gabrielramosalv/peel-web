# Changelog

## 1.1.2

- Build do JitPack com Maven 3.9.9: o Maven padrão do JitPack é antigo demais para o failsafe 3.5.2.

## 1.1.1

- Java 21: cada requisição roda numa virtual thread. `maxThreads` passa a ser o limite de requisições simultâneas, e 0 (o padrão) é sem limite.
- Controllers com o mesmo prefixo de mapeamento dividem o contexto do servidor; método que não casa responde `405`.
- Publicação pelo JitPack; a demonstração virou `examples/hello` e não é publicada.

## 1.1.0

- Cabeçalhos de requisição (`Request.getHeader`) e de resposta (`Response.header`), com `ETag` e `304 Not Modified`.
- Respostas de erro com corpo `{"error": "..."}` em `Responses` (`badRequest`, `notFound`, `gone`, `error`), incluindo `410 Gone` e `413 Payload Too Large`.
- `ServerOptions` no `PeelAppBuilder`: `host` (padrão `127.0.0.1`), `allowedOrigin` (sem CORS por padrão), `maxThreads` e `maxBodyBytes`.
- `405 Method Not Allowed` quando o caminho existe com outro método; parâmetros de consulta decodificados.
- Conversor JSON com records, enums pelo nome, `Optional`, datas, escape conforme a RFC 8259 e sem campos estáticos ou transientes.
