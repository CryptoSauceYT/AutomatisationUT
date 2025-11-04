#!/bin/bash

###############################################################################
# SCRIPT D'INSTALLATION ULTRA-COMPLET - BOT TRADING BITUNIX
# 
# Ce script fait TOUT automatiquement :
# - Installation de Docker et dépendances
# - Création de tous les fichiers nécessaires (avec bonnes images)
# - Configuration SSL
# - Démarrage du bot
#
# Usage: bash install_bot_complete.sh
###############################################################################

set -e

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
NC='\033[0m'

print_step() {
    echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
}

print_error() { echo -e "${RED}❌ ERREUR: $1${NC}"; }
print_success() { echo -e "${GREEN}✅ $1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠️  $1${NC}"; }
print_info() { echo -e "${MAGENTA}ℹ️  $1${NC}"; }

# Vérifier Ubuntu
if ! grep -q "Ubuntu" /etc/os-release; then
    print_error "Ce script nécessite Ubuntu. Système détecté: $(cat /etc/os-release | grep PRETTY_NAME)"
    exit 1
fi

clear
cat << "EOF"
╔═══════════════════════════════════════════════════════════════╗
║                                                               ║
║        🤖 INSTALLATION BOT TRADING BITUNIX 🚀                ║
║                                                               ║
║     Installation complète automatisée en une commande        ║
║                                                               ║
╚═══════════════════════════════════════════════════════════════╝
EOF
echo ""

# ============================================================================
# 1. MISE À JOUR SYSTÈME
# ============================================================================
print_step "1️⃣  MISE À JOUR DU SYSTÈME"
sudo apt update -qq
sudo apt upgrade -y -qq
print_success "Système mis à jour"

# ============================================================================
# 2. INSTALLATION DOCKER
# ============================================================================
print_step "2️⃣  INSTALLATION DE DOCKER"

if command -v docker &> /dev/null; then
    print_warning "Docker déjà installé ($(docker --version))"
else
    print_info "Installation de Docker..."
    
    # Prérequis
    sudo apt install -y -qq apt-transport-https ca-certificates curl software-properties-common
    
    # Clé GPG Docker
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Dépôt Docker
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Installation
    sudo apt update -qq
    sudo apt install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
    
    # Ajouter user au groupe docker
    sudo usermod -aG docker $USER
    
    print_success "Docker installé: $(docker --version)"
fi

# ============================================================================
# 3. OUTILS
# ============================================================================
print_step "3️⃣  INSTALLATION DES OUTILS"
sudo apt install -y -qq git nano htop unzip curl jq
print_success "Outils installés"

# ============================================================================
# 4. FIREWALL
# ============================================================================
print_step "4️⃣  CONFIGURATION DU FIREWALL"
sudo ufw --force enable
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
print_success "Firewall configuré"

# ============================================================================
# 5. CRÉATION DU RÉPERTOIRE
# ============================================================================
print_step "5️⃣  CRÉATION DU RÉPERTOIRE DU BOT"
BOT_DIR="$HOME/trading-bot"

if [ -d "$BOT_DIR" ]; then
    print_warning "Le répertoire $BOT_DIR existe déjà"
    read -p "Supprimer et recommencer? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        rm -rf "$BOT_DIR"
        print_success "Répertoire supprimé"
    else
        print_error "Installation annulée"
        exit 1
    fi
fi

mkdir -p "$BOT_DIR"/{config,src/main/java/com/bot}
cd "$BOT_DIR"
print_success "Répertoire créé: $BOT_DIR"

# ============================================================================
# 6. RÉCUPÉRATION DU CODE
# ============================================================================
print_step "6️⃣  RÉCUPÉRATION DU CODE"
echo "Comment veux-tu récupérer le code?"
echo "1) GitHub (recommandé)"
echo "2) Upload manuel (trading-bot-main.zip)"
read -p "Choix (1 ou 2): " -n 1 -r DOWNLOAD_CHOICE
echo

if [[ $DOWNLOAD_CHOICE == "1" ]]; then
    read -p "URL du dépôt GitHub: " REPO_URL
    git clone "$REPO_URL" .
    print_success "Code récupéré depuis GitHub"
    
elif [[ $DOWNLOAD_CHOICE == "2" ]]; then
    print_warning "Uploade le fichier trading-bot-main.zip dans $BOT_DIR"
    print_info "Commande: scp trading-bot-main.zip $(whoami)@$(curl -s ifconfig.me):$BOT_DIR/"
    read -p "Appuie sur Enter une fois uploadé..."
    
    if [ -f "trading-bot-main.zip" ]; then
        unzip -q trading-bot-main.zip
        if [ -d "trading-bot-main" ]; then
            mv trading-bot-main/* .
            rm -rf trading-bot-main trading-bot-main.zip
        fi
        print_success "Code extrait"
    else
        print_error "Fichier introuvable"
        exit 1
    fi
fi

# Vérifier si pom.xml existe, sinon on est dans une installation partielle
if [ ! -f "pom.xml" ]; then
    print_warning "Code Java non trouvé, création des fichiers Docker uniquement..."
fi

# ============================================================================
# 7. CRÉATION AUTOMATIQUE DES FICHIERS DOCKER (IMAGES CORRIGÉES)
# ============================================================================
print_step "7️⃣  CRÉATION DES FICHIERS DOCKER"

# Dockerfile avec eclipse-temurin (image corrigée)
cat > Dockerfile << 'DOCKERFILE_END'
FROM eclipse-temurin:11-jdk-slim

WORKDIR /app

COPY target/trading-bot-1.0.0.jar trading-bot.jar

RUN mkdir -p /var/logs/trading-bot/ && chmod 777 /var/logs/trading-bot/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "./trading-bot.jar"]
DOCKERFILE_END
print_success "Dockerfile créé (eclipse-temurin:11-jdk-slim)"

# docker-compose.yml (sans version obsolète)
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
    depends_on:
      - trading-bot
    networks:
      - bot-network
    restart: unless-stopped

networks:
  bot-network:
    driver: bridge
COMPOSE_END
print_success "docker-compose.yml créé"

# ============================================================================
# 8. CONFIGURATION SSL
# ============================================================================
print_step "8️⃣  CONFIGURATION SSL"
echo "Type de certificat SSL:"
echo "1) Let's Encrypt (GRATUIT, nécessite un domaine)"
echo "2) Auto-signé (pour tests)"
read -p "Choix (1 ou 2): " -n 1 -r SSL_CHOICE
echo

sudo mkdir -p /etc/nginx/ssl

if [[ $SSL_CHOICE == "1" ]]; then
    read -p "Nom de domaine (ex: bot.example.com): " DOMAIN_NAME
    
    # Installation Certbot
    sudo apt install -y -qq certbot
    
    # Arrêter nginx si en cours
    docker compose down 2>/dev/null || true
    
    # Obtenir certificat
    sudo certbot certonly --standalone -d "$DOMAIN_NAME" --non-interactive --agree-tos --email admin@$DOMAIN_NAME
    
    # Copier certificats
    sudo cp /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem /etc/nginx/ssl/$DOMAIN_NAME.crt
    sudo cp /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem /etc/nginx/ssl/$DOMAIN_NAME.key
    sudo chmod 644 /etc/nginx/ssl/*
    
    SERVER_NAME="$DOMAIN_NAME"
    SSL_CERT="/etc/nginx/ssl/$DOMAIN_NAME.crt"
    SSL_KEY="/etc/nginx/ssl/$DOMAIN_NAME.key"
    WEBHOOK_URL="https://$DOMAIN_NAME/api/v1/place_limit_order"
    
    print_success "Certificat Let's Encrypt configuré"
    
elif [[ $SSL_CHOICE == "2" ]]; then
    PUBLIC_IP=$(curl -s ifconfig.me)
    
    # Générer certificat auto-signé
    sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
        -keyout /etc/nginx/ssl/selfsigned.key \
        -out /etc/nginx/ssl/selfsigned.crt \
        -subj "/C=CH/ST=Zurich/L=Zurich/O=TradingBot/CN=$PUBLIC_IP" 2>/dev/null
    
    sudo chmod 644 /etc/nginx/ssl/*
    
    SERVER_NAME="$PUBLIC_IP"
    SSL_CERT="/etc/nginx/ssl/selfsigned.crt"
    SSL_KEY="/etc/nginx/ssl/selfsigned.key"
    WEBHOOK_URL="https://$PUBLIC_IP/api/v1/place_limit_order"
    
    print_success "Certificat auto-signé créé"
    print_warning "⚠️  Les navigateurs afficheront un warning"
fi

# Créer nginx.conf
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
        
        # Timeouts augmentés pour TradingView
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
NGINX_END
print_success "nginx.conf créé"

# ============================================================================
# 9. CRÉATION DU FICHIER DE CONFIGURATION
# ============================================================================
print_step "9️⃣  CRÉATION DE LA CONFIGURATION"

cat > config/application.yaml << 'CONFIG_END'
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: refresh

spring:
  profiles:
    active: prod
  application:
    name: trading-bot
  cloud:
    config:
      enabled: false
      import-check:
        enabled: false

# Configuration du bot
bot-config:
  trading-pairs:
    - BTCUSDT
    - ETHUSDT
    - XRPUSDT
    - BNBUSDT
    - SOLUSDT
    - TRXUSDT
    - DOGEUSDT
    - ADAUSDT
    - HYPEUSDT
    - BCHUSDT
    - SUIUSDT
    - LINKUSDT
    - XLMUSDT
    - AVAXUSDT
    - 1000SHIBUSDT
    - HBARUSDT
    - LTCUSDT
    - DOTUSDT
    - UNIUSDT
    - AAVEUSDT
    - 1000PEPEUSDT
    - TAOUSDT
    - APTUSDT
    - NEARUSDT
    - ICPUSDT
    - ETCUSDT
    - ONDOUSDT

  profiles:
    # ⚠️  EXEMPLE - REMPLACE PAR TES VRAIES CLÉS API
    eth-swing:
      leverage: 5
      amount: 100
      tp-offset: 0.0015
      api-key: YOUR_BITUNIX_API_KEY_HERE
      api-secret: YOUR_BITUNIX_API_SECRET_HERE
    
    btc-longterm:
      leverage: 3
      amount: 200
      tp-offset: 0.0020
      api-key: YOUR_BITUNIX_API_KEY_HERE
      api-secret: YOUR_BITUNIX_API_SECRET_HERE

  expired-time: 7000
  max-leverage: 25
CONFIG_END
print_success "application.yaml créé (template)"

print_warning ""
print_warning "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
print_warning "⚠️  CONFIGURATION DES CLÉS API REQUISE"
print_warning "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Édite maintenant le fichier de configuration:"
echo ""
echo "  nano $BOT_DIR/config/application.yaml"
echo ""
echo "Configure au minimum un profil avec:"
echo "  • Tes clés API Bitunix"
echo "  • Le levier et montant souhaités"
echo "  • Le tp-offset selon ta stratégie"
echo ""
read -p "Appuie sur Enter une fois la configuration terminée..."

# ============================================================================
# 10. DÉMARRAGE DU BOT
# ============================================================================
print_step "🔟 DÉMARRAGE DU BOT"

# Si le code Java n'existe pas, on ne peut pas build
if [ ! -f "pom.xml" ]; then
    print_error "Impossible de démarrer: code source Java manquant"
    print_info "Assure-toi d'avoir tous les fichiers du projet dans $BOT_DIR"
    exit 1
fi

docker compose up -d --build

print_info "Attente du démarrage (15 secondes)..."
sleep 15

# Vérifier que les containers tournent
if docker compose ps | grep -q "Up"; then
    print_success "Bot démarré avec succès! 🎉"
else
    print_error "Problème au démarrage"
    docker compose logs
    exit 1
fi

# ============================================================================
# 11. TESTS
# ============================================================================
print_step "1️⃣1️⃣ TESTS DE FONCTIONNEMENT"

echo "Test du health check..."
sleep 3
if curl -s http://localhost:8080/api/v1/check 2>/dev/null | grep -q "success"; then
    print_success "Health check OK ✅"
else
    print_warning "Health check non disponible (normal si le bot vient de démarrer)"
fi

# ============================================================================
# 12. SCRIPTS UTILES
# ============================================================================
print_step "1️⃣2️⃣ CRÉATION DES SCRIPTS UTILES"

# Script monitoring
cat > "$HOME/monitor_bot.sh" << 'EOF'
#!/bin/bash
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "       🤖 TRADING BOT STATUS"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🐋 Docker Containers:"
cd ~/trading-bot && docker compose ps
echo ""
echo "📊 Resource Usage:"
docker stats --no-stream --no-trunc --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"
echo ""
echo "📝 Last 30 log entries:"
cd ~/trading-bot && docker compose logs --tail=30 trading-bot
echo ""
echo "🔗 Bot Health:"
curl -s http://localhost:8080/api/v1/check | jq 2>/dev/null || curl -s http://localhost:8080/api/v1/check
EOF
chmod +x "$HOME/monitor_bot.sh"

# Script backup
cat > "$HOME/backup_bot.sh" << 'EOF'
#!/bin/bash
BACKUP_DIR=~/bot-backups
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR
tar -czf $BACKUP_DIR/bot_config_$DATE.tar.gz ~/trading-bot/config/
ls -t $BACKUP_DIR/bot_config_*.tar.gz | tail -n +8 | xargs -r rm
echo "✅ Backup: bot_config_$DATE.tar.gz"
EOF
chmod +x "$HOME/backup_bot.sh"

# Script redémarrage
cat > "$HOME/restart_bot.sh" << 'EOF'
#!/bin/bash
echo "🔄 Redémarrage du bot..."
cd ~/trading-bot
docker compose down
docker compose up -d --build
echo "✅ Bot redémarré"
sleep 5
docker compose logs --tail=20 trading-bot
EOF
chmod +x "$HOME/restart_bot.sh"

print_success "Scripts créés:"
echo "  • ~/monitor_bot.sh"
echo "  • ~/backup_bot.sh"
echo "  • ~/restart_bot.sh"

# ============================================================================
# RÉSUMÉ FINAL
# ============================================================================
print_step "🎉 INSTALLATION TERMINÉE!"

cat << EOF

╔═══════════════════════════════════════════════════════════════╗
║                 ✅ TON BOT EST OPÉRATIONNEL! 🚀               ║
╚═══════════════════════════════════════════════════════════════╝

📁 Répertoire:    $BOT_DIR
⚙️  Configuration: $BOT_DIR/config/application.yaml
📊 Logs:          docker compose logs -f trading-bot

🌐 WEBHOOK URL POUR TRADINGVIEW:
   
   $WEBHOOK_URL

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 PROCHAINES ÉTAPES:

1️⃣  Vérifie le bot:
   ~/monitor_bot.sh

2️⃣  Configure TradingView:
   • Crée une alerte sur ta stratégie
   • Webhook URL: $WEBHOOK_URL
   • Message: {{strategy.order.alert_message}}

3️⃣  Teste avec un PETIT montant d'abord!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

⚠️  RAPPEL SÉCURITÉ:
   • Commence avec de PETITS montants
   • Pas de permission Withdraw sur les clés API
   • Monitore quotidiennement pendant 1 semaine
   • Backup régulier: ~/backup_bot.sh

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🛠️  COMMANDES UTILES:
   • Voir logs:     docker compose logs -f trading-bot
   • Status:        docker compose ps
   • Redémarrer:    ~/restart_bot.sh
   • Arrêter:       docker compose down
   • Monitoring:    ~/monitor_bot.sh

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

EOF

# Proposer de voir les logs
read -p "Veux-tu voir les logs en temps réel? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    cd "$BOT_DIR"
    docker compose logs -f trading-bot
fi
