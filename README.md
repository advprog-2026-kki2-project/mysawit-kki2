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

