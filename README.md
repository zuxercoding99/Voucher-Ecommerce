# 🎫 Voucher E-Commerce -- Backend (Java + Spring Boot)

![Java](https://img.shields.io/badge/Java-17-blue)
![SpringBoot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Status](https://img.shields.io/website?url=https%3A%2F%2Fvoucher-ecommerce.onrender.com)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![PostgreSQL](https://img.shields.io/badge/DB-PostgreSQL-336791)

Backend REST para la compra y gestión de **vouchers (cupones
digitales)**.\
Incluye integración real con **Mercado Pago**, manejo de **webhooks**,
control de stock, expiración automática, activación/uso de vouchers,
usuarios con roles, y deploy en la nube con Docker.

---

## 🌐 **Deploy**

➡️ Backend desplegado en Render (Docker):\
**https://voucher-ecommerce.onrender.com**

Repositorio:\
**https://github.com/zuxercoding99/Voucher-Ecommerce**

---

# 📌 **Descripción general**

Sistema backend de e-commerce donde:

- **ADMIN** publica vouchers (stock, precio, descripción).
- **USUARIO** compra vouchers con **Mercado Pago** o método **FAKE**
  para test.
- El pago activa automáticamente el voucher.
- Los vouchers expiran si no se pagan en 15 minutos.
- Las compras expiradas que luego reciben pago → generan _refund
  pendiente_.
- El usuario puede **activar** un voucher, y luego el **admin lo marca
  como usado**.

Este proyecto está enfocado en **backend profesional**, con reglas
reales de negocio.

---

# 🧠 **Arquitectura del flujo de compra**

### **1️⃣ Creación de compra**

- Valida stock.
- Reserva el voucher y reduce el stock.
- Genera una preferencia de Mercado Pago.
- Crea un Payment en `PENDING`.
- Asigna `external_reference` = ID interno de la compra.

### **2️⃣ Webhook actualiza el estado**

| Estado MP                 | Acción del sistema                  |
| ------------------------- | ----------------------------------- |
| **approved**              | Activa voucher (si no expiró)       |
| **rejected**              | Devuelve stock                      |
| **pending**               | No cambia nada                      |
| **paid after expiration** | Marca Payment como `PENDING_REFUND` |

### **3️⃣ Expiración automática**

Cada 15 minutos (cron job): - Si la compra no se pagó → se marca
`EXPIRED`. - Se restaura el stock. - Evita que compras abandonadas
bloqueen inventario.

### **4️⃣ Activación del voucher (usuario)**

- Un voucher disponible (`AVAILABLE`) debe ser activado manualmente
  (`ACTIVATED`).

### **5️⃣ Uso del voucher (admin)**

- Un admin puede marcar como `USED`.

---

# 🧱 **Modelo de entidades**

- **User** --- autenticación + roles (ADMIN / USER)\
- **Voucher** --- precio, stock, descripción\
- **Purchase** --- registro completo de la compra + timestamps
  (created, expired, used...)\
- **Payment** --- información del pago real + estado\
- **VoucherStatus** --- `AVAILABLE`, `ACTIVATED`, `USED`, `CANCELLED`,
  `EXPIRED`\
- **PaymentStatus** --- `PENDING`, `APPROVED`, `REJECTED`,
  `PENDING_REFUND`, `REFUNDED`

Incluye auditoría automática con `@EnableJpaAuditing`.

---

# 📡 **Webhook de Mercado Pago**

    POST /api/payments/webhook

Procesa:

- pagos aprobados
- pagos rechazados
- pagos pendientes
- pagos tardíos (compra ya expirada)
- idempotencia (evita procesar dos veces el mismo evento)

---

# 🛠 **Tech Stack**

- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA\
- Mercado Pago Java SDK
- PostgreSQL (prod) / H2 (test)
- Docker
- Render (deploy cloud)
- Cron Jobs con Spring Scheduler

---

# 🚀 **Cómo probar la API (guía rápida con curl)**

Puede tomar unos minutos arrancar el deploy

## **1️⃣ Login como ADMIN**

```bash
curl -s -X POST https://voucher-ecommerce.onrender.com/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"admin@system.local","password":"admin1234"}'
```

Guardar:

    ADMIN_TOKEN="eyJ..."

## **2️⃣ Crear voucher (ADMIN)**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/vouchers   -H "Content-Type: application/json"   -H "Authorization: Bearer $ADMIN_TOKEN"   -d '{
    "description": "Depilación rostro",
    "price": 1000,
    "stock": 5
  }'
```

Guardar:

    VOUCHER_ID=1

## **3️⃣ Registrar usuario**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/auth/register   -H "Content-Type: application/json"   -d '{"username":"user123","email":"user123@gmail.com","password":"string","birthDate":"2000-11-27"}'
```

## **4️⃣ Login como usuario**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/auth/login   -H "Content-Type: application/json"   -d '{"email":"user123@gmail.com","password":"string"}'
```

Guardar:

    USER_TOKEN="eyJ..."

## **5️⃣ Comprar un voucher**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/purchases   -H "Authorization: Bearer $USER_TOKEN"   -H "Content-Type: application/json"   -d "{"voucherId": $VOUCHER_ID, "paymentMethod": "MERCADOPAGO"}"
```

## **6️⃣ Ver compras del usuario**

```bash
curl https://voucher-ecommerce.onrender.com/api/purchases/me   -H "Authorization: Bearer $USER_TOKEN"
```

## **7️⃣ Activar un voucher**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/purchases/$PURCHASE_ID/activate   -H "Authorization: Bearer $USER_TOKEN"
```

## **8️⃣ Marcar como usado (ADMIN)**

```bash
curl -X POST https://voucher-ecommerce.onrender.com/api/purchases/$PURCHASE_ID/used   -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

# 👨‍💻 Sobre mí

Desarrollador Backend especializado en **Java + Spring Boot**.

📧 **Contacto:**\
**zkcoding99@gmail.com**
