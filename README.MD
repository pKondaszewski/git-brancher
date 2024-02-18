# Git Brancher
Recruitment task for Atipera.

## Used technologies ⚙️🛠️
- Java 21
- Spring Boot 3
- Gradle
- Docker (Buildpacks)
- Git, Github (Github Actions)
- JUnit, Mockito, WireMock


## How to run ▶️▶️
### Intellij way 🟥🟦
Run main class `java/pl/przemek/gitbrancher/GitbrancherApplication.java`

### Docker way 🐋🐋
Run `./gradlew bootBuildImage` command

Run `docker run -d -p 8080:8080 --name gitbrancher gitbrancher:0.0.1-SNAPSHOT` command

## How to use 🕹️🎮
Enter swagger `http://localhost:8080/swagger-ui/index.html#/` and use prepared endpoint

or

Execute direct request e.g. `http://localhost:8080/api/github/users/repos-without-forks?username={username}`
where `{username}` is the github username you want to search for.