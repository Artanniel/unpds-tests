#!/bin/bash

# Load environment variables from .env if present
if [ -f .env ]; then
  echo "Loading environment variables from .env..."
  set -a
  source .env
  set +a
fi

if [ -z "$SONAR_TOKEN" ]; then
  echo "WARNING: SONAR_TOKEN is not set in environment or .env file."
fi

echo "Running SonarQube analysis..."
./mvnw sonar:sonar ${SONAR_TOKEN:+-Dsonar.token=$SONAR_TOKEN}
