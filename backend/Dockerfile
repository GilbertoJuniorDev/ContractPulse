# ---- Build Stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar arquivos do Gradle
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle gradle.properties ./

# Dar permissão de execução ao gradlew
RUN chmod +x gradlew

# Baixar dependências (cache layer)
RUN ./gradlew dependencies --no-daemon || true

# Copiar código-fonte
COPY src/ src/

# Build do JAR (sem testes para acelerar o deploy)
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar o JAR gerado
COPY --from=build /app/build/libs/*.jar app.jar

# Expor a porta (Railway atribui via $PORT)
EXPOSE 8080

# Comando de inicialização — usar shell form para expandir $PORT
# -Djava.net.preferIPv4Stack=true força IPv4 (Railway usa IPv6, Supabase só aceita IPv4)
CMD java -jar -Dserver.port=${PORT:-8080} -Djava.net.preferIPv4Stack=true app.jar
