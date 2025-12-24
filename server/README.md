# WorkTime Deployment Guide (Debian/Ubuntu)

This README describes a stable, production-oriented server configuration for deploying **WorkTime** on a Linux server (Debian or Ubuntu):

- **Frontend**: Vite-built static site served by **Nginx**
- **Backend**: Spring Boot runnable JAR managed by **systemd** (Java 21)
- **Recommended topology**: Nginx terminates HTTP(S) and serves the frontend; backend is bound to localhost and reached via reverse proxy.

---

## Contents

- [Assumptions](#assumptions)
- [Reference Topology](#reference-topology)
- [Packages](#packages)
  - [Install Java 21](#install-java-21)
  - [Install Nginx](#install-nginx)
- [Users, Permissions, and Directories](#users-permissions-and-directories)
- [Frontend Deployment (Vite dist)](#frontend-deployment-vite-dist)
  - [Notes for sub-path hosting](#notes-for-sub-path-hosting)
  - [Build locally](#build-locally)
  - [Copy dist to the server](#copy-dist-to-the-server)
- [Backend Deployment (Spring Boot JAR)](#backend-deployment-spring-boot-jar)
  - [Update application.yml first](#update-applicationyml-first)
  - [Build the backend](#build-the-backend)
  - [Validate name resolution and port reachability](#validate-name-resolution-and-port-reachability)
  - [Recommended versioned JAR plus stable symlink](#recommended-versioned-jar-plus-stable-symlink)
- [Configuration and Secrets](#configuration-and-secrets)
- [Database Connectivity](#database-connectivity)
- [Systemd Service](#systemd-service)
  - [Start, stop, logs](#start-stop-logs)
  - [Quick local health check](#quick-local-health-check)
- [Reverse Proxy (Nginx)](#reverse-proxy-nginx)
  - [Enable the site](#enable-the-site)
- [TLS (Let’s Encrypt)](#tls-lets-encrypt)
  - [Renewals](#renewals)
- [Firewall](#firewall)
- [Operational Runbook](#operational-runbook)
  - [Upgrade procedure (versioned JAR plus symlink)](#upgrade-procedure-versioned-jar-plus-symlink)
  - [Log retention and rotation](#log-retention-and-rotation)
  - [Backups](#backups)
- [Troubleshooting](#troubleshooting)
  - [Frontend](#frontend)
  - [Backend](#backend)
  - [Nginx](#nginx)

---

## Assumptions

- Server OS: **Debian** (e.g., Debian 12) or Ubuntu LTS.
- Backend runs on **Java 21**.
- Backend artifacts:
  - Install directory: `/opt/worktime-backend/`
  - JAR (recommended): `/opt/worktime-backend/worktime-backend.jar` (a symlink to a versioned JAR)
  - Environment file: `/etc/worktime-backend/worktime-backend.env`
  - Dedicated system user: `worktime`
  - HTTP port: `8080` (backend binds to localhost in production)
  - Profile: `prod` (recommended)
- Frontend artifacts:
  - Vite build output: `dist/`
  - Nginx web root: `/var/www/worktime/`

> If you change the JAR name/version, update the symlink (recommended) or the `ExecStart` path.

---

## Reference Topology

- Internet → **Nginx** (ports 80/443)
- Nginx serves static frontend from `/var/www/worktime`
- Nginx reverse-proxies `/api/` → backend at `http://127.0.0.1:8080`
- Backend binds to localhost only (no direct public exposure)

---

## Packages

### Install Java 21

```bash
sudo apt update
sudo apt install -y openjdk-21-jre-headless
java -version
```

> Optional: install JDK tooling (includes `jcmd`, etc.) for diagnostics:
>
> ```bash
> sudo apt install -y openjdk-21-jdk-headless
> ```

### Install Nginx

```bash
sudo apt install -y nginx
sudo systemctl enable --now nginx
```

---

## Users, Permissions, and Directories

Create a system user/group and directories used by the backend service:

```bash
sudo addgroup --system worktime || true
sudo adduser  --system --no-create-home --ingroup worktime worktime || true

sudo mkdir -p /opt/worktime-backend
sudo chown worktime:worktime /opt/worktime-backend
sudo chmod 755 /opt/worktime-backend

sudo mkdir -p /etc/worktime-backend
sudo chown root:worktime /etc/worktime-backend
sudo chmod 750 /etc/worktime-backend

sudo mkdir -p /var/www/worktime
sudo chown worktime:www-data /var/www/worktime
```

> Recommended: keep secrets in `/etc/worktime-backend` with `root:worktime` ownership and `0640`/`0750` permissions.

---

## Frontend Deployment (Vite dist)

### Notes for sub-path hosting

If the frontend is hosted under a sub-path (e.g., `https://example.com/worktime/`), set Vite `base` accordingly before building:

```js
// vite.config.js
import { defineConfig } from "vite";
export default defineConfig({
  base: "/worktime/",
});
```

### Build locally

Build the frontend artifacts as documented in `../frontend/README.md` and ensure the output is available in `frontend/dist/`.

Typical build steps (from `frontend/`):

```bash
npm install
npm run build
```
### Copy dist to the server

Copy the **contents** of `dist/` to `/var/www/worktime/`.

Recommended permissions after copying:

```bash
sudo chown -R worktime:www-data /var/www/worktime
sudo find /var/www/worktime -type d -exec chmod 2755 {} \;
sudo find /var/www/worktime -type f -exec chmod 0644 {} \;
```

---

## Backend Deployment (Spring Boot JAR)

### Update application.yml first

**Update `application.yml` (or `application-prod.yml`) first, then build.**

This is important because:
- You want production-safe defaults (e.g., bind to `127.0.0.1`)
- Secrets should be injected via environment variables (not committed to Git)
- Some projects perform config validation at startup and/or during tests

Recommended baseline pattern (adapt to your project):

```yaml
server:
  address: 127.0.0.1
  port: 8080

spring:
  datasource:
    url: jdbc:mariadb://sql:3306/private_worktime
    username: ${MYSQL_WORKTIME_USER}
    password: ${MYSQL_WORKTIME_PASSWORD}
```

If you use Spring profiles, place production-only values in `application-prod.yml` and keep:

```bash
SPRING_PROFILES_ACTIVE=prod
```

in the environment file used by `systemd`.

### Build the backend

From your backend repo:

```bash
mvn clean install
```

### Validate name resolution and port reachability

From the backend server (preflight for runtime connectivity):

```bash
getent hosts sql || echo "Host 'sql' does not resolve"
nc -zv sql 3306
```

### Recommended versioned JAR plus stable symlink

Copy your versioned JAR to the server to `/opt/worktime-backend/` and update a stable symlink used by systemd:

```bash
# Example versioned name
sudo cp worktime-backend-1.0.0.jar /opt/worktime-backend/
sudo chown worktime:worktime /opt/worktime-backend/worktime-backend-1.0.0.jar
sudo chmod 0644 /opt/worktime-backend/worktime-backend-1.0.0.jar

# Stable symlink used by systemd
sudo ln -sfn /opt/worktime-backend/worktime-backend-1.0.0.jar /opt/worktime-backend/worktime-backend.jar
sudo chown -h worktime:worktime /opt/worktime-backend/worktime-backend.jar
```

---

## Configuration and Secrets

Create an environment file used by systemd. This keeps secrets out of the unit file and (for most setups) out of your deployment repository.

Edit:

```bash
sudo nano /etc/worktime-backend/worktime-backend.env
```

Example:

```bash
# JVM settings (adjust as needed)
JAVA_OPTS="-Xms256m -Xmx512m"

# Spring profile
SPRING_PROFILES_ACTIVE=prod

# Optional: enforce local-only bind (recommended if not set elsewhere)
# For Spring Boot 2.x/3.x, this typically works:
SPRING_APPLICATION_JSON='{"server":{"address":"127.0.0.1","port":8080}}'

# Database credentials used by application.yml placeholders
MYSQL_WORKTIME_USER=worktime_user
MYSQL_WORKTIME_PASSWORD=replace_me
```

Secure it:

```bash
sudo chown root:worktime /etc/worktime-backend/worktime-backend.env
sudo chmod 0640 /etc/worktime-backend/worktime-backend.env
```

> Important: the env file must contain `KEY=value` lines only.

---

## Database Connectivity

Adjust `application.yml` to use a JDBC URL similar to:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://sql:3306/private_worktime
    username: ${MYSQL_WORKTIME_USER}
    password: ${MYSQL_WORKTIME_PASSWORD}
```

> Recommendations:
> - Ensure TLS to the DB if it is on a separate host/network segment.
> - Use least-privilege DB credentials (schema-specific grants only).

---

## Systemd Service

Create the service file:

```bash
sudo nano /etc/systemd/system/worktime-backend.service
```

Recommended unit file:

```ini
[Unit]
Description=WorkTime Backend (Spring Boot)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=worktime
Group=worktime
WorkingDirectory=/opt/worktime-backend
EnvironmentFile=/etc/worktime-backend/worktime-backend.env

# Bind to localhost in production (recommended) via application.yml or env overrides.
ExecStart=/bin/bash -c '/usr/bin/java $JAVA_OPTS -jar /opt/worktime-backend/worktime-backend.jar'

Restart=on-failure
RestartSec=2
TimeoutStopSec=30

# Hardening (good defaults)
NoNewPrivileges=true
PrivateTmp=true
ProtectHome=true
ProtectSystem=strict
ReadWritePaths=/opt/worktime-backend

# Optional: slightly tighter defaults (enable if compatible with your app)
# LockPersonality=true
# PrivateDevices=true
# ProtectKernelTunables=true
# ProtectKernelModules=true
# ProtectControlGroups=true
# RestrictSUIDSGID=true

[Install]
WantedBy=multi-user.target
```

Apply and start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now worktime-backend.service
sudo systemctl status worktime-backend.service --no-pager
```

### Start, stop, logs

```bash
sudo systemctl start worktime-backend.service
sudo systemctl stop worktime-backend.service
sudo systemctl restart worktime-backend.service

sudo journalctl -u worktime-backend.service -n 200 --no-pager
sudo journalctl -u worktime-backend.service -f
```

### Quick local health check

```bash
curl -i http://127.0.0.1:8080/actuator/health
```

> Recommendation: if Actuator is enabled, avoid exposing it publicly. See the Nginx section for a basic restriction pattern.

---

## Reverse Proxy (Nginx)

The configuration below serves the frontend and reverse-proxies API calls to the backend.

Create:

```bash
sudo nano /etc/nginx/sites-available/worktime-frontend
```

Example (single domain, frontend on `/`, backend on `/api/`):

```nginx
server {
    listen 80;
    server_name example.com;  # change to your domain

    # Frontend (Vite build output)
    root /var/www/worktime;
    index index.html;

    # SPA routing: refresh on /route should return index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API (adjust path if your backend does not use /api)
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Good defaults for upstream stability
        proxy_connect_timeout 10s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Optional: restrict Actuator to localhost only (adjust as needed)
    location /actuator/ {
        allow 127.0.0.1;
        deny all;
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
    }

    # Optional: cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|svg|ico|webp|woff2?)$ {
        expires 7d;
        add_header Cache-Control "public, max-age=604800, immutable";
        try_files $uri =404;
    }
}
```

### Enable the site

```bash
sudo ln -sfn /etc/nginx/sites-available/worktime-frontend /etc/nginx/sites-enabled/worktime-frontend
sudo rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true
sudo nginx -t
sudo systemctl reload nginx
```

---

## TLS (Let’s Encrypt)

Requires a real DNS record pointing to the server.

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d example.com
```

### Renewals

Most systems install a timer for automatic renewal. Validate:

```bash
sudo systemctl list-timers | grep -i certbot || true
sudo certbot renew --dry-run
```

---

## Firewall

Recommended: expose only HTTP/HTTPS externally and keep the backend bound to localhost.

Using UFW:

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
sudo ufw status
```

---

## Operational Runbook

### Upgrade procedure (versioned JAR plus symlink)

1. Update `application.yml`/`application-prod.yml` as needed
2. Build the new versioned JAR
3. Copy the new versioned JAR into place
4. Update the symlink
5. Restart the service
6. Verify logs and health endpoint

Example:

```bash
# 1) Update config in your repo, then:
mvn clean install

sudo systemctl stop worktime-backend.service

sudo cp worktime-backend-1.0.1.jar /opt/worktime-backend/
sudo chown worktime:worktime /opt/worktime-backend/worktime-backend-1.0.1.jar
sudo chmod 0644 /opt/worktime-backend/worktime-backend-1.0.1.jar
sudo ln -sfn /opt/worktime-backend/worktime-backend-1.0.1.jar /opt/worktime-backend/worktime-backend.jar

sudo systemctl start worktime-backend.service
sudo journalctl -u worktime-backend.service -n 200 --no-pager
curl -i http://127.0.0.1:8080/actuator/health
```

### Log retention and rotation

- `journald` retention is controlled via `/etc/systemd/journald.conf`.
- Nginx logs typically rotate via `logrotate` (usually installed by default). Confirm:

```bash
sudo logrotate -d /etc/logrotate.d/nginx
```

### Backups

At minimum, back up:

- Database (logical dumps or snapshots)
- `/etc/worktime-backend/worktime-backend.env` (securely)
- Deployment configs: `/etc/nginx/sites-available/worktime-frontend`, `/etc/systemd/system/worktime-backend.service`

---

## Troubleshooting

### Frontend

**White page / missing assets**

- If you host under a sub-path, set Vite `base` and rebuild.
- Confirm the deployed files exist:

```bash
ls -la /var/www/worktime
```

**404 on browser refresh**

Ensure Nginx has:

```nginx
try_files $uri $uri/ /index.html;
```

### Backend

**Service crash loop**

Stop the service to prevent continuous restarts while debugging:

```bash
sudo systemctl stop worktime-backend.service
```

**Common causes**

- Missing required environment variables (e.g., `MYSQL_WORKTIME_USER`, `MYSQL_WORKTIME_PASSWORD`)
- DB hostname does not resolve (e.g., `sql`)
- Port already in use
- Wrong JAR path/name in `ExecStart`
- Invalid environment file syntax (must be `KEY=value`)

**Confirm JAR path and Java**

```bash
ls -lah /opt/worktime-backend/
readlink -f /opt/worktime-backend/worktime-backend.jar
/usr/bin/java -version
```

**Confirm effective ExecStart**

```bash
sudo systemctl show -p ExecStart worktime-backend.service
```

**Manual run (useful for debugging)**

Run as the service user using the same environment file:

```bash
sudo -u worktime -H bash -lc '
set -a
source /etc/worktime-backend/worktime-backend.env
set +a
exec /usr/bin/java $JAVA_OPTS -jar /opt/worktime-backend/worktime-backend.jar
'
```

### Nginx

```bash
sudo nginx -t
sudo systemctl status nginx --no-pager
sudo tail -n 200 /var/log/nginx/error.log
sudo tail -n 200 /var/log/nginx/access.log
```

---

## Notes on stability

- Prefer **Nginx + TLS** and keep the backend bound to localhost.
- Keep secrets in the env file with tight permissions.
- Consider monitoring (Actuator + restricted access) and define log retention policies if the system will run long-term.