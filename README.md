# 🚚 Microsservices Java — Event-Driven Architecture

Projeto desenvolvido para estudo prático de **Microsserviços com Java e Spring Boot**, utilizando comunicação assíncrona através do **RabbitMQ** e arquitetura orientada a eventos.

O sistema simula um fluxo de entrega em que a criação de uma entrega dispara eventos para outros microsserviços, responsáveis por atualizar o rastreamento e enviar uma notificação por e-mail.

O projeto começou como um laboratório de estudos e tem como objetivo explorar, de forma incremental, conceitos de **microsserviços, sistemas distribuídos e Event-Driven Architecture**.

---

## 🏗️ Arquitetura

```text
                         REST
                          │
                          ▼
                 ┌─────────────────┐
                 │ Delivery Service │
                 │     :8081        │
                 └────────┬────────┘
                          │
                          │ DeliveryCreatedEvent
                          ▼
                    ┌─────────────┐
                    │  RabbitMQ   │
                    └──────┬──────┘
                           │
                           ▼
                 ┌─────────────────┐
                 │ Tracking Service│
                 │     :8082       │
                 └────────┬────────┘
                          │
                          │ DeliveryTrackingEvent
                          ▼
                    ┌─────────────┐
                    │  RabbitMQ   │
                    └──────┬──────┘
                           │
                           ▼
                 ┌─────────────────┐
                 │  Email Service  │
                 │     :8083       │
                 └────────┬────────┘
                          │
                          ▼
                 ┌──────────────────┐
                 │ Thymeleaf Template│
                 └────────┬─────────┘
                          │
                          ▼
                    JavaMailSender
                          │
                          ▼
                     SMTP / Gmail
```

Cada microsserviço possui sua própria responsabilidade e seu próprio estado.

---

# 📦 Microsserviços

## Delivery Service

Responsável pelo gerenciamento das entregas.

### Principais responsabilidades

* Criar entregas
* Persistir os dados da entrega
* Gerenciar informações do pedido
* Publicar eventos no RabbitMQ após a criação de uma entrega

**Porta:** `8081`

---

## Tracking Service

Responsável pelo acompanhamento da entrega.

### Principais responsabilidades

* Consumir eventos enviados pelo Delivery Service
* Criar o registro de tracking
* Manter seu próprio estado da entrega
* Atualizar o status para `IN_TRANSIT`
* Publicar um evento para o Email Service

**Porta:** `8082`

O Tracking Service mantém seu próprio estado e não acessa diretamente as entidades ou o banco de dados do Delivery Service.

---

## Email Service

Responsável pelo processamento e envio das notificações por e-mail.

### Principais responsabilidades

* Consumir eventos do RabbitMQ
* Processar os dados recebidos
* Renderizar templates HTML
* Enviar e-mails através do SMTP
* Isolar a lógica de envio da apresentação da mensagem

**Porta:** `8083`

O Email Service não precisa expor endpoints REST para esse fluxo, pois seu processamento é orientado a eventos.

---

# 🔄 Fluxo da aplicação

## 1. Criação da entrega

O cliente envia uma requisição REST para o Delivery Service.

```text
Client
  │
  │ POST /entregas
  ▼
Delivery Service
```

A entrega é persistida no banco de dados.

---

## 2. Publicação do evento

Após a criação da entrega, o Delivery Service publica um:

```text
DeliveryCreatedEvent
```

Exemplo conceitual:

```json
{
  "codigoPedido": 123,
  "pedido": "Pedido #123",
  "email": "cliente@gmail.com",
  "enderecoDestino": "São Paulo",
  "transporte": "MOTOCICLETA"
}
```

Esse evento é enviado para o RabbitMQ.

---

## 3. Tracking recebe o evento

O Tracking Service possui um consumer que escuta a fila de processamento.

```text
RabbitMQ
    │
    ▼
TrackingConsumer
```

Ao receber o evento, o serviço cria seu próprio registro de tracking.

Exemplo:

```text
codigoPedido: 123
status: IN_TRANSIT
```

O Tracking Service **não consulta o banco de dados do Delivery Service**.

Cada microsserviço é responsável pelo seu próprio estado.

---

## 4. Tracking publica um novo evento

Depois de processar a entrega, o Tracking Service publica um evento destinado ao Email Service.

```text
Tracking Service
       │
       │ DeliveryTrackingEvent
       ▼
    RabbitMQ
```

O evento atualmente contém:

```java
public record DeliveryTrackingEvent(
        String recipientEmail,
        Integer codigoPedido,
        String status
) {}
```

Esses dados são suficientes para que o Email Service consiga montar a notificação.

---

## 5. Email Service recebe o evento

O Email Service possui um `EmailConsumer` responsável por consumir a mensagem:

```text
RabbitMQ
    │
    ▼
EmailConsumer
    │
    ▼
EmailService
```

O `EmailConsumer` não é responsável por montar o conteúdo do e-mail.

Ele apenas recebe o evento e delega o processamento:

```java
emailService.sendDeliveryNotification(trackingEvent);
```

Essa separação mantém a responsabilidade do consumer simples.

---

# 📧 Templates de e-mail

O conteúdo do e-mail foi separado da lógica Java utilizando **Thymeleaf**.

A estrutura atual inclui:

```text
email_service/
└── src/
    └── main/
        └── resources/
            └── templates/
                └── delivery-in-transit.html
```

O template contém o HTML da mensagem e utiliza variáveis dinâmicas:

```html
<strong th:text="${codigoPedido}"></strong>
```

e:

```html
<span th:text="${status}"></span>
```

O `EmailService` fornece os valores ao template:

```java
context.setVariable("codigoPedido", event.codigoPedido());
context.setVariable("status", event.status());
```

O Thymeleaf então renderiza o HTML final antes do envio.

### Fluxo

```text
DeliveryTrackingEvent
        │
        ▼
   EmailService
        │
        ▼
    Thymeleaf
        │
        ▼
 HTML renderizado
        │
        ▼
   MimeMessage
        │
        ▼
 JavaMailSender
```

Essa abordagem evita deixar o conteúdo da mensagem diretamente hardcoded no código Java.

---

# ✉️ Envio de e-mail

Para mensagens HTML, o Email Service utiliza:

```text
MimeMessage
MimeMessageHelper
JavaMailSender
```

Diferentemente de `SimpleMailMessage`, utilizado anteriormente para texto puro, `MimeMessage` permite enviar conteúdo HTML.

O conteúdo final é enviado através do SMTP configurado para o serviço.

No ambiente de estudos atual, o projeto utiliza **Gmail SMTP**.

Em um ambiente de produção, o mesmo conceito poderia ser utilizado com um provedor transacional como:

* Amazon SES
* SendGrid
* Mailgun
* Resend
* Brevo

O destinatário do e-mail não precisa configurar nada. A configuração SMTP pertence ao serviço que realiza o envio.

---

# ⚠️ Tratamento de exceções

O envio de e-mail pode gerar exceções específicas da API de e-mail, como:

```text
jakarta.mail.MessagingException
```

Em vez de expor diretamente essa exceção para o restante da aplicação, o Email Service pode encapsulá-la em uma exceção própria da aplicação:

```text
EmailSendingException
```

Conceitualmente:

```text
JavaMailSender
      │
      X
      │
MessagingException
      │
      ▼
EmailSendingException
      │
      ▼
Spring AMQP
```

Essa abordagem facilita uma futura implementação de mecanismos como:

* Retry
* Dead Letter Queue
* tratamento de falhas temporárias
* observabilidade de erros

---

# 📨 Comunicação assíncrona

O RabbitMQ é utilizado para desacoplar os microsserviços.

Em vez de:

```text
Delivery → HTTP → Tracking → HTTP → Email
```

o projeto utiliza:

```text
Delivery
    │
    ▼
RabbitMQ
    │
    ▼
Tracking
    │
    ▼
RabbitMQ
    │
    ▼
Email
```

Isso permite que os serviços se comuniquem através de eventos sem que um serviço precise chamar diretamente o outro.

---

# 🗄️ Banco de dados

Cada microsserviço possui seu próprio estado e banco de dados.

```text
Delivery Service
      │
      ▼
Delivery DB


Tracking Service
      │
      ▼
Tracking DB
```

O Tracking Service não possui relacionamento JPA com entidades do Delivery Service.

Essa separação ajuda a manter o **baixo acoplamento** entre os microsserviços.

Os identificadores internos dos bancos não precisam ser iguais entre os serviços.

Um identificador de negócio, como:

```text
PED-4071
```

pode ser utilizado posteriormente para correlacionar informações entre diferentes serviços.

---

# 🛠️ Tecnologias

## Backend

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Spring AMQP
* Spring Mail
* Spring Boot Starter Thymeleaf
* Jackson
* PostgreSQL
* RabbitMQ

## Mensageria

* RabbitMQ
* CloudAMQP

## E-mail

* JavaMailSender
* Jakarta Mail
* Thymeleaf
* SMTP
* Gmail SMTP — ambiente de estudos

## Ferramentas

* IntelliJ IDEA
* Maven
* Git/GitHub

---

# 📁 Estrutura do projeto

```text
microsservices-java/
│
├── delivery_service/
│   └── src/
│
├── tracking_service/
│   └── src/
│
├── email_service/
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/codexsystem/email_service/
│           │       ├── config/
│           │       ├── consumer/
│           │       ├── dto/
│           │       └── service/
│           │
│           └── resources/
│               └── templates/
│                   └── delivery-in-transit.html
│
└── .env
```

Cada serviço possui seu próprio projeto Spring Boot e ciclo de execução independente.

---

# 🔐 Configuração

As credenciais não devem ser versionadas no Git.

Exemplo de variáveis necessárias:

```env
RABBITMQ_ADDRESS=amqps://...
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=...

MAIL_USERNAME=seuemail@gmail.com
MAIL_PASSWORD=sua-app-password
```

No caso do Gmail, `MAIL_PASSWORD` representa uma **App Password**, e não a senha normal da conta Google.

> Nunca coloque credenciais reais no `README.md`, `application.properties`, `.env` versionado ou GitHub.

---

# ▶️ Executando o projeto

Cada microsserviço deve ser executado separadamente.

## Delivery Service

```bash
cd delivery_service
./mvnw spring-boot:run
```

Porta:

```text
8081
```

---

## Tracking Service

```bash
cd tracking_service
./mvnw spring-boot:run
```

Porta:

```text
8082
```

---

## Email Service

```bash
cd email_service
./mvnw spring-boot:run
```

Porta:

```text
8083
```

---

# 🧪 Testando o fluxo

O fluxo pode ser testado criando uma nova entrega através da API do Delivery Service.

Após a criação:

```text
POST Delivery
      │
      ▼
Delivery DB
      │
      ▼
DeliveryCreatedEvent
      │
      ▼
RabbitMQ
      │
      ▼
Tracking Service
      │
      ▼
Tracking DB
      │
      ▼
DeliveryTrackingEvent
      │
      ▼
RabbitMQ
      │
      ▼
Email Service
      │
      ▼
Thymeleaf
      │
      ▼
HTML
      │
      ▼
JavaMailSender
      │
      ▼
SMTP
      │
      ▼
Cliente
```

### Resultado esperado

1. Entrega é criada.
2. Delivery Service persiste a entrega.
3. Delivery Service publica `DeliveryCreatedEvent`.
4. Tracking Service recebe o evento.
5. Tracking é criado/atualizado com status `IN_TRANSIT`.
6. Tracking Service publica `DeliveryTrackingEvent`.
7. Email Service recebe o evento.
8. Email Service carrega o template HTML.
9. Thymeleaf substitui as variáveis do template.
10. `MimeMessage` é criado.
11. `JavaMailSender` envia o e-mail.
12. Cliente recebe a notificação.

---

# 🎯 Objetivos de aprendizado

Este projeto foi desenvolvido para praticar conceitos fundamentais de sistemas distribuídos:

* Arquitetura de microsserviços
* Separação de responsabilidades
* Comunicação síncrona vs. assíncrona
* Event-Driven Architecture
* Message Brokers
* RabbitMQ
* Producers e Consumers
* Spring AMQP
* JSON serialization/deserialization
* Isolamento de banco por serviço
* Identificadores de negócio
* Comunicação baseada em eventos
* SMTP
* JavaMailSender
* MIME e envio de HTML
* Thymeleaf
* Templates dinâmicos
* Tratamento de exceções
* Configuração através de variáveis de ambiente
* Spring Boot

---

# 📚 Possíveis evoluções

O projeto foi desenvolvido de forma incremental e pode evoluir para um sistema de tracking mais completo.

Possíveis próximos passos:

* Implementar mais estados de tracking
* Criar eventos específicos para cada mudança de status
* Utilizar identificadores de negócio para correlação entre serviços
* Adicionar API para consulta do tracking
* Implementar Retry
* Implementar Dead Letter Queues (DLQ)
* Adicionar observabilidade
* Implementar métricas e tracing distribuído
* Adicionar Docker e Docker Compose
* Implementar API Gateway
* Avaliar Service Discovery
* Trabalhar com autenticação entre serviços
* Implementar testes de integração
* Criar Read Models para consultas e relatórios
* Explorar CQRS
* Evoluir a arquitetura para um ambiente cloud
* Utilizar Python para processamento de dados e analytics
* Explorar recursos de IA para análise das entregas

---

# 📌 Status

**Projeto funcional e validado.**

O fluxo principal foi implementado e testado de ponta a ponta:

```text
Delivery
    ↓
RabbitMQ
    ↓
Tracking
    ↓
RabbitMQ
    ↓
Email
    ↓
Thymeleaf
    ↓
JavaMailSender
    ↓
SMTP
    ↓
Cliente
```

O projeto atualmente funciona como um **laboratório prático para estudo de Java, Spring Boot, Microsserviços, RabbitMQ e Arquitetura Orientada a Eventos**.

A ideia é continuar evoluindo o projeto gradualmente, utilizando os novos requisitos como oportunidade para estudar conceitos de **sistemas distribuídos, arquitetura de software, observabilidade, processamento de dados e cloud**.
