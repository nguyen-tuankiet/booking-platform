#!/bin/bash

# Build and test script for booking platform Docker setup
set -e

echo "🚀 Starting Booking Platform Docker Setup"
echo "=========================================="

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "📋 Checking prerequisites..."
if ! command_exists docker; then
    echo "❌ Docker is not installed"
    exit 1
fi

if ! command_exists docker-compose; then
    echo "❌ Docker Compose is not installed"
    exit 1
fi

echo "✅ Prerequisites check passed"

# Clean up any existing containers
echo "🧹 Cleaning up existing containers..."
docker-compose down -v --remove-orphans || true

# Build infrastructure services first
echo "🏗️  Building infrastructure services..."
docker-compose up -d mysql mongodb redis zookeeper kafka

# Wait for infrastructure services to be healthy
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 30

# Check infrastructure health
echo "🔍 Checking infrastructure health..."
docker-compose ps

# Build and start Eureka server
echo "🌐 Starting Eureka Server..."
docker-compose up -d eureka-server

# Wait for Eureka to be ready
echo "⏳ Waiting for Eureka Server..."
sleep 30

# Build and start all microservices
echo "🔧 Starting microservices..."
docker-compose up -d auth-service booking-service payment-service notification-service

# Wait for services to register with Eureka
echo "⏳ Waiting for service registration..."
sleep 30

# Start API Gateway last
echo "🚪 Starting API Gateway..."
docker-compose up -d api-gateway

# Final status check
echo "📊 Final status check..."
sleep 20
docker-compose ps

echo ""
echo "🎉 Booking Platform is ready!"
echo "=========================================="
echo "🌐 Eureka Dashboard: http://localhost:8761"
echo "🚪 API Gateway: http://localhost:8080"
echo "🔐 Auth Service: http://localhost:8081"
echo "📋 Booking Service: http://localhost:8082"
echo "💳 Payment Service: http://localhost:8083"
echo "📧 Notification Service: http://localhost:8084"
echo "=========================================="
echo ""
echo "🔍 To view logs: docker-compose logs -f"
echo "🛑 To stop all: docker-compose down"
echo "🧹 To clean all: docker-compose down -v --remove-orphans"