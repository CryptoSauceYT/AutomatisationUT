#!/bin/bash

###############################################################################
# BITUNIX TRADING BOT INSTALLATION SCRIPT - GITHUB VERSION
# 
# Optimized for installation from GitHub
# Code is already present after git clone
#
# Compatible with Ubuntu 24.04+ (including Ubuntu 25.x)
#
# Usage: 
#   git clone https://github.com/CryptoSauceYT/AutomatisationUT.git
#   cd AutomatisationUT
#   bash install_bot_github.sh
###############################################################################

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m'

print_step() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

print_error() { echo -e "${RED}ERROR: $1${NC}"; }
print_success() { echo -e "${GREEN}$1${NC}"; }
print_warning() { echo -e "${YELLOW}$1${NC}"; }
print_info() { echo -e "${MAGENTA}$1${NC}"; }

# Check Ubuntu
if ! grep -q "Ubuntu" /etc/os-release; then
    print_error "This script requires Ubuntu"
    exit 1
fi

# Get Ubuntu version info
UBUNTU_VERSION=$(lsb_release -rs)
UBUNTU_CODENAME=$(lsb_release -cs)
print_info "Detected Ubuntu $UBUNTU_VERSION ($UBUNTU_CODENAME)"

clear
cat << "EOF"
+===============================================================+
|                                                               |
|         BITUNIX TRADING BOT INSTALLATION                      |
|                                                               |
|              Installation from GitHub                         |
|                                                               |
+===============================================================+
EOF
echo ""

# ============================================================================
# SOURCE CODE VERIFICATION
# ============================================================================
print_step "SOURCE CODE VERIFICATION"

if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found!"
    print_error "Make sure you are in the correct directory"
    print_error "You must run this script from the cloned folder"
    echo ""
    echo "Correct commands:"
    echo "  git clone https://github.com/CryptoSauceYT/AutomatisationUT.git"
    echo "  cd AutomatisationUT"
    echo "  bash install_bot_github.sh"
    exit 1
fi

print_success "Source code detected (pom.xml found)"

# Detect working directory
CURRENT_DIR=$(pwd)
print_info "Working directory: $CURRENT_DIR"

# ============================================================================
# 0. TIMEZONE CONFIGURATION (UTC)
# ============================================================================
print_step "0. TIMEZONE CONFIGURATION (UTC)"

CURRENT_TZ=$(timedatectl show --property=Timezone --value 2>/dev/null || cat /etc/timezone)
if [ "$CURRENT_TZ" = "UTC" ] || [ "$CURRENT_TZ" = "Etc/UTC" ]; then
    print_success "Timezone already set to UTC"
else
    print_info "Setting timezone to UTC for webhook compatibility..."
    sudo timedatectl set-timezone UTC 2>/dev/null || sudo ln -sf /usr/share/zoneinfo/UTC /etc/localtime
    print_success "Timezone set to UTC"
fi

# Display current time
print_info "Current server time: $(date '+%Y-%m-%d %H:%M:%S %Z')"

# ============================================================================
# 1. SYSTEM UPDATE (Ubuntu 24+ compatible)
# ============================================================================
print_step "1. SYSTEM UPDATE"

# Disable third-party repositories that may not support newer Ubuntu versions
print_info "Checking repository compatibility..."

# Create backup of sources list and disable incompatible repos
if [ -d "/etc/apt/sources.list.d" ]; then
    # Find and disable repos that don't support the current Ubuntu version
    for repo_file in /etc/apt/sources.list.d/*.list; do
        if [ -f "$repo_file" ]; then
            # Check if repo contains unsupported codenames or known incompatible sources
            if grep -qE "monarx|questing" "$repo_file" 2>/dev/null; then
                print_warning "Disabling incompatible repository: $(basename $repo_file)"
                sudo mv "$repo_file" "${repo_file}.disabled" 2>/dev/null || true
            fi
        fi
    done
fi

# Update with error tolerance for Ubuntu 25+
print_info "Updating package lists..."
sudo apt-get update 2>&1 | grep -v "does not have a Release file" | grep -v "N: Skipping" || true

print_info "Upgrading system packages..."
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y 2>/dev/null || {
    print_warning "Some packages could not be upgraded (non-critical)"
}

print_success "System updated"

# ============================================================================
# 2. DOCKER INSTALLATION
# ============================================================================
print_step "2. DOCKER INSTALLATION"

if command -v docker &> /dev/null; then
    print_warning "Docker already installed ($(docker --version))"
else
    print_info "Installing Docker..."
    
    # Install prerequisites
    sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common gnupg lsb-release 2>/dev/null
    
    # Add Docker GPG key
    sudo mkdir -p /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg 2>/dev/null
    sudo chmod a+r /etc/apt/keyrings/docker.gpg
    
    # Determine the correct Ubuntu codename for Docker repo
    # For Ubuntu 25+, use the latest supported LTS (noble = 24.04)
    DOCKER_CODENAME=$UBUNTU_CODENAME
    MAJOR_VERSION=$(echo $UBUNTU_VERSION | cut -d. -f1)
    
    if [ "$MAJOR_VERSION" -ge 25 ]; then
        print_warning "Ubuntu $UBUNTU_VERSION detected - using Docker repo for Ubuntu 24.04 (noble)"
        DOCKER_CODENAME="noble"
    fi
    
    # Add Docker repository
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $DOCKER_CODENAME stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    sudo apt-get update 2>/dev/null || true
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin 2>/dev/null
    
    # Add current user to docker group
    sudo usermod -aG docker $USER
    
    print_success "Docker installed: $(docker --version)"
fi

# Ensure Docker service is running
sudo systemctl enable docker 2>/dev/null || true
sudo systemctl start docker 2>/dev/null || true

# ============================================================================
# 3. TOOLS INSTALLATION
# ============================================================================
print_step "3. TOOLS INSTALLATION"
sudo apt-get install -y git nano htop unzip curl jq 2>/dev/null
print_success "Tools installed"

# ============================================================================
# 4. FIREWALL CONFIGURATION
# ============================================================================
print_step "4. FIREWALL CONFIGURATION"
sudo ufw --force enable 2>/dev/null || true
sudo ufw allow OpenSSH 2>/dev/null || true
sudo ufw allow 80/tcp 2>/dev/null || true
sudo ufw allow 443/tcp 2>/dev/null || true
sudo ufw allow 8080/tcp 2>/dev/null || true
print_success "Firewall configured"

# ============================================================================
# 5. DOWNLOAD PRE-COMPILED JAR
# ============================================================================
print_step "5. DOWNLOAD PRE-COMPILED JAR"

mkdir -p target

if [ ! -f "target/trading-bot-1.0.0.jar" ]; then
    print_info "Downloading JAR from GitHub Releases..."
    wget -q --show-progress \
        https://github.com/CryptoSauceYT/AutomatisationUT/releases/download/v1.0.0/trading-bot-1.0.0.jar \
        -O target/trading-bot-1.0.0.jar
    
    if [ -f "target/trading-bot-1.0.0.jar" ]; then
        JAR_SIZE=$(du -h target/trading-bot-1.0.0.jar | cut -f1)
        print_success "JAR downloaded ($JAR_SIZE)"
    else
        print_error "JAR download failed"
        print_error "Check your internet connection and try again"
        exit 1
    fi
else
    print_success "JAR already present"
fi

# ============================================================================
# 6. DOCKER FILES VERIFICATION
# ============================================================================
print_step "6. DOCKER FILES VERIFICATION"

# Create optimized Dockerfile
print_info "Creating optimized Dockerfile..."
cat > Dockerfile << 'DOCKERFILE_END'
FROM eclipse-temurin:11-jdk

WORKDIR /app

# Set timezone to UTC
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/trading-bot-1.0.0.jar trading-bot.jar

RUN mkdir -p /var/logs/trading-bot/ && chmod 777 /var/logs/trading-bot/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./trading-bot.jar"]
DOCKERFILE_END
print_success "Dockerfile created"

# Create/verify docker-compose.yml
if [ ! -f "docker-compose.yml" ]; then
    print_warning "docker-compose.yml missing, creating..."
fi

cat > docker-compose.yml << 'COMPOSE_END'
services:
  trading-bot:
    build: .
    ports:
      - "8080:8080"
    container_name: trading-bot
    volumes:
      - ./config:/config
      - /var/logs/trading-bot:/var/logs/trading-bot
    environment:
        SPRING_CONFIG_LOCATION: file:///config/
        SPRING_PROFILES_ACTIVE: prod
        TZ: UTC
    networks:
      - bot-network
    restart: unless-stopped

  nginx:
    image: nginx:latest
    ports:
      - "443:443"
    volumes:
      - ./config/nginx.conf:/etc/nginx/conf.d/default.conf
      - /etc/nginx/ssl:/etc/nginx/ssl
    environment:
      - TZ=UTC
    depends_on:
      - trading-bot
    networks:
      - bot-network
    restart: unless-stopped

networks:
  bot-network:
    driver: bridge
COMPOSE_END
print_success "docker-compose.yml created"

# ============================================================================
# 7. SSL CONFIGURATION (Self-signed)
# ============================================================================
print_step "7. SSL CONFIGURATION"

sudo mkdir -p /etc/nginx/ssl

print_info "Generating self-signed SSL certificate..."
PUBLIC_IP=$(curl -4 -s ifconfig.me)

sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/selfsigned.key \
    -out /etc/nginx/ssl/selfsigned.crt \
    -subj "/C=CH/ST=Zurich/L=Zurich/O=TradingBot/CN=$PUBLIC_IP" 2>/dev/null

sudo chmod 644 /etc/nginx/ssl/*

SERVER_NAME="$PUBLIC_IP"
SSL_CERT="/etc/nginx/ssl/selfsigned.crt"
SSL_KEY="/etc/nginx/ssl/selfsigned.key"
WEBHOOK_URL="https://$PUBLIC_IP/api/v1/place_limit_order"

print_success "Self-signed certificate created for $PUBLIC_IP"

# Create nginx.conf
mkdir -p config
cat > config/nginx.conf << NGINX_END
server {
    listen 443 ssl;
    server_name $SERVER_NAME;

    ssl_certificate $SSL_CERT;
    ssl_certificate_key $SSL_KEY;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://trading-bot:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location /api/v1/place_limit_order {
        proxy_pass http://trading-bot:8080/api/v1/place_limit_order;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
NGINX_END
print_success "nginx.conf created"

# ============================================================================
# 8. BUILD AND START
# ============================================================================
print_step "8. BUILD AND START BOT"

print_info "Building Docker image (may take 2-3 minutes)..."
docker compose up -d --build

print_info "Waiting for startup (15 seconds)..."
sleep 15

if docker compose ps | grep -q "Up"; then
    print_success "Bot started successfully!"
else
    print_error "Startup problem detected"
    docker compose logs
    exit 1
fi

# ============================================================================
# 9. TESTS
# ============================================================================
print_step "9. FUNCTIONALITY TESTS"

echo "Testing health check..."
sleep 3
if curl -s http://localhost:8080/api/v1/check 2>/dev/null | grep -q "success"; then
    print_success "Health check OK"
else
    print_warning "Health check not available (normal if bot just started)"
fi

# ============================================================================
# 10. UTILITY SCRIPTS
# ============================================================================
print_step "10. CREATING UTILITY SCRIPTS"

cat > "$HOME/monitor_bot.sh" << 'EOF'
#!/bin/bash
echo "=============================================="
echo "       TRADING BOT STATUS"
echo "=============================================="
echo ""
echo "Server Time (UTC): $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
echo ""
echo "Docker Containers:"
cd ~/AutomatisationUT && docker compose ps
echo ""
echo "Resource Usage:"
docker stats --no-stream --no-trunc --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
echo ""
echo "Last 30 log entries:"
cd ~/AutomatisationUT && docker compose logs --tail=30 trading-bot
echo ""
echo "Bot Health:"
curl -s http://localhost:8080/api/v1/check | jq 2>/dev/null || curl -s http://localhost:8080/api/v1/check
EOF
chmod +x "$HOME/monitor_bot.sh"

cat > "$HOME/backup_bot.sh" << 'EOF'
#!/bin/bash
BACKUP_DIR=~/bot-backups
DATE=$(date -u +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
tar -czf $BACKUP_DIR/bot_config_$DATE.tar.gz ~/AutomatisationUT/config/
ls -t $BACKUP_DIR/bot_config_*.tar.gz | tail -n +8 | xargs -r rm
echo "Backup complete: bot_config_$DATE.tar.gz"
EOF
chmod +x "$HOME/backup_bot.sh"

cat > "$HOME/restart_bot.sh" << 'EOF'
#!/bin/bash
echo "Restarting bot..."
cd ~/AutomatisationUT
docker compose down
docker compose up -d --build
echo "Bot restarted"
sleep 5
docker compose logs --tail=20 trading-bot
EOF
chmod +x "$HOME/restart_bot.sh"

print_success "Scripts created:"
echo "  - ~/monitor_bot.sh"
echo "  - ~/backup_bot.sh"
echo "  - ~/restart_bot.sh"

# ============================================================================
# FINAL SUCCESS MESSAGE
# ============================================================================
echo ""
echo ""
echo -e "${GREEN}${BOLD}=================================================================${NC}"
echo -e "${GREEN}${BOLD}                                                                 ${NC}"
echo -e "${GREEN}${BOLD}     INSTALLATION SUCCESSFUL!                                   ${NC}"
echo -e "${GREEN}${BOLD}                                                                 ${NC}"
echo -e "${GREEN}${BOLD}     Your server is now ready.                                  ${NC}"
echo -e "${GREEN}${BOLD}                                                                 ${NC}"
echo -e "${GREEN}${BOLD}=================================================================${NC}"
echo ""
echo -e "${GREEN}${BOLD}WEBHOOK URL:${NC} $WEBHOOK_URL"
echo ""
echo -e "${YELLOW}${BOLD}NEXT STEPS:${NC}"
echo ""
echo -e "  ${BOLD}1.${NC} Edit your API keys in the configuration file:"
echo ""
echo -e "     ${MAGENTA}nano $CURRENT_DIR/config/application.yaml${NC}"
echo ""
echo -e "  ${BOLD}2.${NC} After saving your configuration, restart the bot:"
echo ""
echo -e "     ${MAGENTA}~/restart_bot.sh${NC}"
echo ""
echo -e "  ${BOLD}3.${NC} Your bot will then be ready to receive webhook requests!"
echo ""
echo -e "${GREEN}${BOLD}=================================================================${NC}"
echo -e "${GREEN}${BOLD}                    HAPPY TRADING!                              ${NC}"
echo -e "${GREEN}${BOLD}=================================================================${NC}"
echo ""
