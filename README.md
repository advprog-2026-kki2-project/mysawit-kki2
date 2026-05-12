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

.
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



**Individual container diagram payment(Shelia) (with its code diagram) of the group mysawit**

**Component diagram**

<img width="634" height="498" alt="Screenshot 2026-05-12 at 20 26 42" src="https://github.com/user-attachments/assets/673ecfd2-68ae-417c-9649-19275c981a96" />

This is a C4 Component Diagram for the Payment Management Service within the MySawit backend, built as a Spring Boot module. It illustrates six internal components and their interactions with four external containers.
The left-side green components (PaymentController, PayrollService, WageConfigService) represent the core business flow: the Frontend SPA sends requests to the REST controller, which delegates to the service layer for payroll calculation, which in turn consults WageConfigService for wage/kg rates.
The right-side purple components (PaymentGatewayAdapter, PayrollRepository, PayrollEventListener) handle integrations: the gateway adapter communicates with Midtrans (payment provider) via HTTPS, the repository persists data to a PostgreSQL/H2 database using JPA, and the event listener handles async triggers from external modules.
Auth module connects via a dashed arrow to PaymentController, indicating JWT-based token verification happens asynchronously/out-of-band rather than as a direct blocking call.

**Code Diagram 1**

<img width="506" height="423" alt="Screenshot 2026-05-12 at 20 28 15" src="https://github.com/user-attachments/assets/91a9e8f3-5ca4-4d71-a0c7-03b890dce7b9" />

The diagram is a Payment Module Domain Class Diagram consisting of five components: Payroll (entity), PayrollStatus (enumeration), WageConfig (entity), PayrollService (service), and PaymentGatewayAdapter (adapter).
The Payroll entity is the core data model, holding fields like recipientId, recipientRole, weightKg, wagePerKg, totalWage, status, and createdAt, with methods approve() and reject().
PayrollStatus is an enumeration with three possible states — PENDING, ACCEPTED, and REJECTED — and is referenced by the Payroll entity through its status field.
WageConfig is a configuration entity that stores per-role wage rates (laborerWagePerKg, driverWagePerKg, foremanWagePerKg) and provides those rates to PayrollService.
PayrollService acts as the orchestrator: it uses WageConfig to determine wage rates, creates Payroll records (with a 1..* multiplicity), and delegates payment processing to PaymentGatewayAdapter.
PaymentGatewayAdapter wraps the external payment gateway, exposing methods chargePayroll(), checkStatus(), and refund() to decouple the service layer from the payment provider implementation.

**Code Diagram 2**

<img width="586" height="452" alt="Screenshot 2026-05-12 at 20 29 20" src="https://github.com/user-attachments/assets/f70ad2c4-ed23-4a21-bbf5-81816a48b4ad" />

This is a sequence diagram showing the method-level flow of an admin approving a payroll in the MySawit payment module. The flow involves five participants: Admin, PaymentController, PayrollService, GatewayAdapter, and Repository.
The Admin initiates the process by sending a POST /payroll/{id}/approve HTTP request to the PaymentController, which then delegates to PayrollService via approve(payrollId, adminId). PayrollService first performs a self-call validateStatus() to verify the payroll is in an approvable state, then calls GatewayAdapter's chargePayroll() method. GatewayAdapter communicates with the external Midtrans payment gateway via HTTPS, receives a transaction ID and success status, after which PayrollService saves the payroll as ACCEPTED through the Repository. Finally, responses bubble back up the chain, ending with a 200 OK { payrollId, status } returned to the Admin. An alt fragment at the bottom notes that if the gateway call fails, a PaymentGatewayException is thrown and the payroll status remains PENDING.

**Code Diagram 3**

<img width="618" height="474" alt="Screenshot 2026-05-12 at 20 30 42" src="https://github.com/user-attachments/assets/0f689888-726d-40e6-a402-afb7354f2168" />

This diagram illustrates the asynchronous payroll creation event flow in a payment module. Three publisher services — HarvestService, DeliveryService, and OrderService — each fire their respective Spring ApplicationEvent (e.g., HarvestApprovedEvent) when an approval occurs. These events are consumed asynchronously by a single PayrollEventListener, which is decorated with Spring's @EventListener annotation, represented by the dashed arrows. Inside the listener, the wage is calculated using a configurable formula sourced from WageConfig — for example, a laborer's pay is computed as wagePerKg × weightKg × 0.9, with drivers and foremen using different rates. After the wage is resolved, the listener builds a Payroll entity and persists it to the database via PayrollRepository.save(). The design is fully decoupled: publisher modules have no direct dependency on the payment module, making the architecture clean and extensible.

**Code Diagram 4**

<img width="531" height="570" alt="Screenshot 2026-05-12 at 20 32 49" src="https://github.com/user-attachments/assets/df0cc240-da43-4a57-abea-424b0601da11" />

The flowchart represents the calculateWage(event) function, which handles payroll calculation logic for different worker roles. It begins by fetching the latest wage configuration from wageConfigRepo.findLatest(). The flow then hits a decision node (event.role?) that branches into three paths: LABORER, DRIVER, or FOREMAN. Both LABORER and DRIVER use the same formula (rate × kg × 0.9), while FOREMAN uses rate × approvedKg × 0.9, reflecting a distinction based on approved weight rather than raw weight. All three branches converge into a single step that builds a Payroll entity with a PENDING status and a description. Finally, the payroll is persisted via repository.save(payroll) and the completed Payroll object is returned.

**Code Diagram 5**

<img width="797" height="446" alt="Screenshot 2026-05-12 at 20 36 13" src="https://github.com/user-attachments/assets/b1f8cd97-238e-4864-9d30-f30a688125d8" />

This is a UML State Machine diagram for a PayrollStatus entity, showing all valid lifecycle transitions. The flow begins at an initial pseudo-state (filled circle), where a PayrollEventListener creates the payroll and immediately places it in the PENDING state, which sends a notification on entry.
From PENDING, an admin can take one of two actions: approve the payroll (guarded by [gateway success]), transitioning it to ACCEPTED, or reject it (guarded by [reason required]), transitioning it to REJECTED. The ACCEPTED state represents a successfully processed payment and logs a transaction ID (txId) on exit, while the REJECTED state records the reason for refusal on exit.
Both ACCEPTED and REJECTED are terminal states — each leads to a final pseudo-state (bullseye circle) and cannot be re-opened or reversed, as enforced by the "no re-open once terminal" guard note at the bottom.
