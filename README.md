# Exam Scheduling System

An intelligent Exam Scheduling System developed using Spring Boot to automate the generation of examination timetables efficiently. The system generates exam dates and slot allocations while minimizing scheduling conflicts using scheduling algorithms.

---

# Project Overview

The Exam Scheduling System is designed to simplify and automate the process of creating examination timetables for educational institutions. Manual scheduling is time-consuming and error-prone, especially when handling multiple subjects, student batches, and time slots.

This project automatically generates:
- Exam dates
- Slot allocations
- Conflict-free schedules
- Optimized timetables

The system uses Greedy and Backtracking algorithms to generate efficient exam schedules while avoiding overlapping exams for students.

---

# Features

- Automated exam timetable generation
- Exam date allocation
- Slot-based scheduling
- Conflict detection system
- RESTful API support
- Greedy scheduling algorithm
- Backtracking scheduling algorithm
- PDF timetable generation using OpenPDF
- JSON-based input processing
- Dynamic schedule generation
- Error handling and validation
- User-friendly interface
- Maven-based Spring Boot project

---

# Technologies Used

| Technology | Purpose |
|---|---|
| Java 17 | Core programming language |
| Spring Boot | Backend framework |
| Maven | Dependency management |
| Spring Web | REST APIs |
| OpenPDF | PDF generation |
| Jackson | JSON processing |
| Lombok | Boilerplate reduction |
| HTML/CSS/JavaScript | Frontend |

---

# Project Objectives

- Automate exam scheduling
- Reduce manual scheduling effort
- Minimize exam conflicts
- Generate optimized date-slot combinations
- Improve timetable accuracy
- Provide downloadable schedules
- Support scalable scheduling solutions

---

# System Modules

## 1. Scheduling Module
- Generates exam schedules automatically
- Assigns dates and slots to exams
- Produces timetable output

## 2. Conflict Detection Module
- Detects overlapping exams
- Prevents student timetable conflicts
- Ensures proper slot allocation

## 3. Algorithm Module
Implements:
- Greedy Algorithm
- Backtracking Algorithm

## 4. PDF Generation Module
- Generates downloadable exam timetable PDFs
- Creates printable schedules

## 5. REST API Module
- Handles API requests
- Processes JSON data
- Returns generated schedules

## 6. User Interface Module
- Displays generated timetables
- Accepts scheduling inputs
- Provides scheduling reports

---

# Installation Steps

## Prerequisites

Install:
- Java 17+
- Maven
- Git
- IDE (IntelliJ / Eclipse / VS Code)

---

## Clone Repository

```bash
git clone https://github.com/yourusername/exam-scheduling-system.git
```

---

## Navigate to Project Folder

```bash
cd exam-scheduling-system
```

---

## Build Project

```bash
mvn clean install
```

---

## Run Application

```bash
mvn spring-boot:run
```

OR

```bash
java -jar target/exam-scheduling-system.jar
```

---

## Access Application

```text
http://localhost:8080
```

---

# Project Structure

```text
exam-scheduling-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/exam/scheduler/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── model/
│   │   │       ├── algorithm/
│   │   │       ├── pdf/
│   │   │       └── config/
│   │   │
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   ├── templates/
│   │   │   └── application.properties
│
├── pom.xml
├── README.md
└── target/
```

---

# JSON Input Format

## Sample Input

```json
{
  "subjects": [
    {
      "subjectCode": "CS101",
      "subjectName": "Data Structures"
    },
    {
      "subjectCode": "CS102",
      "subjectName": "Operating Systems"
    }
  ],
  "slotsPerDay": 2,
  "startDate": "2026-04-10"
}
```

---

# API Endpoints

## Generate Schedule

```http
POST /api/schedule/generate
```

---

## Get All Schedules

```http
GET /api/schedule/all
```

---

## Download PDF Schedule

```http
GET /api/schedule/pdf
```

---

## Check Conflicts

```http
POST /api/schedule/conflicts
```

---

# Usage Guide

## Step 1
Start the Spring Boot application.

---

## Step 2
Open browser:

```text
http://localhost:8080
```

---

## Step 3
Enter exam subject details.

---

## Step 4
Choose scheduling algorithm:
- Greedy
- Backtracking

---

## Step 5
Generate schedule.

---

## Step 6
View generated timetable with:
- Exam date
- Slot number
- Subject details

---

## Step 7
Download timetable PDF.

---

# Algorithms Explained

## Greedy Algorithm

### Advantages
- Faster execution
- Simple implementation
- Suitable for smaller datasets

### Disadvantages
- May not produce globally optimal schedules

---

## Backtracking Algorithm

### Advantages
- Better conflict handling
- More optimized schedules

### Disadvantages
- Higher computation time

---

## Comparison

| Feature | Greedy | Backtracking |
|---|---|---|
| Speed | Fast | Moderate |
| Optimization | Medium | High |
| Complexity | Low | High |

---

# Sample Output

| Subject | Date | Slot |
|---|---|---|
| Data Structures | 10-04-2026 | Slot 1 |
| Operating Systems | 10-04-2026 | Slot 2 |

---

# Configuration

## application.properties

```properties
server.port=8080

spring.application.name=exam-scheduling-system
```

---

# Screenshots

Add screenshots:
- Dashboard
- Input Form
- Generated Timetable
- PDF Output

---

# Demo Video

```text
https://drive.google.com/your-demo-video-link
```

---

# GitHub Repository

```text
https://github.com/yourusername/exam-scheduling-system
```

Repository shared with:
- suruchi.dedgaonkar@vit.edu
- suruchi.dedgaonkar@viit.ac.in

---

# Team Members

- Your Name
- Team Member 2
- Team Member 3

---

# Future Scope

- AI-based scheduling optimization
- Automatic invigilator assignment
- Mobile application support
- Cloud deployment
- Real-time conflict analytics
- Multi-department scheduling

---

# License

Educational project for academic purposes.

---

# Conclusion

The Exam Scheduling System automates the process of assigning exam dates and slots efficiently using Spring Boot and scheduling algorithms. The project minimizes scheduling conflicts, reduces manual effort, and improves timetable management for educational institutions.
