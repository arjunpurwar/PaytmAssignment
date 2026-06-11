# URL Shortener

A small Spring Boot service that shortens URLs, redirects short codes with HTTP 301, supports custom aliases, and persists mappings in H2 file mode.

## Features

- `POST /shorten` creates a short code for a long URL.
- `GET /{code}` returns a 301 redirect to the original URL.
- Duplicate long URLs are idempotent: the first stored code is returned again.
- Custom aliases are supported.
- Unknown codes return 404.
- Validates input URLs and alias format.

## Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- H2 database (file-backed for local persistence)
- JUnit 5 + MockMvc

## Run locally

```bash
mvn spring-boot:run
```

The service starts at `http://localhost:8080`.

## API

### Create a short URL

`POST /shorten`

Request:

```json
{
  "longUrl": "https://example.com/articles/123",
  "customAlias": "myalias"
}
```

`customAlias` is optional.

Response:

```json
{
  "code": "myalias",
  "shortUrl": "http://localhost:8080/myalias",
  "longUrl": "https://example.com/articles/123",
  "created": true
}
```

### Redirect

`GET /myalias`

Returns `301 Moved Permanently` with a `Location` header.

## Tests

```bash
mvn test
```

