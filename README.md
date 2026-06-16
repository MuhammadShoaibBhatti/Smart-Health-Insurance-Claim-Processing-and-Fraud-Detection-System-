# Smart Health Insurance - Fraud Detection System 🛡️🏥

A Java Swing-based desktop application designed to streamline healthcare claim management while incorporating intelligent automated rules to detect potentially fraudulent insurance claims. This project showcases the application of software engineering principles combined with practical cyber security fraud assessment logic.

---

## 🚀 Project Overview

The **Smart Health Insurance Fraud Detection System** simulates an automated validation pipeline where different stakeholders (Hospitals, Review Officers, and Administrators) interact with insurance data. 

As a **Cyber Security student**, this project focuses on:
* **Data Integrity & Risk Mitigation:** Detecting anomalies, financial discrepancies, and high-risk claims (e.g., automated inflation of bills or suspicious admission durations).
* **Role-Based Access Control (RBAC):** Restricting and dynamic-filtering views based on user privileges (Admin, Officer, Hospital).
* **Secure Auditing:** Exporting static text-based cryptographic-ready logs/reports for compliance and digital forensic trails.

---

## 🛡️ Cyber Security Features Implemented

* **Algorithmic Risk Scoring (Fraud Detection Engine):**
    * Automatically flags claims exceeding high-threshold limits (e.g., over \$100,000) for deep inspection.
    * Cross-references data points (e.g., high bills coupled with 0 days of hospital admission) to catch logical inconsistencies and double-billing flags.
* **Secure State Management:** Built-in cryptographic-style data backup and restoration engine utilizing structural flat-file CSV dumps to maintain business continuity.
* **Dynamic Logging & Non-Repudiation:** Generates formatted `.txt` official audit compliance reports tracking specific patients, diagnosis codes, and assigned verification officers.

---

## ⚙️ Core Functionalities

* **Multi-View Dashboard:** Real-time data visualization cards showing Total Claims, Approved Claims, Flagged Fraud Cases, and Unique Patients.
* **Role-Based Navigation:** Custom dynamic menus generated dynamically on-the-fly depending on the logged-in user role.
* **Search & Filtering System:** Ability to seamlessly filter entries by timestamp using regular expressions (`RowFilter.regexFilter`).
* **Dynamic Live Timeline:** An interactive graphical timeline component reflecting the custom workflow state (`ClaimStage`).

---

## 📁 Tech Stack & Prerequisites

* **Language:** Java (JDK 8 or higher)
* **GUI Framework:** Java Swing & AWT (Abstract Window Toolkit)
* **Architecture Pattern:** View-Controller Isolation with centralized State Management
* **IDE Recommended:** NetBeans, IntelliJ IDEA, or Eclipse

---

## 🛠️ Installation & Execution

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/MuhammadShoaibBhatti/Smart-Health-Insurance.git
   cd Smart-Health-Insurance
   javac SmartHealthInsurance.java
   java SmartHealthInsurance

   🔑 Default Credentials for Testing:
Username: admin | Password: admin | Role: Admin
