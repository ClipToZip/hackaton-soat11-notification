# 📧 Notification Service

Serviço responsável por **consumir mensagens de uma fila SQS** e **enviar emails** com base no conteúdo recebido.

> ⚠️ **Importante**
>
> - Este serviço **NÃO cria filas SQS em produção**
> - A criação da fila é responsabilidade da **infraestrutura (Terraform / CloudFormation / outro time)**
> - O serviço apenas **escuta** e **processa mensagens**

---

## 🧱 Arquitetura

[SQS] ➜ [notification-service] ➜ [SMTP / Email]

---

## 🚀 Execução LOCAL (LocalStack)

### Pré-requisitos
- Docker
- Docker Compose
- PowerShell (Windows) ou terminal compatível

---

### 1️⃣ Subir o ambiente

```powershell
docker compose up -d
```

Verifique:
```powershell
docker compose ps
```

---

### 2️⃣ Criar a fila SQS (somente local)

```powershell
docker exec -it localstack awslocal sqs create-queue `
  --queue-name cliptozip-notifications
```

---

### 3️⃣ Mensagem de teste (`msg.json`)

```json
{
  "titulo": "Teste local antes de ir pra AWS",
  "status": "APROVADO",
  "mensagem": "Último teste funcionando",
  "emailUsuario": "samuel.videira@gmail.com",
  "nomeUsuario": "Samuel"
}
```

---

### 4️⃣ Enviar mensagem (FORMA CORRETA NO POWERSHELL)

```powershell
$body = Get-Content .\msg.json -Raw `
        | ConvertFrom-Json `
        | ConvertTo-Json -Compress

docker exec -i localstack awslocal sqs send-message `
  --queue-url http://localhost:4566/000000000000/cliptozip-notifications `
  --message-body "$body"
```

---

### 5️⃣ Logs

```powershell
docker compose logs -f notification-service
```

---

## ☁️ Execução em AWS (Produção)

### 📌 Responsabilidades

| Item | Responsável |
|----|----|
| Criar fila SQS | Infra |
| Criar DLQ | Infra |
| Permissões IAM | Infra |
| Consumir mensagens | notification-service |

---

### Permissões IAM mínimas

```json
{
  "Effect": "Allow",
  "Action": [
    "sqs:ReceiveMessage",
    "sqs:DeleteMessage",
    "sqs:GetQueueAttributes",
    "sqs:GetQueueUrl"
  ],
  "Resource": "arn:aws:sqs:REGIAO:ACCOUNT_ID:cliptozip-notifications"
}
```

---

### Variáveis de ambiente (AWS)

```env
SPRING_PROFILES_ACTIVE=prod
AWS_REGION=us-east-1
APP_SQS_QUEUE_NAME=cliptozip-notifications

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=senha-ou-app-password
```

---

## 📩 Contrato da Mensagem SQS

```json
{
  "titulo": "string",
  "status": "string",
  "mensagem": "string",
  "emailUsuario": "string",
  "nomeUsuario": "string"
}
```

---

