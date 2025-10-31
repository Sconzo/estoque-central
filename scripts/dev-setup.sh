#!/bin/bash

# Estoque Central - Development Environment Setup Script
# This script validates prerequisites and sets up the development environment

set -e

echo "======================================"
echo "Estoque Central - Dev Environment Setup"
echo "======================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check command existence
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to compare versions
version_ge() {
    printf '%s\n%s' "$2" "$1" | sort -V -C
}

echo "Step 1: Checking prerequisites..."
echo "-----------------------------------"

# Check Java 21
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 21 ]; then
        echo -e "${GREEN}✓${NC} Java $JAVA_VERSION found"
    else
        echo -e "${RED}✗${NC} Java 21 or higher required. Found: Java $JAVA_VERSION"
        exit 1
    fi
else
    echo -e "${RED}✗${NC} Java not found. Please install Java 21 LTS"
    exit 1
fi

# Check Maven
if command_exists mvn; then
    MVN_VERSION=$(mvn -version | grep "Apache Maven" | awk '{print $3}')
    echo -e "${GREEN}✓${NC} Maven $MVN_VERSION found"
else
    echo -e "${RED}✗${NC} Maven not found. Please install Maven 3.9+"
    exit 1
fi

# Check Node.js
if command_exists node; then
    NODE_VERSION=$(node -v | sed 's/v//')
    NODE_MAJOR=$(echo $NODE_VERSION | cut -d'.' -f1)
    if [ "$NODE_MAJOR" -ge 22 ]; then
        echo -e "${GREEN}✓${NC} Node.js $NODE_VERSION found"
    else
        echo -e "${YELLOW}⚠${NC} Node.js $NODE_VERSION found. Recommended: v22 LTS or v24 LTS"
    fi
else
    echo -e "${RED}✗${NC} Node.js not found. Please install Node.js 22 LTS or 24 LTS"
    exit 1
fi

# Check npm
if command_exists npm; then
    NPM_VERSION=$(npm -v)
    echo -e "${GREEN}✓${NC} npm $NPM_VERSION found"
else
    echo -e "${RED}✗${NC} npm not found. Please install npm 10+"
    exit 1
fi

echo ""
echo "Step 2: Installing backend dependencies..."
echo "-------------------------------------------"
cd backend
mvn clean install -DskipTests
cd ..

echo ""
echo "Step 3: Installing frontend dependencies..."
echo "--------------------------------------------"
cd frontend
npm install
cd ..

echo ""
echo "Step 4: Creating .env.template file..."
echo "---------------------------------------"
cat > .env.template << 'EOF'
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/estoque_central
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres

# Redis Configuration
REDIS_URL=redis://localhost:6379

# Google OAuth 2.0
GOOGLE_OAUTH_CLIENT_ID=your-client-id-here
GOOGLE_OAUTH_CLIENT_SECRET=your-client-secret-here

# Focus NFe API (Fiscal integration)
FOCUS_NFE_API_KEY=your-api-key-here
FOCUS_NFE_ENVIRONMENT=homologacao

# Mercado Livre API
ML_CLIENT_ID=your-ml-client-id-here
ML_CLIENT_SECRET=your-ml-client-secret-here
EOF

echo -e "${GREEN}✓${NC} Created .env.template"
echo ""

echo "======================================"
echo "✅ Development environment setup complete!"
echo "======================================"
echo ""
echo "Next steps:"
echo "1. Copy .env.template to .env and configure your environment variables:"
echo "   ${YELLOW}cp .env.template .env${NC}"
echo ""
echo "2. Start PostgreSQL and Redis services (via Docker or local installation)"
echo ""
echo "3. Build the complete project (backend + frontend):"
echo "   ${YELLOW}cd backend && mvn clean install${NC}"
echo ""
echo "4. Run the application:"
echo "   ${YELLOW}cd backend && mvn spring-boot:run${NC}"
echo ""
echo "5. Access the application:"
echo "   - Frontend: http://localhost:8080"
echo "   - API Docs: http://localhost:8080/swagger-ui.html"
echo "   - Actuator: http://localhost:8080/actuator/health"
echo ""
echo "For more information, see README.md"
echo ""
