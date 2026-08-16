#!/bin/bash

NEW_PASSWORD="031210"
CONTAINER_NAME="unipds-tests-oracle-1"
IS_NECESSARY_CHANGE_PASS=true

echo "Execute mvn clean package..."
./mvnw clean package -DskipTests
sleep 2
echo "Finished mvn clean package..."

echo "Execute docker-compose up..."
docker-compose up --build -d
sleep 2
echo "Finished docker-compose up..."

echo "Waiting for container $CONTAINER_NAME to start..."
while "$IS_NECESSARY_CHANGE_PASS"; do
  CONTAINER_RUNNING=$(docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" --format '{{.Names}}')
  echo "Oracle Container: $CONTAINER_RUNNING"

  HEALTH_STATUS=$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}no-health-check{{end}}' "$CONTAINER_NAME" 2>/dev/null)
  echo "Health Status: $HEALTH_STATUS"

  if [ -n "$HEALTH_STATUS" ] && [ "$HEALTH_STATUS" = "healthy" ]; then
    ORACLE_PASSWORD=$(docker inspect --format='{{range .Config.Env}}{{if eq (index (split . "=") 0) "ORACLE_PASSWORD"}}{{index (split . "=") 1}}{{end}}{{end}}' $CONTAINER_NAME 2>/dev/null)
    echo "Default Oracle password: $ORACLE_PASSWORD"
    echo "Resetting Oracle password to $NEW_PASSWORD"
    docker exec "$CONTAINER_RUNNING" resetPassword "$NEW_PASSWORD"
    echo "Oracle password reset successfully!"
    IS_NECESSARY_CHANGE_PASS=false
  else
    echo "Oracle Container is not ready yet. Waiting..."
    sleep 5
  fi
done

sleep 10
echo "UniPDS Tests iniciado com sucesso!"
