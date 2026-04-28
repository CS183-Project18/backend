# Unique Finds

Unique Finds is a full-stack discovery and sharing platform for interesting products found in physical stores. The project is designed to help users capture, organize, search, and revisit offline product discoveries that would otherwise be lost in scattered social posts or personal notes.

## Project Overview

The platform turns real-world store finds into structured, searchable content. Instead of treating these discoveries as temporary social content, Unique Finds allows users to publish posts with meaningful metadata such as title, description, category, price range, store information, and images.

The project combines community sharing with discovery-oriented browsing and search. Its goal is to make offline retail discoveries easier to preserve, explore, and share with others.

## Core Features

- User registration, login, and authenticated account access
- Post creation, update, deletion, and listing for store finds
- Item detail viewing with accumulated engagement data
- Structured content organization for discovery and later retrieval
- Frontend browsing interface and backend API support
- Foundation for community interaction, ranking, and intelligent search

## Repository Structure

This repository is the **main submission repository** for the project.

- `src/`, `pom.xml`, `sql/`: backend code and database scripts
- `frontend/`: frontend static files used for the project UI

## Tech Stack

- Backend: Java 21, Spring Boot, Maven, MyBatis, MySQL
- Frontend: HTML, CSS, JavaScript

## Run the Backend

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Frontend Files

The frontend is included in the `frontend/` directory:

- `frontend/index.html`
- `frontend/styles.css`
- `frontend/script.js`

## Original Repositories

- Frontend: https://github.com/CS183-Project18/frontend
- Backend: https://github.com/CS183-Project18/backend

## Submission Version

The final submission version for this repository is tracked with the Git tag `v1`.
