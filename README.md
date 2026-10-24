# 💳 OliveBank – DevSecOps Banking Application on Azure App Service

OliveBank is a fully containerized, monitored, secured, and auto-remediated banking application deployed on **Azure App Service**. This project was built to demonstrate **end-to-end DevSecOps practices** with real-world tooling, CI/CD, monitoring, alerting, and secure architecture.

---

## 📌 Table of Contents

* [🎯 Project Objectives](#-project-objectives)
* [🧱 Tech Stack](#-tech-stack)
* [⚙️ Architecture Overview](#️-architecture-overview)
* [🚀 CI/CD Pipeline](#-cicd-pipeline)
* [🔐 DevSecOps Integrations](#-devsecops-integrations)
* [📊 Monitoring & Alerting](#-monitoring--alerting)
* [⚒️ Self-Healing Setup](#️-self-healing-setup)
* [🌐 Application Features](#-application-features)
* [📂 Repository Structure](#-repository-structure)
* [🛠️ How to Deploy](#️-how-to-deploy)
* [🧠 Lessons Learned](#-lessons-learned)
* [📸 Screenshots](#-screenshots)
* [📃 License](#-license)

---

## 🎯 Project Objectives

* Simulate a real-world online banking platform.
* Implement complete CI/CD with GitHub Actions.
* Apply DevSecOps practices: code scanning, container scanning, and runtime security.
* Set up monitoring dashboards with Prometheus + Grafana.
* Enable auto-remediation through monitoring alerts.
* Demonstrate separation of CI and CD responsibilities.

---

## 🧱 Tech Stack

### 💻 Application

* **Frontend:** Angular + NGINX
* **Backend:** Spring Boot (Java)
* **Database:** Azure MySQL

### 🐳 Containerization

* Docker
* Docker Compose (for local testing)

### ☁️ Cloud & Infrastructure

* **Azure App Service**
* Azure Container Registry (ACR)
* Azure MySQL Flexible Server (private access)
* Terraform (Infrastructure as Code)

### ⚙️ CI/CD

* GitHub Actions (CI/CD)

### 🔒 Security

* Trivy (container scanning)
* SonarCloud (code scanning)
* OWASP ZAP (optional for dynamic scanning)

### 📈 Monitoring

* Prometheus
* Grafana
* Alerting
* Slack & Email Alerting

---

## ⚙️ Architecture Overview

```text
+----------------+     Git Push      +------------------------+
|   Developer    +----------------->| GitHub Repo (CI/CD)    |
+----------------+                  +-----------+------------+
                                                |
                                                v
                            +---------------------------+
                            | GitHub Actions (CI)       |
                            | - Lint, test, build       |
                            | - Trivy + SonarCloud scan |
                            | - Push image to ACR       |
                            +-------------+-------------+
                                          |
                                          v
                                  +------------------+
                                  |   ACR (Docker)   |
                                  +--------+---------+
                                           |
                                           v
                            +---------------------------+
                            |    Azure App Service     |
                            |    Container Hosting     |
                            +------------+--------------+
                                         |
                     +-------------------+-------------------+
                     |                                       |
                     v                                       v
          +--------------------------+           +--------------------------+
          | Frontend (Angular+NGINX) | <-------> | Backend (Spring Boot API)|
          +--------------------------+           +------------+-------------+
                                                              |
                                                              v
                                                   +----------------------+
                                                   |     Azure MySQL      |
                                                   +----------------------+

Supporting Services:
- Terraform for infrastructure provisioning
- Prometheus + Grafana for monitoring, where configured
- Alerting through supported notification integrations
```

---

## 🚀 CI/CD Pipeline

### ✅ CI – GitHub Actions

Runs on commits and pull requests.

Performs:

* Code linting
* Unit tests
* SonarCloud static analysis
* Trivy vulnerability scanning
* Docker image build and push to ACR

### 🔁 CD – Azure App Service

Deployment process:

* GitHub Actions deploys the application to Azure App Service.
* App Service pulls the configured container image from Azure Container Registry.
* Updated application versions are deployed to the App Service environment.
* Application health is verified after deployment.

---

## 🔐 DevSecOps Integrations

| Tool                   | Purpose                                                         |
| ---------------------- | --------------------------------------------------------------- |
| Trivy                  | Scans Docker images for CVEs                                    |
| SonarCloud             | Analyzes source code for bugs, vulnerabilities, and code smells |
| OWASP ZAP              | Optional dynamic security scanning                              |
| Email & Discord Alerts | Notify on critical events                                       |

---

## 📊 Monitoring & Alerting

Prometheus and Grafana can be configured to monitor application metrics and performance.

Grafana dashboards can display:

* Application health
* CPU and memory metrics where available
* API response metrics
* Application performance

Prometheus scrapes configured application metrics and supported infrastructure metrics.

Monitoring alerts can notify through:

* Email
* Slack webhook

Alerts can also trigger an appropriately configured remediation workflow.

---

## ⚒️ Self-Healing Setup

Monitoring alerts can detect application failures or degraded performance.

A configured remediation workflow can:

* Restart an unhealthy App Service application where appropriate.
* Scale the application according to configured rules and supported hosting-plan capabilities.
* Collect diagnostic information.
* Notify the team about failures.

💡 You can test the monitoring setup by simulating an application failure or making a health endpoint unavailable in a test environment.

---

## 🌐 Application Features

| Feature              | Description                                                |
| -------------------- | ---------------------------------------------------------- |
| Register/Login       | Secure authentication with hashed passwords and PIN        |
| Deposit/Withdraw     | Updates account balance                                    |
| Transaction Logs     | Viewable from the frontend through `/api` endpoints        |
| Application Routing  | Routes requests between the frontend and backend           |
| Live Monitoring      | View application and API performance on Grafana dashboards |
| Secure Communication | Routes backend API requests through the configured proxy   |

---

## 🛠️ How to Deploy

### Prerequisites

* Azure CLI and Terraform installed
* GitHub repository with the required secrets and variables configured
* Docker installed
* Azure App Service, Azure Container Registry, and Azure MySQL provisioned

### 1️⃣ Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd SecureBank
```

### 2️⃣ Configure Terraform

Configure your `dev.tfvars` file in the appropriate environment directory.

Initialize Terraform:

```bash
cd infra/components
terraform init
terraform apply -var-file="../env/dev.tfvars"
```

Review your Terraform plan and Azure resource configuration before applying changes.

### 3️⃣ Push code to GitHub

The CI pipeline will:

* Build and test the application.
* Run Trivy and SonarCloud scans.
* Build and push Docker images to Azure Container Registry.

### 4️⃣ Deploy to Azure App Service

The deployment workflow will:

* Authenticate with Azure.
* Deploy the configured container image from ACR to Azure App Service.
* Update the application to the new version.
* Verify application health.

### 5️⃣ Verify the application

* Open the Azure App Service URL.
* Test registration and login.
* Verify deposit, withdrawal, and transaction history functionality.
* Confirm database connectivity.
* Check monitoring dashboards and configured alerts.

---

## 🧠 Lessons Learned

* Deep understanding of CI/CD separation.
* Implementing automated deployments with GitHub Actions.
* Setting up security scanning with Trivy and SonarCloud.
* Configuring monitoring and alert notifications.
* Handling application routing and proxy configuration.
* Connecting Azure App Service securely to Azure MySQL.
* Provisioning cloud infrastructure with Terraform.
* Creating a realistic DevSecOps pipeline from scratch.

---

## 📸 Screenshots

Add screenshots of your implementation, such as:

* SecureBank application homepage
* Registration and login pages
* Banking dashboard and transaction history
* Azure App Service deployment
* Azure Container Registry images
* GitHub Actions pipeline
* SonarCloud analysis
* Trivy vulnerability scan
* Grafana monitoring dashboard

---

## 📃 License

MIT License. Feel free to fork, use, or contribute.

---

## 🙌 Thanks

This project is part of my journey to build advanced, real-world DevOps solutions and share them publicly.

Questions or feedback?

Connect with me on LinkedIn or reach out in the comments.
