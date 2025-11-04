## 🚀 Installation Ultra-Rapide

**Pas besoin de compte GitHub !**

Sur ton VPS Ubuntu :
```bash
git clone https://github.com/CryptoSauceYT/AutomatisationUT.git
cd AutomatisationUT
bash install_bot_github.sh
```

⏱️ Durée : 5-10 minutes

Le script installe automatiquement :
- Docker et Docker Compose
- Compilation du projet Java
- Configuration SSL
- Démarrage du bot
```

---

### 6️⃣ **Vérifier config/application.yaml** ⚠️ SÉCURITÉ

Sur GitHub :
- Clique sur `config/application.yaml`
- **VÉRIFIE** qu'il n'y a **AUCUNE vraie clé API**
- Tu dois voir : `YOUR_BITUNIX_API_KEY_HERE`
- Si tu vois tes vraies clés → **Supprime le fichier et re-upload la version template !**

---

## ✅ Résumé des Actions
```
☐ 1. Remplacer Dockerfile (multi-stage)
☐ 2. Vérifier docker-compose.yml (sans "version:")
☐ 3. Remplacer script d'installation
☐ 4. Supprimer trading-bot-main.zip
☐ 5. Mettre à jour README.md
☐ 6. Vérifier application.yaml (pas de vraies clés)
