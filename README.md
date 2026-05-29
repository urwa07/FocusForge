# FocusForge

FocusForge is a JavaFX-based productivity and study management application designed to help students organize tasks, optimize study schedules, improve focus, and gain insights into their learning habits.

### Features:

 ## 🏠 Dashboard
- View daily study overview
- Track upcoming tasks and deadlines
- Monitor productivity metrics
- Personalized study recommendations

 ## 📝 Task Management
- Create, update, and delete tasks
- Set priorities and deadlines
- Track completion status
- Organize workload efficiently

## 📅 Smart Scheduling
- Automatic study schedule generation
- Deadline-based planning strategy
- Balanced workload planning strategy
- Energy-aware task allocation

## 🎧 Focus Mode
- Dedicated focus sessions
- Study timer support
- Focus reflections and session tracking
- Productivity monitoring

## 📊 Analytics & Insights
- Productivity analysis
- Behavioral pattern recognition
- Risk assessment for overdue tasks
- Study habit insights

## 🎓 Course & Exam Management
- Manage courses and subjects
- Track upcoming exams
- Allocate study time effectively

## 💾 Persistent Data Storage
- Saves application state using JSON
- Automatically restores data on restart
- Maintains tasks, schedules, and user progress

---

  Software Architecture

The project follows a layered architecture inspired by Clean Architecture principles.

### Domain Layer
Contains the core business logic:

- Task
- Course
- Exam
- Student
- StudySession
- DailyPlan
- ProductivityProfile
- RiskAssessment


### Application Layer
Coordinates business operations:

- StudyCoachService
- ProgressTracker
- Rescheduler
- FocusAudioService

 ### Infrastructure Layer
Handles persistence and external resources:

- JsonStudyCoachRepository
- Jackson ObjectMapper configuration

### Presentation Layer
JavaFX user interface:

- MainController
- MainViewModel
- FXML Views
- CSS Styling

---


##  Technologies Used

| Technology | Purpose |
|------------|----------|
| Java 21 | Core Programming Language |
| JavaFX 21 | GUI Framework |
| Gradle | Build Automation |
| Jackson Databind | JSON Serialization |
| Jackson JSR310 | Java Time Support |
| FXML | UI Layout |
| CSS | Application Styling |
| JUnit 5 | Testing |

---

##  Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com.studycoach/
│   │       ├── application/
│   │       ├── domain/
│   │       ├── infrastructure/
│   │       └── presentation/
│   └── resources/
│       ├── fxml/
│       └── css/
```

---

##  Installation

### Prerequisites

- Java JDK 21
- Gradle 8+
- JavaFX 21

### Clone Repository

```bash
git clone https://github.com/yourusername/FocusForge.git
cd FocusForge
```

### Run Application

```bash
./gradlew run
```

On Windows:

```bash
gradlew.bat run
```

---

##  Data Persistence

The application stores data in JSON format using Jackson serialization.

Data includes:

- Tasks
- Courses
- Exams
- Study schedules
- Focus sessions
- Productivity analytics

The state is automatically restored when the application is launched again.

---


##  License

This project is intended for educational purposes.
