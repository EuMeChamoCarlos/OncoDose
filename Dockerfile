# ========================================================
# Etapa 1: Compilação (Build)
# Dependências antes do código: mudar só o src reaproveita o cache
# (o OR-Tools é grande e não é baixado de novo a cada build).
# ========================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ========================================================
# Etapa 2: Execução
# Ubuntu (glibc), não Alpine: as bibliotecas nativas do OR-Tools
# não carregam na musl do Alpine e a otimização daria 500.
# ========================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home oncodose
COPY --from=build /app/target/*.jar app.jar
USER oncodose
# Datas e horários (confirmadaEm, "hoje") no fuso do Brasil, não em UTC.
ENV TZ=America/Sao_Paulo
EXPOSE 8080
# Heap proporcional ao limite do container: sobra memória fora do heap para o OR-Tools.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
