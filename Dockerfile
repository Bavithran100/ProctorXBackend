# ---- builder stage ----
FROM eclipse-temurin:24-jdk AS builder
WORKDIR /app

# copy gradle wrapper and build files
COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/

# copy source
COPY src/ src/

# make wrapper executable and build (skip tests to speed up)
RUN chmod +x gradlew
RUN ./gradlew build -x test

# ---- runtime stage ----
FROM eclipse-temurin:24-jre
WORKDIR /app

# copy the built jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# let Render/Heroku style use PORT env var if set
ENV PORT 8080
EXPOSE 8080

# start app (using wildcard-built jar)
CMD ["java", "-jar", "/app/app.jar"]