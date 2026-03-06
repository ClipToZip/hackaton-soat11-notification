![Static Badge](https://img.shields.io/badge/Java-17-blue)
![Static Badge](https://img.shields.io/badge/Spring_Boot-3.3.3-green)
[![Apache 2.0 License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

# 📱 Hackaton FIAP - ClipToZip - Microsserviço de Notificação - Grupo 13

![Logo ClipToZip](/docs/cliptozip.png)

## 📝 Sobre o Projeto

Este repositório contém o código-fonte do microsserviço de **Notificação** do ecossistema **ClipToZip**, desenvolvido pelo Grupo 13 como parte do projeto Hackaton da FIAP.

O objetivo principal deste serviço é enviar e-mails ao usuário com o status do processamento do vídeo.

### Funcionalidades Principais

*   **Consumer SQS**: Escuta a fila de eventos, aguardando por novas notificações.
*   **Envio de e-mail**: Envio de e-mail ao usuário com status do processamento do vídeo.

---

## 🛠️ Tecnologias Utilizadas

O projeto foi construído utilizando as seguintes tecnologias e bibliotecas:

*   **Linguagem**: [Java 17](https://openjdk.org/projects/jdk/17/)
*   **Framework**: [Spring Boot 3.3.3](https://spring.io/projects/spring-boot)
*   **Testes**: [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/)
*   **Cobertura de Código**: [JaCoCo](https://www.eclemma.org/jacoco/)
*   **Containerização**: [Docker](https://www.docker.com/) & Docker Compose

---

## 🧩 Arquitetura da Solução

A aplicação segue os princípios da **Arquitetura Hexagonal (Ports and Adapters)**, promovendo o desacoplamento entre a lógica de negócio e os detalhes de infraestrutura.

### Camadas da Aplicação

1.  **Domain (Núcleo)**: Contém as entidades e regras de negócio da aplicação.
    Define os objetos de domínio como NotificationEvent e EmailMessage, além das interfaces (ports) que representam contratos para envio de e-mails, geração de templates e deduplicação de mensagens.
    Essa camada é independente de frameworks externos.
2.  **Application (Casos de Uso)**: Responsável por orquestrar o fluxo de processamento das notificações.
    *   `NotifyUserService`: Serviço responsável por processar eventos de notificação recebidos, validar os dados, evitar mensagens duplicadas e acionar o envio de e-mail ao usuário.
    *   **Ports**: Interfaces que definem os contratos de entrada e saída da aplicação, permitindo desacoplamento entre a lógica de negócio e as implementações de infraestrutura.
3.  **Adapters (Infraestrutura)**: Implementações concretas das portas definidas na camada de domínio.
    *   **In (Entrada)**:
        *   `NotificationSqsListener`: Responsável por consumir mensagens da fila AWS SQS e convertê-las em eventos de domínio para processamento.
    *   **Out (Saída)**:
        *   `SmtpEmailSenderAdapter`: Responsável pelo envio de e-mails via SMTP.
        *   `SimpleTemplateAdapter`: Responsável pela geração do conteúdo do e-mail com base nos dados do evento.
        *   `CaffeineDedupAdapter`: Responsável por evitar o processamento de mensagens duplicadas utilizando cache em memória.

---

## 🚀 Como Executar

### Pré-requisitos
*   Java 17 instalado
*   Docker e Docker Compose instalados
*   Maven (wrapper incluído no projeto)

### Passo a Passo

1.  **Subir a Infraestrutura Local**:
    Utilize o Docker Compose para subir a infraestrutura local.
    ```bash
    docker-compose up -d
    ```

2.  **Executar a Aplicação**:
    Inicie a aplicação.
    ```bash
    ./mvnw spring-boot:run
    ```

3. Criar a fila SQS (somente se estiver rodando local)
    ```powershell
    docker exec -it localstack awslocal sqs create-queue `
      --queue-name cliptozip-notifications
    ```

4.  Mensagem de teste (`msg.json`)

    ```json
    {
      "titulo": "Teste local",
      "status": "APROVADO",
      "mensagem": "Teste funcionou",
      "emailUsuario": "nome.sobrenome@email.com",
      "nomeUsuario": "Nome"
    }
    ```

5. Enviar mensagem (FORMA CORRETA NO POWERSHELL)

    ```powershell
    $body = Get-Content .\msg.json -Raw `
            | ConvertFrom-Json `
            | ConvertTo-Json -Compress
    
    docker exec -i localstack awslocal sqs send-message `
      --queue-url http://localhost:4566/000000000000/cliptozip-notifications `
      --message-body "$body"
    ```

---

## 🧪 Testes e Qualidade

O projeto possui uma forte cultura de testes automatizados.

*   Utilizamos JUnit 5 e Mockito para validar a lógica de negócio isolada.
*   Cobertura: O projeto mantém uma cobertura de código superior a 80%.
*   Foco: Validação de regras de negócio e mapeamento de dados.

### ⚙️ Como executar os testes
Para rodar a suíte completa de testes unitários e gerar o relatório de cobertura, execute o comando Maven:

```Bash
mvn clean verify
```

Após a execução, o relatório estará disponível em:
- Relatório de Cobertura (JaCoCo): `target/site/jacoco/index.html`

---

## 📡 Endpoints Principais

| Método | Endpoint | Descrição |
|--------|---|---|
| `GET`  | `/actuator/health` | Verifica se o serviço está saudável e pronto para receber requisições. |
| `GET` | `/actuator/info` | Retorna informações básicas da aplicação. |
| `GET` | `/actuator/metrics` | Exibe métricas internas da aplicação para monitoramento. |
| `GET`  | `/actuator/prometheus` | Endpoint para coleta de métricas por ferramentas de monitoramento como Prometheus. |

---

## 👥 Autores - Grupo 13

| Nome | RM |
|---|---|
| **Fabiana Casagrande Costa** | RM362339 |
| **Felipe Costacurta Paruce** | RM364868 |
| **Rafael Fonseca Hermes Azevedo** | RM361445 |
| **Samuel Videira** | RM363405 |
