# 🚚 Microsservices Java — Event-Driven Architecture

Projeto desenvolvido para estudo prático de **Microsserviços com Java e Spring Boot**, utilizando comunicação assíncrona através do **RabbitMQ**.

O sistema simula um fluxo de entrega em que a criação de uma entrega dispara eventos para outros microsserviços, responsáveis por atualizar o rastreamento e enviar uma notificação por e-mail.

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
                          │ Email/Tracking Event
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
                    Gmail SMTP
```

Cada microsserviço possui sua própria responsabilidade e seu próprio estado.

---

## 📦 Microsserviços

### Delivery Service

Responsável pelo gerenciamento das entregas.

Principais responsabilidades:

* Criar entregas
* Persistir os dados da entrega
* Gerenciar informações do pedido
* Publicar eventos no RabbitMQ após a criação de uma entrega

**Porta:** `8081`

---

### Tracking Service

Responsável pelo acompanhamento da entrega.

Principais responsabilidades:

* Consumir eventos enviados pelo Delivery Service
* Criar o registro de tracking
* Manter seu próprio estado da entrega
* Atualizar o status para `IN_TRANSIT`
* Publicar um evento para o Email Service

**Porta:** `8082`

---

### Email Service

Responsável pelo envio das notificações por e-mail.

Principais responsabilidades:

* Consumir eventos do RabbitMQ
* Processar os dados da notificação
* Montar a mensagem
* Enviar o e-mail através do Gmail SMTP

**Porta:** `8083`

O Email Service não precisa expor endpoints REST para esse fluxo, pois seu processamento é orientado a eventos.

---

# 🔄 Fluxo da aplicação

O fluxo completo funciona da seguinte maneira:

### 1. Criação da entrega

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

### 2. Publicação do evento

Após a criação da entrega, o Delivery Service publica um evento:

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

### 3. Tracking recebe o evento

O Tracking Service possui um consumer que escuta a fila de processamento.

```text
RabbitMQ
    │
    ▼
TrackingConsumer
```

Ao receber o evento, o serviço cria seu próprio registro de tracking.

Por exemplo:

```text
codigoPedido: 123
status: IN_TRANSIT
```

O Tracking Service **não consulta o banco de dados do Delivery Service**.

Cada microsserviço é responsável pelo seu próprio estado.

---

### 4. Tracking publica um novo evento

Depois de processar a entrega, o Tracking Service publica um evento destinado ao Email Service.

Conceitualmente:

```text
Tracking Service
       │
       │ DeliveryTrackingEvent
       ▼
    RabbitMQ
```

---

### 5. Email Service recebe o evento

O Email Service possui um `EmailConsumer`:

```text
RabbitMQ
    │
    ▼
EmailConsumer
    │
    ▼
EmailService
```

O consumer recebe os dados e delega o envio para o serviço responsável pela lógica de e-mail.

---

### 6. Envio do e-mail

O `EmailService` utiliza:

```text
JavaMailSender
```

para enviar a mensagem através do SMTP do Gmail.

Exemplo de mensagem:

```text
Olá!

Seu pedido está em trânsito.

Em breve ele chegará ao endereço informado.

Atenciosamente,
Codex System
```

---

# 📨 Comunicação assíncrona

O RabbitMQ é utilizado para desacoplar os microsserviços.

Em vez de:

```text
Delivery → HTTP → Tracking → HTTP → Email
```

utilizamos:

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

Cada microsserviço possui seu próprio banco.

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

---

# 🛠️ Tecnologias

## Backend

* Java 21
* Spring Boot 4.1.1
* Spring Web
* Spring Data JPA
* Spring AMQP
* Spring Mail
* Jackson
* PostgreSQL
* RabbitMQ

## Mensageria

* RabbitMQ
* CloudAMQP

## Comunicação de e-mail

* Gmail SMTP
* JavaMailSender

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
│   └── src/
│
└── .env
```

Cada serviço possui seu próprio projeto Spring Boot.

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

A senha utilizada pelo Gmail deve ser uma **App Password**, e não a senha normal da conta Google.

> Nunca coloque credenciais reais no `README.md`, `application.properties` ou GitHub.

---

# ▶️ Executando o projeto

Cada microsserviço deve ser executado separadamente.

### Delivery Service

```bash
cd delivery_service
./mvnw spring-boot:run
```

Porta:

```text
8081
```

### Tracking Service

```bash
cd tracking_service
./mvnw spring-boot:run
```

Porta:

```text
8082
```

### Email Service

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
RabbitMQ
      │
      ▼
Tracking Service
      │
      ▼
Tracking DB
      │
      ▼
RabbitMQ
      │
      ▼
Email Service
      │
      ▼
Gmail
```

O resultado esperado é:

1. Entrega criada.
2. Evento publicado pelo Delivery Service.
3. Tracking Service recebe o evento.
4. Tracking é criado com status `IN_TRANSIT`.
5. Tracking Service publica o evento de notificação.
6. Email Service recebe o evento.
7. JavaMailSender envia o e-mail.
8. Cliente recebe a notificação.

---

# 🎯 Objetivos de aprendizado

Este projeto foi desenvolvido para praticar conceitos fundamentais de sistemas distribuídos:

* Arquitetura de microsserviços
* Separação de responsabilidades
* Comunicação síncrona vs. assíncrona
* Event-driven architecture
* Message brokers
* RabbitMQ
* Producers e Consumers
* Spring AMQP
* JSON serialization/deserialization
* Isolamento de banco por serviço
* Comunicação via eventos
* SMTP
* JavaMailSender
* Configuração através de variáveis de ambiente
* Spring Boot

---

# 📚 Próximos passos

Possíveis evoluções do projeto:

* Implementar mais estados de tracking
* Criar eventos específicos para cada mudança de status
* Adicionar API para consulta do tracking
* Implementar tratamento de mensagens com falha
* Trabalhar com Dead Letter Queues (DLQ)
* Implementar retry de mensagens
* Adicionar Docker e Docker Compose
* Adicionar observabilidade
* Implementar API Gateway
* Adicionar service discovery
* Trabalhar com autenticação entre serviços
* Implementar testes de integração
* Evoluir a arquitetura para um ambiente cloud

---

## 📌 Status

**Projeto funcional.**

O fluxo principal foi implementado e validado:

```text
Delivery → RabbitMQ → Tracking → RabbitMQ → Email → Gmail
```

O projeto serve como laboratório prático para estudo de **Java, Spring Boot, Microsserviços, RabbitMQ e Arquitetura Orientada a Eventos**.
