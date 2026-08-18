pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = credentials('docker-registry')
        DOCKER_CREDENTIALS_ID = 'docker-credentials'
        POSTGRES_DB = credentials('postgres-db')
        POSTGRES_USER = credentials('postgres-user')
        POSTGRES_PASSWORD = credentials('postgres-password')
        REDIS_HOST = credentials('redis-host')
        REDIS_PORT = credentials('redis-port')
        REDIS_PASSWORD = credentials('redis-password')
        JWT_SECRET = credentials('jwt-secret')
        JWT_EXPIRATION = credentials('jwt-expiration')
        ADMIN_EMAIL = credentials('admin-email')
        ADMIN_PASSWORD = credentials('admin-password')
        NOTIFICATION_EMAIL = credentials('notification-email')
    }
    
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 21'
    }
    
    stages {
        stage('Build') {
            steps {
                echo 'Building the project...'
                sh './mvnw clean compile'
            }
        }
        
        stage('Quality Checks') {
            parallel {
                stage('Checkstyle') {
                    steps {
                        echo 'Running Checkstyle...'
                        sh './mvnw checkstyle:check'
                    }
                }
                stage('Spotless') {
                    steps {
                        echo 'Running Spotless...'
                        sh './mvnw spotless:check'
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
        }
        
        stage('Coverage Report') {
            steps {
                echo 'Generating coverage report...'
                sh './mvnw jacoco:report'
                publishHTML target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/site/jacoco/index.html',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report'
                ]
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests with Testcontainers...'
                sh './mvnw verify -Ptest'
            }
        }
        
        stage('Security Scan') {
            steps {
                echo 'Running OWASP Dependency-Check...'
                sh './mvnw org.owasp:dependency-check-maven:check'
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh './mvnw package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo 'Building Docker image...'
                script {
                    docker.build("finance-ledger:${BUILD_NUMBER}")
                }
            }
        }
        
        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                echo 'Pushing Docker image to registry...'
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("finance-ledger:${BUILD_NUMBER}").push()
                        docker.image("finance-ledger:${BUILD_NUMBER}").push('latest')
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                sh 'docker-compose -f docker-compose.staging.yml up -d'
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            input {
                message 'Deploy to production?'
                ok 'Deploy'
                submitter 'admin'
            }
            steps {
                echo 'Deploying to production environment on Render...'
                script {
                    // Deploy to Render using render CLI or API
                    // Option 1: Using Render CLI (requires render-cli installed)
                    // sh 'render blueprint apply --confirm'

                    // Option 2: Using Render API (requires RENDER_API_TOKEN)
                    // withCredentials([string(credentialsId: 'render-api-token', variable: 'RENDER_TOKEN')]) {
                    //     sh '''
                    //         curl -X POST \
                    //         https://api.render.com/v1/services/YOUR_SERVICE_ID/deploys \
                    //         -H "Authorization: Bearer $RENDER_TOKEN" \
                    //         -H "Content-Type: application/json"
                    //     '''
                    // }

                    // Option 3: Trigger GitHub webhook to Render (simplest)
                    // Just push to main branch and Render auto-deploys
                    echo 'Render will auto-deploy on push to main branch'
                    echo 'Manual deployment can be triggered via Render dashboard'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded!'
            emailext (
                subject: "Jenkins Build Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <p>Build successful for ${env.JOB_NAME} - ${env.BUILD_NUMBER}</p>
                    <p>Check the build at: ${env.BUILD_URL}</p>
                """,
                to: '${NOTIFICATION_EMAIL}',
                mimeType: 'text/html'
            )
        }
        failure {
            echo 'Pipeline failed!'
            emailext (
                subject: "Jenkins Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <p>Build failed for ${env.JOB_NAME} - ${env.BUILD_NUMBER}</p>
                    <p>Check the build at: ${env.BUILD_URL}</p>
                """,
                to: '${NOTIFICATION_EMAIL}',
                mimeType: 'text/html'
            )
        }
    }
}
