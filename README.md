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

---

## 1. Model Controller

Handles the ingestion of API models. An API model is uniquely defined by its endpoint and method.

### Example: Store Models

**Request** (`POST /api/models`)

```json
{
  "models": [
    {
      "path": "/entity/create",
      "method": "POST",
      "query_params": [],
      "headers": [],
      "body": [
        {
          "name": "username",
          "types": [
            "String"
          ],
          "required": true
        },
        {
          "name": "age",
          "types": [
            "Int"
          ],
          "required": false
        }
      ]
    }
  ]
}
```

**Response** (200 OK)

```json
{
  "status": "success",
  "message": "1 models processed successfully"
}
```

## 2. Detection Controller

Receives data for a single request and decides if it is abnormal. It returns the status, abnormal fields, and the reason
for abnormality.

### Example: Valid Traffic

**Request** (`POST /api/detection/validate`)

```json
{
  "path": "/entity/create",
  "method": "POST",
  "body": {
    "username": "Mike",
    "age": "twenty"
  }
}
```

**Response** (200 OK)

```json
[
  {
    "type": "TYPE_MISMATCH_BODY",
    "description": "BODY parameter 'age' has value 'twenty' which does not match any allowed types: [Int]"
  }
]
```