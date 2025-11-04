#!/bin/bash

echo "Cleaning old builds..."
mvn clean

echo "Packaging application..."
mvn package

echo "Building Docker image..."
docker-compose build --no-cache

echo "Stopping and removing old containers..."
docker-compose down

echo "Starting new containers..."
docker-compose up -d