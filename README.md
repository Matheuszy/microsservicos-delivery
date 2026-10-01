# 🚚 Java Microservices — Delivery & Tracking

Projeto desenvolvido para estudo prático de **arquitetura de microsserviços**, **comunicação assíncrona** e **mensageria com RabbitMQ**, utilizando Java e Spring Boot.

O projeto é composto por dois microsserviços independentes:

* **Delivery Service** — responsável pelo cadastro e gerenciamento das entregas.
* **Tracking Service** — responsável pelo recebimento dos eventos e gerenciamento do rastreamento.

A comunicação entre os serviços é feita de forma **assíncrona através do RabbitMQ**.

---

## 🏗️ Arquitetura

```text
                    HTTP / REST
                        │
                        ▼
              ┌───────────────────┐
              │  Delivery Service │
              │     Spring Boot   │
              └─────────┬─────────┘
                        │
                        │ DeliveryEvent
                        ▼
                 ┌──────────────┐
                 │   RabbitMQ   │
                 │    Queue     │
                 └───────┬──────┘
                         │
                         │ DeliveryEvent
                         ▼
              ┌───────────────────┐
              │  Tracking Service │
              │     Spring Boot   │
              └───────────────────┘
```

O `Delivery Service` não chama diretamente o `Tracking Service`.

Em vez disso, ele publica um evento no RabbitMQ. O `Tracking Service` consome esse evento quando estiver disponível.

Isso cria um **desacoplamento temporal** entre os serviços.

Por exemplo, se o Tracking Service estiver temporariamente desligado, as mensagens podem permanecer na fila e serem processadas quando o consumidor voltar a ficar disponível.

---

## 📦 Microsserviços

### Delivery Service

Responsável por:

* Criar entregas.
* Persistir os dados das entregas.
* Disponibilizar consultas das entregas.
* Publicar eventos de criação de entrega no RabbitMQ.

Exemplo de informações de uma entrega:

```text
codigoPedido
pedido
valor
tipoProduto
enderecoDestino
transporte
```

### Tracking Service

Responsável por:

* Consumir eventos publicados pelo Delivery Service.
* Receber informações necessárias sobre uma nova entrega.
* Posteriormente manter o estado de rastreamento da entrega.

O Tracking possui seu próprio domínio e não depende diretamente das entidades JPA do Delivery Service.

---

## 📨 Comunicação assíncrona

O evento utilizado na comunicação entre os serviços é o `DeliveryEvent`.

O Delivery Service não envia necessariamente todos os dados da entidade `Entrega`.

Ele cria um evento contendo apenas as informações necessárias para o Tracking:

```text
DeliveryEvent
├── codigoPedido
├── pedido
├── enderecoDestino
└── transporte
```

Fluxo:

```text
Entrega
   │
   ▼
DeliveryEvent
   │
   ▼
JSON
   │
   ▼
RabbitMQ
   │
   ▼
DeliveryCreatEvent
   │
   ▼
Tracking Service
```

Os dois microsserviços possuem suas próprias classes para representar o evento. Eles não compartilham diretamente classes Java.

O que existe entre eles é um **contrato de mensagem**.

---

## 🐇 RabbitMQ

O RabbitMQ atua como **Message Broker** entre os microsserviços.

O Delivery Service funciona como **Producer**:

```text
Delivery Service
      │
      │ publish
      ▼
   RabbitMQ
```

O Tracking Service funciona como **Consumer**:

```text
RabbitMQ
   │
   │ consume
   ▼
Tracking Service
```

A comunicação é assíncrona, portanto o produtor não precisa esperar o consumidor processar a mensagem imediatamente.

### Exemplo

Se o Tracking Service estiver desligado:

```text
Delivery Service
      │
      ├── DeliveryEvent ──► RabbitMQ
      ├── DeliveryEvent ──► RabbitMQ
      └── DeliveryEvent ──► RabbitMQ
                                │
                                │ mensagens aguardando
                                ▼
                         Tracking Service
                              OFF
```

Quando o Tracking Service voltar:

```text
RabbitMQ
   │
   ├──► DeliveryEvent
   ├──► DeliveryEvent
   └──► DeliveryEvent
          │
          ▼
   Tracking Service
```

---

## 🗄️ Persistência

A ideia da arquitetura é que cada microsserviço seja responsável pelos seus próprios dados.

```text
Delivery Service             Tracking Service
      │                            │
      ▼                            ▼
Delivery Database           Tracking Database
```

O Tracking Service **não acessa diretamente o banco do Delivery Service**.

Quando precisar conhecer uma informação do Delivery, ela pode ser recebida através de eventos ou de uma API, dependendo da necessidade.

Essa separação evita um forte acoplamento entre os bancos dos serviços.

---

## 🔄 Fluxo atual

Quando uma nova entrega é criada:

```text
1. Cliente
      │
      ▼
2. POST /deliveries/save
      │
      ▼
3. Delivery Service
      │
      ├── Salva Entrega
      │
      └── Cria DeliveryEvent
                │
                ▼
4. RabbitMQ
                │
                ▼
5. Tracking Service
                │
                └── Recebe DeliveryEvent
```

---

## 🛠️ Tecnologias

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring AMQP
* Maven

### Mensageria

* RabbitMQ
* JSON
* Jackson

### Banco de dados

* PostgreSQL

### Desenvolvimento

* IntelliJ IDEA
* Postman
* Git / GitHub

---

## 📚 Conceitos praticados

Este projeto foi desenvolvido principalmente para estudar:

* Microsserviços
* Comunicação assíncrona
* Message Broker
* Producer / Consumer
* RabbitMQ
* Eventos
* Event-driven communication
* JSON message conversion
* Desacoplamento entre serviços
* Separação de domínios
* Persistência independente por serviço
* DTOs
* Eventos de integração
* Spring AMQP
* Jackson Message Converter

---

## 🚀 Próximos passos

O projeto pode evoluir gradualmente para incluir:

* [ ] Persistência do Tracking Service
* [ ] Status da entrega
* [ ] Histórico de rastreamento
* [ ] Endpoint para consulta do tracking
* [ ] Estados da entrega (`CREATED`, `IN_TRANSIT`, `DELIVERED`, etc.)
* [ ] Retry de mensagens
* [ ] Dead Letter Queue (DLQ)
* [ ] Idempotência dos consumidores
* [ ] Docker / Docker Compose
* [ ] Observabilidade
* [ ] Testes de integração
* [ ] Segundo consumidor de eventos
* [ ] Serviço de Analytics em Python
* [ ] Integração com Kafka para estudo de Event Streaming

---

## 🎯 Objetivo do projeto

O objetivo principal não é criar um sistema completo de logística, mas utilizar um domínio simples para estudar, na prática, como microsserviços podem ser estruturados e como podem se comunicar de forma **síncrona ou assíncrona**.

A arquitetura poderá evoluir posteriormente para explorar conceitos mais avançados de **sistemas distribuídos, mensageria, event-driven architecture e processamento de dados**.
