# Akriti Adeshwar — Spring Boot Backend

## Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 16

## Local Setup

### 1. Create PostgreSQL database
```sql
CREATE DATABASE akriti_db;
```

### 2. Configure environment variables (or update application.properties)
```
DB_URL=jdbc:postgresql://localhost:5432/akriti_db
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=akriti-adeshwar-super-secret-key-2025-minimum-256-bits-long
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
TWILIO_WHATSAPP_FROM=whatsapp:+14155238886
```

### 3. Run the application
```bash
mvn spring-boot:run
```

Server starts at: http://localhost:8080

On first startup, DataSeeder automatically seeds all 40 flats into the database.

## API Endpoints

### Auth
- POST /api/auth/admin/login     — Admin login
- POST /api/auth/send-otp        — Send OTP to resident phone
- POST /api/auth/verify-otp      — Verify OTP and get JWT token

### Dashboard
- GET /api/dashboard             — Full dashboard summary
- GET /api/dashboard/trend       — 6-month trend data

### Maintenance
- GET  /api/maintenance          — All payments for a month (?month=3&year=2025)
- GET  /api/maintenance/summary  — Month summary
- GET  /api/maintenance/flat/{flatNo} — Flat payment history
- POST /api/maintenance/flat/{flatNo}/pay?month=3&year=2025 — Mark paid (resident)
- POST /api/maintenance/reminders — Send WhatsApp reminders (admin)
- POST /api/maintenance/generate-dues — Trigger due generation (admin)

### Complaints
- GET   /api/complaints          — All complaints
- GET   /api/complaints/flat/{flatNo} — Flat's complaints
- POST  /api/complaints          — Raise complaint (resident)
- PATCH /api/complaints/{id}/status  — Update status (admin)

### Announcements
- GET    /api/announcements      — All / filtered by role
- POST   /api/announcements      — Create + send WhatsApp (admin)
- PATCH  /api/announcements/{id}/pin — Pin/unpin (admin)
- DELETE /api/announcements/{id} — Delete (admin)

### Expenses
- GET    /api/expenses           — By month
- GET    /api/expenses/summary   — Summary with category breakdown
- POST   /api/expenses           — Add expense (admin)
- DELETE /api/expenses/{id}      — Delete (admin)

### Visitors
- GET   /api/visitors            — All / ?todayOnly=true / ?flatNo=1A
- GET   /api/visitors/stats      — Count stats
- POST  /api/visitors            — Log entry (admin)
- PATCH /api/visitors/{id}/checkout — Check out (admin)

### Flats
- GET /api/flats                 — All flats
- GET /api/flats/{flatNo}        — Single flat
- PUT /api/flats/{flatNo}        — Update flat details (admin)

## WebSocket Topics
Connect to: ws://localhost:8080/ws (SockJS)

Subscribe to:
- /topic/payments     — payment marked paid by resident
- /topic/complaints   — new complaint or status change
- /topic/announcements — new announcement posted
- /topic/visitors     — new visitor logged

## Simulate Mode
By default app.otp.simulate=true — OTPs are logged to console only.
Set app.otp.simulate=false and configure Twilio credentials for real WhatsApp delivery.

## Railway Deployment
Set these environment variables in Railway:
- DB_URL (auto-set by Railway PostgreSQL plugin)
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET
- TWILIO_ACCOUNT_SID
- TWILIO_AUTH_TOKEN
- CORS_ORIGINS=https://your-vercel-app.vercel.app
