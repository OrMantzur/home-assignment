# API Anomaly Detection Service

This micro-service is a high-performance engine designed to identify "abnormal" HTTP requests by comparing incoming
traffic against pre-learned "normal" API models. The service consists of two main parts: receiving API models and
real-time processing of request data.

## Core Features

* **API Model Ingestion**: Receives and stores structural models (path, method, and parameter schemas) representing "
  normal" traffic.
* **Real-time Traffic Validation**: Determines if a single request is "abnormal" due to type mismatches or missing
  required parameters.
* **Custom Schema Engine**: Implements strict validation for specified types like `Int`, `String`, `Boolean`, `Date`,
  `Email`, `UUID`, and `Auth-Token`.

## 🛠️ Tech Stack

* **Language:** Java 11
* **Build Tool:** Maven
* **Testing:** JUnit 5

In order to run the service with docker, use the following command:

```bash
docker-compose up --build
```