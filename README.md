# 🚀 Backend Engineering Home Assignment

This repository contains my solutions for the backend engineering home assignment. The project focuses on writing clean, thread-safe infrastructure code and designing scalable distributed systems.

## 🛠️ Tech Stack
* **Language:** Java 11
* **Build Tool:** Maven
* **Testing:** JUnit 5

---

# 🚀 Backend Engineering Home Assignment

This repository contains my solutions for the backend engineering home assignment. The project focuses on writing clean, thread-safe infrastructure code and designing scalable distributed systems.

## 🛠️ Tech Stack
* **Language:** Java
* **Testing:** JUnit 5 (Comprehensive coverage)
* **Key Concepts:** Concurrency, LRU Caching (TTL + Eviction), System Design (Event-Driven Architecture), Parsing.

---

## 📂 Project Structure

The project is organized into separate packages for each question, with dedicated directories for source code and tests.
Note that Q5 primarily consists of design assets rather than code.
In addition, I've included example JSON files to illustrate the event formats 
discussed in Q5. This is the link for the diagram: [Architecture Diagram](src/main/java/org/assignment/q5/q5.png).
Diagram available also online at:
[Online Diagram Link](https://lucid.app/lucidchart/50adb46a-8de1-4ed6-9474-a3ede7b1b732/edit?viewport_loc=-754%2C-334%2C2517%2C1405%2C0_0&invitationId=inv_5738cd11-6999-4ef6-8b0b-6224efe2e8f7)
```text
src/
├── main/
│   ├── java/
│   │   └── org/
│   │       └── assignment/
│   │           ├── q1/              # Q1: Calculator & logic
│   │           ├── q2/              # Q2: Logic
│   │           ├── q3/              # Q3: Logic
│   │           ├── q4/              # Q4: Generic LRU Cache implementation
│   │           └── q5/              # Q5: System Design Assets
│   │               ├── q5.png       # Architecture Diagram
│   │               ├── create_event_example.json
│   │               ├── delta_update_event_example.json
│   │               ├── delete_event_example.json
│   │               └── cache_invalidate_event_example.json
│   └── resources/
└── test/
    └── java/
        └── org/
            └── assignment/          # Unit Tests for all components
                ├── q1/
                ├── q2/
                ├── q3/
                └── q4/