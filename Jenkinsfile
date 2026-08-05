pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = credentials('docker-registry')
        DOCKER_CREDENTIALS_ID = 'docker-credentials'
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/smartledger_test'
        SPRING_DATASOURCE_USERNAME = 'test'
        SPRING_DATASOURCE_PASSWORD = 'test'
        SPRING_REDIS_HOST = 'localhost'
        SPRING_REDIS_PORT = '6379'
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
                echo 'Deploying to production environment...'
                sh 'docker-compose -f docker-compose.prod.yml up -d'
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
