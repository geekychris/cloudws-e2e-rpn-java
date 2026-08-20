# RPN Scientific Calculator

A REST API service that evaluates scientific calculator expressions written in
[Reverse Polish Notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation) (RPN / postfix notation).

RPN avoids the need for parentheses or operator-precedence rules — operators
are applied to the operands that immediately precede them on a stack, so
expressions like `(3 + 4) * 2` become `3 4 + 2 *`.

## Features

- `POST /calculate` accepts a JSON body with a space-separated RPN expression
  and returns the numeric result or a descriptive error.
- Supported operators:
  - Basic: `+` `-` `*` `/`
  - Power: `^`
  - `sqrt`, `sin`, `cos`, `tan` (radians), `log` (natural log)
- Clear, actionable error messages for:
  - Stack underflow (not enough operands for an operator)
  - Division by zero
  - Unknown operators / non-numeric tokens
  - Malformed expressions (e.g. leftover operands)
- Returns HTTP `400 Bad Request` with `{ "error": "<message>" }` on any parse
  or evaluation failure, and `200 OK` with `{ "result": <number> }` on success.

## Tech Stack

- Java 21
- Spring Boot 3
- Maven

## Build

Requires Java 21 and Maven 3.8+.

```bash
mvn clean package
```

## Run

```bash
mvn spring-boot:run
```

Or, after building, run the packaged jar directly:

```bash
java -jar target/rpn-calculator-1.0.0.jar
```

The service starts on `http://localhost:8080` by default.

## Example Requests

Basic addition:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "3 4 +"}'
# {"result":7.0}
```

Precedence handled naturally via RPN — `(3 + 4) * 2 - 5 / 5`:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "3 4 + 2 * 5 5 / -"}'
# {"result":13.0}
```

Power and square root — `sqrt(2^10)`:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "2 10 ^ sqrt"}'
# {"result":32.0}
```

Trigonometric functions (radians) — `sin(0)`:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "0 sin"}'
# {"result":0.0}
```

Error example — division by zero:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "5 0 /"}'
# HTTP 400
# {"error":"Division by zero"}
```

Error example — stack underflow:

```bash
curl -X POST http://localhost:8080/calculate \
  -H 'Content-Type: application/json' \
  -d '{"expression": "5 +"}'
# HTTP 400
# {"error":"Stack underflow: operator '+' requires 2 operands but only 1 available"}
```
