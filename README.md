# Non-Profit Organization Management System

A Java Swing desktop application designed for non-profit organizations to manage **Beneficiary Assistance**, **Donations**, **Volunteers**, and **Resource Inventory**. The system has a login screen with a separate interface for each user type (**Admin, Donor, Volunteer, Beneficiary**), full CRUD operations, automated inventory synchronization from donations, payment strategy implementations, and local persistence powered by a single, thread-safe JSON database.

---

## 🛠️ Tech Stack & Key Concepts

- **Language:** Java 17+
- **GUI Framework:** Java Swing (`JFrame`, `JTabbedPane`, `JSplitPane`, `JTable`)
- **Persistence:** Local JSON file storage via Jackson (`jackson-databind`)
- **Architecture:** Model-View-Controller (MVC) Pattern
- **Design Patterns:** Strategy, Decorator, Factory, Singleton, Observer, Repository and MVC (see the [Design Pattern Map](#-design-pattern-map))

---

## 🧩 Design Pattern Map

| Pattern | Module | Classes | What it does in the app |
| :--- | :--- | :--- | :--- |
| **Strategy** | Donation | `IPaymentStrategy` (strategy interface); `CashPayment`, `CreditCardPayment`, `DebitCardPayment`, `CheckPayment`, `InKindPayment` (concrete strategies); `Donation` (context, can swap via `setPaymentStrategy()`) | Each donation is processed by the payment method the donor picks in **+ Record Donation**. |
| **Decorator** | Donation | `PaymentDecorator` (abstract decorator, wraps any `IPaymentStrategy`); `FeeCoveredPayment` (concrete decorator); applied in `DonationPanel` | For **card** donations the donor can choose to cover a 3% processing fee. The decorator adds the fee before calling the wrapped card payment and appends "(+3% fee covered)" to the method name, without changing any payment class. |
| **Factory** | Volunteer | `VolunteerTaskFactory.createTask(type, ...)` builds `FundraiserTask`, `WorkshopTask` or `OutreachTask` (subclasses of abstract `VolunteerTask`); called by `VolunteerService` | Used when the admin creates a task in **Volunteer Management → Tasks**. |
| **Singleton** | Database | `JsonRepository`: private constructor + `JsonRepository.getInstance()` | Exactly one database object is created (from `data/database.json`) and shared by every module and every login session. |
| **Observer** | Database + all UI panels | Subject: `JsonRepository` (`registerObserver`, `removeObserver`, `notifyObservers`). Observer interface: `DatabaseObserver` (`databaseChanged()`). Concrete observers: `DonationPanel`, `BeneficiaryPanel`, `VolunteerPanel`. Wiring: `Main.observe(...)` | After every saved change the database notifies all open screens, which refresh themselves. Example: an in-kind donation appears immediately in **Beneficiary Management → Inventory & Aid**. Screens unregister when their window closes or the user logs out. |
| **Repository** | Volunteer | `VolunteerRepository` (interface), `JsonVolunteerRepository` (implementation over `JsonRepository`) | Volunteer business logic does not depend on how data is stored. |
| **MVC** | Donation, Beneficiary, Volunteer | `model/` classes, `view/*Panel`, `controller/*Controller`, `service/*Service` | Views only call controllers; controllers delegate to services; services read/write the database. |

> The full class diagram of the project is in `UML.drawio` (editable in draw.io) and `UML Image.png`.

---

## 👥 User Types & Interfaces

On start-up a **Login** dialog asks which user type you are. Every window has a **Log out** button that returns to the login screen.

| User type | How to log in | What they see | CRUD |
| :--- | :--- | :--- | :--- |
| **Admin** | Password `admin` (constant `ADMIN_PASSWORD` in `Main.java`) | All three management modules as tabs: Donation, Beneficiary, Volunteer | Full CRUD on donors, donations, beneficiaries, needs, requests, resources, distributions, services, impact, volunteers, tasks, assignments and hours |
| **Donor** | Pick your donor account or **+ Create new account** | **My Donor Account**: own profile and own donations only | **C** record a donation · **R** own donations, receipts, tax statement · **U** edit profile · **D** delete account |
| **Volunteer** | Pick your volunteer account or **+ Create new account** | **My Profile**, **My Assignments & Hours**, **History & Certificates** (own data only) | **C** add skills / availability, log hours · **R** own assignments & history · **U** edit profile, accept / decline assignments · **D** remove skills / slots, deactivate account |
| **Beneficiary** | Pick your beneficiary account or **+ Create new account** | **My Profile** and **Needs & Requests** (own data only) | **C** needs assessments, resource / service requests · **R** own requests & assistance history · **U** edit profile · **D** cancel requests, delete account |

Each personal interface reuses the same panel class as the admin view, with an optional user ID (`DonationPanel(controller, donorId)`, `VolunteerPanel(controller, volunteerId)`, `BeneficiaryPanel(controller, beneficiaryId)`) that filters the data and hides admin-only buttons.

---

## 📁 Package & Directory Structure

```text
src/
├── database/                   # Central Persistence Layer
│   ├── Database.java           # In-memory data store containing all domain maps & ID counters
│   ├── JsonRepository.java     # Singleton + Observer subject; thread-safe read/write of database.json
│   ├── DatabaseObserver.java   # Observer interface implemented by the UI panels
│   └── JavaTimeJsonModule.java # Saves LocalDate / LocalTime / LocalDateTime as text (no extra library)
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
│   ├── repository/
│   │   └── BeneficiaryRepository.java  # legacy storage, not used by the app
│   ├── service/
│   │   ├── BeneficiaryService.java
│   │   └── InventoryManager.java       # legacy singleton, not used by the app
│   └── view/
│       └── BeneficiaryPanel.java       # Observer; admin view or a single beneficiary's view
├── donation/                   # Donation & Payment Module
│   ├── controller/
│   │   └── DonationController.java
│   ├── model/
│   │   ├── Donor.java
│   │   ├── Donation.java               # Strategy context
│   │   ├── IPaymentStrategy.java       # Strategy interface
│   │   ├── CashPayment.java
│   │   ├── CreditCardPayment.java
│   │   ├── DebitCardPayment.java
│   │   ├── CheckPayment.java
│   │   ├── InKindPayment.java
│   │   ├── PaymentDecorator.java       # Decorator (abstract)
│   │   └── FeeCoveredPayment.java      # Decorator (concrete): donor covers 3% card fee
│   ├── service/
│   │   └── DonationService.java
│   └── view/
│       └── DonationPanel.java          # Observer; admin view or a single donor's view
├── volunteer/                  # Volunteer Module (Factory Pattern)
│   ├── controller/
│   │   └── VolunteerController.java
│   ├── model/
│   │   ├── Volunteer.java, Skill.java, AvailabilitySlot.java
│   │   ├── VolunteerTask.java (abstract), FundraiserTask.java, WorkshopTask.java, OutreachTask.java
│   │   ├── VolunteerAssignment.java, VolunteerHourRecord.java, VolunteerCertificate.java
│   │   └── TaskType, TaskStatus, AssignmentStatus, ApprovalStatus, VolunteerStatus, SkillLevel, CertificateType
│   ├── service/
│   │   ├── VolunteerTaskFactory.java      # Factory Pattern
│   │   ├── VolunteerService.java
│   │   ├── VolunteerRepository.java       # Repository interface
│   │   ├── JsonVolunteerRepository.java   # saves to data/database.json
│   │   └── VolunteerNotifier.java, ConsoleVolunteerNotifier.java
│   └── view/
│       └── VolunteerPanel.java            # Observer; admin view or a single volunteer's view
├── user/model/                 # User base class & EventCoordinator (used as the admin/coordinator user)
└── Main.java                   # Entry point: login screen, one window per user type, Observer wiring
```

---

## 🗄️ Database & Persistence Model

All application data is centralized inside a single file located at `data/database.json`, managed by the **Singleton** `JsonRepository.getInstance()`. Every change is saved to disk immediately and then broadcast to the open screens (**Observer**).

### 1. Centralized ID Sequence
Human user profiles share a unified sequential counter (`nextUserId`) to ensure clean sequential identification:
- **Donors, Beneficiaries & Volunteers:** Shared `nextUserId` counter (IDs: 1, 2, 3, 4...).
- **Volunteer records** (skills, availability slots, tasks, assignments, hours, certificates) use `nextId`.
- **Transactions & Inventory:** `nextId` and `nextDonationId` manage internal inventory allocations and donation records.

### 2. Automated Inventory Integration
When an **In-Kind** or **Financial Donation** is processed:
1. `DonationService` validates and processes the transaction via the selected `IPaymentStrategy` (possibly wrapped by `FeeCoveredPayment`).
2. Upon success, the service automatically injects the contributed items or financial funds directly into the `Resource` inventory managed by `BeneficiaryService`.
3. Inventory items immediately become available for distribution to beneficiaries, and the inventory screen refreshes automatically.

### 3. Reliable Reloading
All saved classes (donations, payment methods including decorated ones, needs assessments, requests, distributions, services and impact records) have a constructor Jackson can use, so `database.json` loads correctly on every start.

---

## 💳 Payment Strategy & Decorator

The `donation.model` package implements the **Strategy Pattern** for handling diverse payment methods:

| Strategy Class | Description |
| :--- | :--- |
| `CashPayment` | Processes direct cash contributions. |
| `CreditCardPayment` | Captures card transaction IDs. |
| `DebitCardPayment` | Captures debit authorization IDs. |
| `CheckPayment` | Stores check numbers and bank issuing details. |
| `InKindPayment` | Accepts physical items (e.g., Clothing, Food, Supplies) with unit quantities and estimated valuations. |

On top of these, the **Decorator Pattern** adds optional behaviour without changing the strategy classes:

| Decorator Class | Wraps | Description |
| :--- | :--- | :--- |
| `PaymentDecorator` | any `IPaymentStrategy` | Abstract base that forwards `ProcessPayment()` and `getMethodName()` to the wrapped payment. |
| `FeeCoveredPayment` | `CreditCardPayment`, `DebitCardPayment` | Charges the donation amount **plus 3%** so the charity receives the full donation; shown as e.g. "Credit Card (+3% fee covered)" in the table and receipt. |

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

1. **Desktop GUI Mode (Default):** compile, then run.
   ```bash
   javac -cp "libs/*" -d out $(find src -name "*.java")
   ```
   ```bash
   java -cp "out:libs/*" Main
   ```
   (On Windows use `;` instead of `:` in the classpath.)

2. **Console Strategy Demo Mode:** runs the payment strategy demonstration in the terminal without launching the Swing GUI.
   ```bash
   java -cp "out:libs/*" Main --donation-demo
   ```

---

## 🖥️ Feature Walkthrough

### 0. Login & User Interfaces
- **Login:** choose Admin (password `admin`), Donor, Volunteer or Beneficiary; non-admin users pick their account or create a new one.
- **Per-user windows:** donors, volunteers and beneficiaries only see and edit their own data; the admin sees everything.
- **Log out:** returns to the login screen; the window's screens stop observing the database.

### 1. Donor & Donation Management Tab
- **Register Donors:** Capture donor name, contact details, and Tax ID/SSN.
- **Record Donations:** Select a registered donor and choose a payment strategy (Cash, Credit Card, Debit Card, Check, In-Kind).
- **Cover the Processing Fee (Decorator):** for card donations, optionally add a 3% fee paid by the donor.
- **Print Official Receipts:** Generate structured receipt documents detailing payment method, donor info, and item breakdown for in-kind gifts.
- **Annual Tax Deduction Statements:** Produce consolidated annual tax statements for individual donors showing total tax-deductible contributions for any selected tax year.

### 2. Beneficiary Management Tab
- **Register & Search Beneficiaries:** Search by name, ID, or contact number. Change assistance status.
- **Needs Assessments & Assistance Requests:** Log resource or service requests linked to specific beneficiaries.
- **Inventory & Aid Distribution:** Deduct resources from inventory to fulfill active assistance requests.
- **Services & Impact Tracking:** Record delivered program outcomes and generate historical impact summaries.
- **CRUD Matrix View:** Inspect module requirements mapped against system entities.

### 3. Volunteer Management Tab
- **Volunteers:** Register, update, search (name, email, phone, skill) and deactivate volunteers; manage skills and weekly availability.
- **Tasks:** Create Fundraiser, Workshop and Outreach tasks through `VolunteerTaskFactory`, optionally linked to an event.
- **Assignments & Hours:** Assign volunteers only when they have the required skills, are available and have no overlapping task; volunteers accept or decline; hours are logged and approved by a coordinator (never by the volunteer).
- **History & Certificates:** View each volunteer's assignments and approved hours; issue participation or hours-milestone certificates.

---

## ✅ Testing the Patterns in the UI

- **Observer:** log in as Admin, record an **In-Kind** donation in Donation Management, then open **Beneficiary Management → Inventory & Aid**. The new item is already listed; the screen was refreshed by `databaseChanged()`, not by clicking the tab.
- **Singleton:** the console prints "Successfully loaded persistent database" only once, even after logging out and in as other users; data entered by one user type is visible to the others and survives a restart.
- **Decorator:** record a **Credit Card** donation of 100 and answer **Yes** to the fee question. The Payment Method shows "Credit Card (+3% fee covered)" and the console shows a $103.00 charge. Cash, Check and In-Kind donations are not offered the fee.
- **Strategy:** record donations with each payment method; each one is processed by its own strategy class.
- **Factory:** create a Fundraiser, Workshop and Outreach task; the Type and Details columns show the class built by the factory.
