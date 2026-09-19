# ---- Stage 1: Build the React frontend ----
FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: Build the Spring Boot backend and embed the frontend ----
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /backend
COPY backend/pom.xml ./
RUN mvn -B dependency:go-offline
COPY backend/src ./src
COPY --from=frontend-build /frontend/dist /frontend-dist
RUN mkdir -p src/main/resources/static \
    && cp -r /frontend-dist/* src/main/resources/static/ \
    && mvn -B clean package -DskipTests

# ---- Stage 3: Minimal runtime image ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /backend/target/smart-pantry.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
