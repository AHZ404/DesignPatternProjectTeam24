# Non-Profit Organization Management System

A Java Swing desktop application designed for non-profit organizations to manage **Beneficiary Assistance**, **Donations**, and **Resource Inventory**. The system features full CRUD operations, automated inventory synchronization from donations, payment strategy implementations, and local persistence powered by a thread-safe JSON object store.

---

## 🛠️ Tech Stack & Key Concepts

- **Language:** Java 17+
- **GUI Framework:** Java Swing (`JFrame`, `JTabbedPane`, `JSplitPane`, `JTable`)
- **Persistence:** Local JSON file storage via Jackson (`jackson-databind`)
- **Architecture:** Model-View-Controller (MVC) Pattern
- **Design Patterns:** Strategy Pattern (Payment Processing), Singleton Pattern (Inventory Management), Thread-Safe Repository Pattern

---

## 📁 Package & Directory Structure

```text
src/
├── database/                   # Central Persistence Layer
│   ├── Database.java           # In-memory data store containing all domain maps & ID counters
│   └── JsonRepository.java     # Thread-safe read/write coordinator for database.json
├── beneficiary/                # Beneficiary & Inventory Module
│   ├── controller/
│   │   └── BeneficiaryController.java
│   ├── model/
│   │   ├── Beneficiary.java
│   │   ├── NeedAssessment.java
│   │   ├── AssistanceRequest.java
│   │   ├── Resource.java
│   │   ├── ResourceDistribution.java
│   │   ├── ServiceRecord.java
│   │   ├── ImpactRecord.java
│   │   └── CrudMatrix.java
│   ├── service/
│   │   ├── BeneficiaryService.java
│   │   └── InventoryManager.java
│   └── view/
│       └── BeneficiaryPanel.java
├── donation/                   # Donation & Payment Module
│   ├── controller/
│   │   └── DonationController.java
│   ├── model/
│   │   ├── Donor.java
│   │   ├── Donation.java
│   │   ├── IPaymentStrategy.java
│   │   ├── CashPayment.java
│   │   ├── CreditCardPayment.java
│   │   ├── DebitCardPayment.java
│   │   ├── CheckPayment.java
│   │   └── InKindPayment.java
│   ├── service/
│   │   └── DonationService.java
│   └── view/
│       └── DonationPanel.java
└── Main.java                   # Application entry point & shared repository wiring

```

## 🗄️ Database & Persistence Model

All application data is centralized inside a single file located at `data/database.json`.

### 1. Centralized ID Sequence
Human user profiles share a unified sequential counter (`nextUserId`) to ensure clean sequential identification:
- **Donors & Beneficiaries:** Shared `nextUserId` counter (IDs: 1, 2, 3, 4...).
- **Transactions & Inventory:** `nextId` and `nextDonationId` manage internal inventory allocations and donation records.

### 2. Automated Inventory Integration
When an **In-Kind** or **Financial Donation** is processed:
1. `DonationService` validates and processes the transaction via the selected `IPaymentStrategy`.
2. Upon success, the service automatically injects the contributed items or financial funds directly into the `Resource` inventory managed by `BeneficiaryService`.
3. Inventory items immediately become available for distribution to beneficiaries.

---

## 💳 Payment Strategy Implementations

The `donation.model` package implements the **Strategy Pattern** for handling diverse payment methods:

| Strategy Class | Description |
| :--- | :--- |
| `CashPayment` | Processes direct cash contributions. |
| `CreditCardPayment` | Captures card transaction IDs. |
| `DebitCardPayment` | Captures debit authorization IDs. |
| `CheckPayment` | Stores check numbers and bank issuing details. |
| `InKindPayment` | Accepts physical items (e.g., Clothing, Food, Supplies) with unit quantities and estimated valuations. |

---

## 🚀 How to Run the Application

### Prerequisites
- JDK 17 or higher installed
- Maven or Gradle (or IDE library importer for Jackson dependencies)

### Required Jackson Dependencies
If using **Maven** (`pom.xml`), ensure `jackson-databind` is included:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.0</version>
</dependency>
```
### Running the Application

1. **Desktop GUI Mode (Default):**
   ```bash
   javac -cp "libs/*" -d out src/**/*.java Main.java
   java -cp "out:libs/*" Main
   ```
   
2. **Console Strategy Demo Mode**
   To run the payment strategy demonstration directly in terminal without launching the Swing GUI:
   ```bash
    java -cp "out:libs/*" Main --donation-demo
   ```

## 🖥️ Feature Walkthrough

### 1. Donor & Donation Management Tab
- **Register Donors:** Capture donor name, contact details, and Tax ID/SSN.
- **Record Donations:** Select a registered donor and choose a payment strategy (Cash, Credit Card, Debit Card, Check, In-Kind).
- **Print Official Receipts:** Generate structured receipt documents detailing payment method, donor info, and item breakdown for in-kind gifts.
- **Annual Tax Deduction Statements:** Produce consolidated annual tax statements for individual donors showing total tax-deductible contributions for any selected tax year.

### 2. Beneficiary Management Tab
- **Register & Search Beneficiaries:** Search by name, ID, or contact number. Change assistance status.
- **Needs Assessments & Assistance Requests:** Log resource or service requests linked to specific beneficiaries.
- **Inventory & Aid Distribution:** Deduct resources from inventory to fulfill active assistance requests.
- **Services & Impact Tracking:** Record delivered program outcomes and generate historical impact summaries.
- **CRUD Matrix View:** Inspect module requirements mapped against system entities.