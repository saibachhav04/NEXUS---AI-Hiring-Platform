NEXUS — AI-Powered Hiring Platform
NEXUS is a full-stack hiring platform built with Spring Boot and React. It uses AI to help companies hire better by removing bias from job descriptions, scoring resumes automatically, and helping interviewers make fairer decisions.

What problem does it solve?
Hiring is time-consuming and often unfair. Recruiters spend hours reading resumes manually, job descriptions often contain biased language without the recruiter realizing it, and rejected candidates never get any feedback. NEXUS solves all of these problems using AI.

Features
For Recruiters:

Post a job and AI automatically scans it for biased or exclusionary language
Upload candidate resumes and AI scores them against the job description
View all candidates on a live kanban pipeline board
Get AI-generated interview questions tailored to each candidate
After interviews, get an AI verdict — hire or no hire — based on all evaluator scores

For Candidates:

Browse open job postings
Upload your resume and get an AI match score before applying
Track your application status in real time


Tech Stack
Backend:

Java 17
Spring Boot 3.2
Spring Security with JWT for authentication
Spring WebSocket for real-time pipeline updates
PostgreSQL for the database
Redis for caching
MinIO for storing uploaded resume files
Apache PDFBox for reading text from PDF resumes
Groq API (Llama 3.3 model) for all AI features

Frontend:

React 18
React Router for navigation
Zustand for state management
Axios for API calls
Recharts for charts
Light and dark mode support


How the AI works
There are 7 places in the app where AI is used:

Bias check — when a recruiter writes a job description, AI reads it and flags biased words like "rockstar", "ninja", "young and energetic" and suggests better alternatives
Resume parsing — AI reads the PDF resume and extracts the candidate's name, skills, experience, and education into structured data
Match scoring — AI compares the resume to the job description and gives a score from 0 to 100 with reasons
Interview questions — AI looks at the candidate's resume gaps and generates specific questions to ask in the interview
Scorecard verdict — after all interviewers submit their ratings, AI reads all feedback and gives a final hiring recommendation
Pipeline insight — AI gives a one-line tip when a candidate is moved to interview stage
Candidate feedback — candidates see AI feedback on why they are or are not a good fit for the role


User Roles
There are 4 roles in the system:

RECRUITER — can post jobs, view resumes, manage the pipeline
HIRING_MANAGER — same as recruiter, used for the actual hiring team
CANDIDATE — can browse jobs, apply, and track their status
ADMIN — full access


How to run locally
Requirements:

Java 17
Node.js 20
PostgreSQL 16
Redis
MinIO
A free Groq API key from console.groq.com

Steps:

Clone the repository
Create a PostgreSQL database called nexusdb
Update application.properties with your database credentials and Groq API key
Run the backend with mvn spring-boot:run
Run the frontend with npm install then npm start
Open http://localhost:3000 in your browser


Database Tables

users — stores all user accounts with their role
job_descriptions — stores job posts along with the AI bias analysis
applications — stores each resume submission with AI match score and stage
interview_scorecards — stores interviewer ratings and the final AI verdict

┌─────────────────────────────────────────────────────────────┐
│                     React Frontend                          │
│  Login  │  Recruiter Dashboard  │  Pipeline  │  Interview  │
└────────────────────┬────────────────────────────────────────┘
                     │  REST API + WebSocket
┌────────────────────▼────────────────────────────────────────┐
│                  Spring Boot Backend                        │
│                                                             │
│  AuthController    JobController    ResumeController        │
│  PipelineController               ScorecardController       │
│                                                             │
│  AuthService  JobService  ResumeService  PipelineService    │
│  ScorecardService         ClaudeService  EmailService       │
└──────────┬──────────────────────┬──────────────────────────┘
           │                      │
    ┌──────▼──────┐      ┌────────▼────────────────────┐
    │ PostgreSQL  │      │  Groq AI API                │
    │ Redis       │      │  MinIO (PDF storage)        │
    └─────────────┘      └─────────────────────────────┘


Project Structure
nexus-hiring/
├── backend/
│   └── src/main/java/com/nexus/backend/
│       ├── config/       JWT, security, WebSocket config
│       ├── controller/   API endpoints
│       ├── service/      Business logic and AI calls
│       ├── model/        Database entities
│       ├── repository/   Database queries
│       └── dto/          Request and response objects
│
└── frontend/
    └── src/
        ├── pages/        Full page components
        ├── components/   Reusable UI parts
        ├── api/          All API call functions
        └── store/        Auth and theme state

Why I built this
This was my internship project. I wanted to build something that solves a real problem, not just a todo list or weather app. Hiring bias is a real issue that affects millions of people and most tools that solve it cost thousands of dollars per year. NEXUS is a free, open source alternative.
