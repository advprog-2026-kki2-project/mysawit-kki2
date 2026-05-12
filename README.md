## I. Technical Design Decisions
1. Language & Framework
<br>Java with the Spring Boot framework.
<br>Spring Boot's modular architecture is ideal for enterprise systems requiring strict Role-Based Access Control (RBAC) and complex module integrations like payroll and logistics.


3. Architecture & Resilience
<br>Architecture: Modular Monolith with a clear separation of responsibilities.
<br>Surges and Outages: To handle peak reporting hours, the system will use Asynchronous Processing for payroll. Database transactions will be strictly enforced to ensure data integrity during hardware or network outages.


4. Non-Functional Testing Tools
<br>Static Analysis: SonarCloud and Linters for code quality metrics.
<br>Functional Testing: Selenium or Serenity BDD for user story validation.
<br>Performance: Lighthouse for frontend and JMeter for load testing during activity surges.
<br>Usability & Monitoring: Clarity for UX testing and Observability Dashboards to monitor system health.


5. Deployment
<br>Azure
<br>Due to delays in AWS license distribution, we are pivoting to Azure to ensure deployment readiness. We will utilize Docker containers to maintain environment parity across all stages.


## II. Project Timeline & PIC Distribution

Milestone 1: Preparation
<br>20 Feb 2026
- Assigning one module to each person:
  - Authentication: Davin
  - Plantation: Zahran
  - Harvest: Rani
  - Transport: Juan
  - Payment: Shelia
- Davin: Initialize the GitHub organization and created the CI/CD environment. 
- Shelia, Juan, Rani, Zahran : Created the design decision specifications in the README.


Milestone 2: 25% Progress
<br>Internal: 4 Mar 2026 | Official: 6 Mar 2026
- Davin (Auth): Define the RBAC security schema, implement account registration and login for Laborers, Foremen, and Drivers.
- Zahran (Plantation): Design the database schema for square-shaped coordinates and unique codes, Implement basic CRUD for plantations and code uniqueness validation.
- Rani (Harvest): Draft the Daily Harvest DTO and image storage interface for photo evidence, implement the "Record Daily Harvest" form with one-entry-per-day logic.
- Juan (Transport): Define the transport state machine (Loading, Transporting, Arrived) and implement the Truck Driver’s view of assigned deliveries.
- Shelia (Payment): Setup the CI/CD pipeline with a dummy deployment script and transition the Continuous Deployment (CD) to Azure for Students.


Milestone 3: 50% Progress
<br>Internal: 15 Apr 2026 | Official: 17 Apr 2026
- Davin (Auth): Implement Admin authority to assign Laborers to Foremen.
- Zahran (Plantation): Implement logic for assigning Foremen and Drivers to specific plantations.
- Rani (Harvest): Build the Foreman’s harvest approval/rejection interface.
- Juan (Transport): Implement Pickup Assignment logic with the 400kg capacity constraint.
- Shelia (Payment): Implement the base Payroll Calculation engine.


Milestone 4: 75% Progress
<br>Internal: 6 May 2026 | Official: 8 May 2026
- Davin (Auth): Integrate Automatic Notifications for assignment changes and status updates.
- Zahran (Plantation): Implement advanced filtering for plantations by Name, Code, and Foreman.
- Rani (Harvest): Build the Laborer's harvest history with status and date filtering.
- Juan (Transport): Implement Foreman verification for arrived deliveries (Approve/Reject).
- Shelia (Payment): Integrate the Sandbox Payment Gateway and create the Admin Payroll Dashboard.


Milestone 5: 100% Final
<br>Internal: 20 May 2026 | Official: 22 May 2026
- Davin (Auth): Finalize Secure Coding audit and session management testing.
- Zahran (Plantation): Optimize database queries for plantation coordinate lookups.
- Rani (Harvest): Optimize photo retrieval performance and image compression.
- Juan (Transport): Conduct full integration testing for the harvest-to-factory data flow.
- Shelia (Payment): Finalize Observability dashboards and performance profiling reports.

# Tutorial 9
### 1. The current architecture of the group MySawit, the context, container and deployment diagram
Context diagram(Maharani):

<img width="926" height="701" alt="mysawitcontext" src="https://github.com/user-attachments/assets/f8f20ad4-efa0-4c57-b3e9-8badc4a66476" />

Container diagram(Davin):

<img width="1932" height="1322" alt="original" src="https://github.com/user-attachments/assets/534ac529-e103-41e1-ab26-2f636a6fff10" />

Deployment diagram(Shelia): 

<img width="681" height="881" alt="deployment drawio" src="https://github.com/user-attachments/assets/2d80f889-6d94-4988-88fc-27e7f0ab957e" />

### 2. The future architecture of the group mysawit
(Juansao)

<img width="1067" height="1412" alt="mysawit_future_architecture_container drawio" src="https://github.com/user-attachments/assets/e058dc8a-b3da-499e-8c9f-37a1639647d7" />


### 3. Explanation of risk storming of the group mysawit
(Shelia)
**Key Findings (13 risks identified)**

* R1: Dyno Sleeping on Free/Eco Tier

This risk happens because the system uses Heroku Eco or Basic dynos, which automatically “sleep” after 30 minutes without activity. When plantation workers open the application after inactivity, the first request can take 10–30 seconds due to the dyno cold start process. This creates a poor user experience, especially during morning harvest operations when many workers access the system simultaneously. The delay may also cause users to think the system is broken or disconnected.

* R2: Ephemeral Filesystem (No Persistent Storage)

Heroku dynos do not provide permanent local storage. Any files saved locally, such as harvest photos or logs, will disappear whenever the dyno restarts or the application is redeployed. This creates a critical risk of losing important operational data if the backend stores files directly inside the server instead of using cloud storage services like Amazon S3 or Cloudinary.

* R3: No Zero-Downtime Deployment Strategy

During deployment, Heroku temporarily restarts dynos, which can create short downtime periods. Even a few seconds of downtime is dangerous for a plantation management system because workers may lose data while submitting harvest records. If submissions fail during peak harvest hours, operational records can become incomplete or inconsistent.

* R4: Single-Region Deployment with No CDN

The application is hosted in a single region without a CDN, meaning Indonesian users may experience high latency if the server is located far away, such as in the US or Europe. Plantation workers in remote areas like Kalimantan or Sumatra may experience slow page loading and delayed API responses, reducing system efficiency during field operations.

* R5: Monolith with No Horizontal Scaling Plan

The backend combines all major modules (authentication, harvest, transport, payment, and plantation management) inside one Spring Boot application. During harvest season, a large increase in harvest submissions could overload the entire application, causing slower performance for all other services. Because everything runs together, one overloaded feature can impact the whole system.

* R6: No API Versioning

The frontend directly communicates with backend APIs without version control such as /api/v1/. This creates a strong dependency between frontend and backend systems. If developers change backend APIs, the frontend may immediately stop working. This increases the risk of deployment failures and makes rollback processes more difficult.

* R7: Session State Not Shared Across Dynos

If the application scales to multiple dynos, user sessions stored in memory may not be synchronized between servers. As a result, users may suddenly be logged out when their requests are routed to another dyno. This is dangerous for plantation workers because they could lose unsaved harvest submissions while working in the field.

* R8: Payment Gateway Still in Sandbox Mode

The system still uses a sandbox payment gateway environment. If sandbox credentials are mistakenly used in production, payments may appear successful even though no real transaction occurs. This can lead to serious financial problems, including incorrect salary payments for plantation workers.

* R9: Secret Leakage via Environment Variables

Sensitive information such as database credentials, JWT secrets, and payment API keys are stored in Heroku Config Vars. Any collaborator with access to the Heroku project can potentially view these secrets. Without strict access control, there is a high risk of credential leakage and unauthorized system access.

* R10: No Automated Backup Verification

Although Heroku Postgres provides automated backups, low tier plans keep backups for only a limited period. If database corruption or accidental deletion is discovered too late, important plantation records, payment history, and harvest data may become permanently unrecoverable.

* R11: No Offline Mode for Field Workers

Plantation areas often have unstable internet connections, but the current frontend does not support offline mode or local caching. If workers lose internet access while filling out harvest forms, all entered data may disappear, forcing them to repeat their work manually.

* R12: No Automated Testing Before Deployment

The project lacks strong automated testing in both frontend and backend systems. Without proper testing gates in the CI/CD pipeline, broken or unstable code can be deployed directly into production. This increases the chance of system failures affecting active plantation operations.

* R13: No Monitoring or Error Tracking

The system currently has no monitoring, logging, or alerting tools. If the backend crashes or experiences memory issues, administrators may not realize the problem until users report it. During critical harvest periods, undetected downtime can severely disrupt plantation activities and reduce operational reliability.

<hr>
Risk Matrix:
<br>
<img width="694" height="475" alt="riskmatrix" src="https://github.com/user-attachments/assets/16967c6f-b833-485a-a8c1-7a5913fa9793" />

| ID   | Risk |
|------|------|
| R1   | Dyno sleeping causes 10–30s cold-starts for field workers |
| R2   | Ephemeral filesystem — harvest photo uploads lost on restart |
| R8   | Payment gateway still in Sandbox mode — no real transactions |
| R12  | No test gate in CI/CD — broken code reaches production |
| R13  | No monitoring/observability — silent crashes go undetected |
