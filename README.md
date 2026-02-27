# Distributive Streams

A lightweight distributed execution system where:
- the **Client** uploads a Python script,
- the **Server** runs it in an isolated temporary file,
- and the execution output is returned to the client.

## Why this project

This project is built around a simple idea: use idle hardware that already exists on your network instead of renting cloud machines.

If one system is mostly idle, it can run jobs submitted by another system acting as a client. That lets you distribute processing and model-training tasks across available machines, reducing cost while making better use of local resources.

## What was improved

- Fixed architecture by implementing a true client (the old client duplicated server behavior).
- Added a clear binary protocol with size checks for safer file transfer.
- Added concurrent request handling on the server with a worker thread pool.
- Added process timeout support to prevent hung scripts.
- Added filename sanitization and strict upload size limits.
- Added unit tests for protocol behavior and data validation.

## Requirements

- Java 11+
- Maven 3.8+
- Python runtime available as `python3` (or set `PYTHON_CMD` env var)

## Build

```bash
mvn clean package
```

## Run

### Start server

```bash
java -cp target/distributive-streams-1.1.0.jar Server 4331
```

`4331` is optional; default port is `4331`.

### Run client

```bash
java -cp target/distributive-streams-1.1.0.jar Client localhost path/to/script.py 4331
```

Arguments:
1. `host`
2. `script-path`
3. optional `port` (defaults to `4331`)

## Protocol details

Client request:
1. `UTF` filename
2. `int` script length
3. script bytes

Server response:
1. `boolean` success
2. `int` exit code
3. `UTF` output

## Test

```bash
mvn test
```
