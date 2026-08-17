# Render Free Tier Deployment Guide

## Overview
This guide covers deploying SmartLedger (Web App + AI + DB + Redis) on Render free tier.

## Prerequisites
- GitHub account with SmartLedger repository
- Render account (free tier)
- Redis Cloud or Upstash account (for free Redis)
- n8n instance (for AI workflows - can be self-hosted or cloud)

## Architecture on Render Free Tier

### Services
1. **PostgreSQL Database** (Render PostgreSQL free tier)
   - 90 days free, then $7/month
   - 1GB RAM, 10GB disk

2. **Spring Boot Web App** (Render Web Service free tier)
   - Always free
   - 512MB RAM, sleeps after 15min inactivity
   - Port 10000

3. **Redis** (External - Redis Cloud or Upstash)
   - Redis Cloud: 30MB free forever
   - Upstash: 10K commands/day free

4. **AI/n8n** (External)
   - Self-host n8n on Render or use n8n cloud
   - Or use alternative AI services

## Step-by-Step Deployment

### 1. Set Up External Services

#### Redis (Redis Cloud)
1. Go to [Redis Cloud](https://redis.com/try-free/)
2. Create free account
3. Create database (30MB free tier)
4. Note connection details:
   - Host
   - Port (usually 6379)
   - Password

#### n8n (AI Workflows)
**Option A: Self-host on Render**
1. Fork n8n repository
2. Deploy as Render web service
3. Set up webhook endpoint

**Option B: n8n Cloud**
1. Sign up at [n8n.cloud](https://n8n.cloud)
2. Create workflow
3. Note webhook URL

### 2. Configure Render Environment Variables

Update `render.yaml` with your external service details:

```yaml
- key: REDIS_HOST
  value: your-redis-cloud-host.com  # From Redis Cloud
- key: REDIS_PASSWORD
  value: your-redis-password  # From Redis Cloud
- key: N8N_BASE_URL
  value: https://your-n8n-instance.com  # Your n8n instance
- key: MAIL_USERNAME
  value: your-email@gmail.com  # Your Gmail
- key: MAIL_PASSWORD
  value: your-app-password  # Gmail app password
```

### 3. Deploy to Render

#### Option A: Using render.yaml (Recommended)
1. Push code to GitHub
2. Go to Render dashboard
3. Click "New +" → "Blueprint"
4. Connect your GitHub repository
5. Render will detect `render.yaml` and create all services
6. Review and deploy

#### Option B: Manual Setup
1. **PostgreSQL**
   - New → PostgreSQL
   - Name: `smartledger-db`
   - Database: `smartledger_db`
   - Plan: Free
   - Deploy

2. **Web Service**
   - New → Web Service
   - Name: `smartledger-app`
   - Environment: Docker
   - Plan: Free
   - Connect to GitHub repo
   - Add environment variables (see below)
   - Deploy

### 4. Environment Variables

Set these in Render web service:

**Database:**
- `SPRING_PROFILES_ACTIVE`: `prod`
- `DATASOURCE_URL`: (auto-filled from PostgreSQL service)
- `DATASOURCE_USERNAME`: (auto-filled from PostgreSQL service)
- `DATASOURCE_PASSWORD`: (auto-filled from PostgreSQL service)

**Redis:**
- `REDIS_HOST`: Your Redis Cloud host
- `REDIS_PORT`: `6379`
- `REDIS_PASSWORD`: Your Redis Cloud password

**Security:**
- `JWT_SECRET`: (generate random string)
- `JWT_EXPIRATION`: `86400000`
- `ADMIN_EMAIL`: `admin@smartledger.com`
- `ADMIN_PASSWORD`: (generate secure password)

**Application:**
- `APP_BASE_URL`: `https://smartledger-app.onrender.com`
- `APP_EMAIL_ENABLED`: `true`

**AI/n8n:**
- `N8N_BASE_URL`: Your n8n instance URL
- `N8N_WEBHOOK_PATH`: `/webhook/finance-ai-insight`
- `N8N_CALLBACK_SECRET`: (generate random string)

**Email:**
- `MAIL_HOST`: `smtp.gmail.com`
- `MAIL_PORT`: `587`
- `MAIL_USERNAME`: Your Gmail
- `MAIL_PASSWORD`: Gmail app password

### 5. Post-Deployment Steps

1. **Access Application**
   - URL: `https://smartledger-app.onrender.com`
   - First login: Use admin credentials from env vars

2. **Test Database Connection**
   - Check Render logs for successful Flyway migrations
   - Verify tables created

3. **Test Redis Connection**
   - Check logs for Redis connection success
   - Test caching functionality

4. **Configure AI Workflows**
   - Set up n8n workflow for finance insights
   - Configure webhook in SmartLedger
   - Test AI features

## Free Tier Limitations & Considerations

### Web Service
- **Sleep Mode**: App sleeps after 15min inactivity
  - Cold start takes ~30-60 seconds
  - Not suitable for real-time features
  - Use Render cron jobs or external pingers to keep awake

- **Resources**: 512MB RAM, 0.1 CPU
  - Reduced connection pools configured
  - May experience slow performance under load

### PostgreSQL
- **Free Duration**: 90 days only
  - After 90 days, $7/month
  - Consider migrating to free alternatives (Supabase, Neon)

- **Resources**: 1GB RAM, 10GB disk
  - Suitable for small to medium datasets
  - Monitor storage usage

### Redis
- **External Service Required**
  - Render doesn't offer Redis on free tier
  - Redis Cloud: 30MB free (suitable for caching)
  - Upstash: 10K commands/day free

## Monitoring

1. **Render Dashboard**
   - Monitor service health
   - View logs
   - Check resource usage

2. **Application Metrics**
   - Access `/actuator/health`
   - Access `/actuator/metrics`
   - Set up alerts for failures

## Troubleshooting

### App Won't Start
- Check Render logs for errors
- Verify all environment variables set
- Ensure PostgreSQL is healthy before app starts

### Database Connection Issues
- Verify DATASOURCE_URL format
- Check PostgreSQL service is running
- Ensure credentials match

### Redis Connection Issues
- Verify Redis Cloud credentials
- Check Redis Cloud service status
- Ensure host/port correct

### App Sleeps Too Often
- Use external cron job to ping every 10min
- Consider upgrading to paid tier for always-on
- Implement wake-on-webhook if possible

## Cost Breakdown (After Free Periods)

- **Web Service**: Free (always)
- **PostgreSQL**: $7/month (after 90 days)
- **Redis**: Free (Redis Cloud) or $5/month (Upstash paid)
- **n8n**: $20/month (cloud) or free (self-hosted)

**Total**: $7-32/month depending on choices

## Alternative Free Options

### PostgreSQL Alternatives
- **Supabase**: 500MB free forever
- **Neon**: Serverless PostgreSQL, free tier available
- **PlanetScale**: MySQL-based, free tier

### Redis Alternatives
- **Upstash**: 10K commands/day free
- **Redis Cloud**: 30MB free forever

## Scaling Considerations

When ready to scale beyond free tier:
1. Upgrade web service to paid ($7/month for 512MB always-on)
2. Add Redis instance on Render ($15/month)
3. Consider load balancer for multiple instances
4. Implement CDN for static assets

## Security Notes

1. **Secrets Management**
   - Never commit secrets to Git
   - Use Render's environment variables
   - Rotate secrets regularly

2. **HTTPS**
   - Render provides automatic SSL certificates
   - No additional configuration needed

3. **Database Access**
   - PostgreSQL only accessible from Render services
   - Use internal service names for connections

## Backup Strategy

1. **Database Backups**
   - Render PostgreSQL includes daily backups
   - Export dumps regularly using pg_dump

2. **Application Code**
   - Stored in GitHub
   - Tag releases for rollback capability

## Jenkins CI/CD Integration

### Current Setup
Your project includes a `Jenkinsfile` configured for CI/CD with Render deployment.

### Jenkins Pipeline Stages
1. **Build** - Maven compilation
2. **Quality Checks** - Checkstyle and Spotless
3. **Unit Tests** - JUnit tests
4. **Coverage Report** - JaCoCo coverage
5. **Integration Tests** - Testcontainers
6. **Security Scan** - OWASP Dependency-Check
7. **Package** - Maven package
8. **Docker Build** - Build Docker image
9. **Docker Push** - Push to registry (main branch)
10. **Deploy to Staging** - Deploy to staging (develop branch)
11. **Deploy to Production** - Deploy to Render (main branch)

### Jenkins + Render Integration Options

**Option 1: Auto-Deploy (Recommended)**
- Jenkins runs tests and quality checks
- On success, push to main branch
- Render auto-deploys from GitHub
- Simplest approach

**Option 2: Render CLI**
```groovy
stage('Deploy to Production') {
    steps {
        sh 'render blueprint apply --confirm'
    }
}
```
- Requires `render-cli` installed on Jenkins
- More control over deployment

**Option 3: Render API**
```groovy
stage('Deploy to Production') {
    steps {
        withCredentials([string(credentialsId: 'render-api-token', variable: 'RENDER_TOKEN')]) {
            sh '''
                curl -X POST \
                https://api.render.com/v1/services/YOUR_SERVICE_ID/deploys \
                -H "Authorization: Bearer $RENDER_TOKEN"
            '''
        }
    }
}
```
- Requires Render API token in Jenkins credentials
- Programmatic control

### Jenkins Setup for Render

1. **Install Required Plugins**
   - Docker Pipeline
   - Git
   - Email Extension

2. **Configure Credentials**
   - `render-api-token`: Render API key
   - `docker-registry`: Docker registry URL
   - `docker-credentials`: Docker registry credentials
   - Database and application secrets

3. **Configure Tools**
   - Maven 3.9
   - JDK 21

4. **Configure SMTP for Email Notifications**
   - Navigate to Jenkins → Manage Jenkins → Configure System
   - Scroll to "Email Extension" section
   - Configure SMTP settings:

   **For Gmail (Development):**
   - SMTP Server: `smtp.gmail.com`
   - SMTP Port: `587`
   - Use TLS: `✓` (checked)
   - Use SSL: `✗` (unchecked)
   - Username: Your Gmail address
   - Password: Gmail App-Specific Password (not regular password)
   - Reply-To Address: Your email
   - Default Content Type: `HTML (text/html)`

   **For SendGrid (Production Recommended):**
   - SMTP Server: `smtp.sendgrid.net`
   - SMTP Port: `587`
   - Use TLS: `✓`
   - Username: `apikey`
   - Password: Your SendGrid API key

   **For Mailgun (Production Alternative):**
   - SMTP Server: `smtp.mailgun.org`
   - SMTP Port: `587`
   - Use TLS: `✓`
   - Username: Your Mailgun username
   - Password: Your Mailgun password

   **Test Configuration:**
   - Click "Test by sending test email"
   - Enter recipient email
   - Verify email is received

5. **Set Up Webhook**
   - Configure GitHub webhook to trigger Jenkins on push
   - Jenkins runs pipeline and validates
   - On success, Render auto-deploys

### Simplified Jenkins for Render

Since Render handles deployment from GitHub, you can simplify Jenkins to focus on CI:

```groovy
pipeline {
    agent any
    stages {
        stage('Build & Test') {
            steps {
                sh './mvnw clean test'
            }
        }
        stage('Quality Checks') {
            steps {
                sh './mvnw spotless:check checkstyle:check'
            }
        }
        stage('Security Scan') {
            steps {
                sh './mvnw org.owasp:dependency-check-maven:check'
            }
        }
    }
    post {
        success {
            echo 'Tests passed - Render will auto-deploy on push to main'
        }
    }
}
```

### Benefits of Jenkins + Render

- **Jenkins**: Comprehensive CI (tests, quality, security)
- **Render**: Simple CD (auto-deploy from GitHub)
- **Separation of concerns**: Jenkins validates, Render deploys

## Support

- Render Documentation: https://render.com/docs
- Redis Cloud Docs: https://redis.com/docs
- n8n Documentation: https://docs.n8n.io
- Jenkins Documentation: https://www.jenkins.io/doc
