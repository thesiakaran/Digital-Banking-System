@echo off
echo Starting API Gateway (Port 8080)...
start cmd /k "cd api-gateway && mvn spring-boot:run"

echo Starting Account Service (Port 8081)...
start cmd /k "cd account-service && mvn spring-boot:run"

echo Starting Transaction Service (Port 8082)...
start cmd /k "cd transaction-service && mvn spring-boot:run"

echo Starting Fraud Detection Service (Port 8083)...
start cmd /k "cd fraud-service && mvn spring-boot:run"

echo All microservices are starting up in separate windows!
