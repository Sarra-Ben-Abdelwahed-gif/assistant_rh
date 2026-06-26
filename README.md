# Intelligent HR Assistant - Backend API

This repository contains the Backend API for the Intelligent HR Assistant project.

## Tech Stack
* **Framework:** Java / Spring Boot
* **Security:** Spring Security (JWT)
* **Database:** PostgreSQL
* **Object Storage:** MinIO
* **AI Integration:** Google Gemini API

---

## Prerequisites & Setup

### Google Gemini API Key Configuration
To maintain security, the Google Gemini API key is externalized and managed via system environment variables. 

Before running the application, you must set a permanent environment variable named `GEMINI_API_KEY` on your local machine with your personal API key from Google AI Studio.

#### Setup on Windows:
1. Open the Windows Start Menu, search for **"Edit the system environment variables"** and open it.
2. Click on the **Environment Variables...** button at the bottom.
3. Under the **User variables** section (top half), click **New...**
4. Set the fields exactly as follows:
   * **Variable name:** `GEMINI_API_KEY`
   * **Variable value:** *Your actual Gemini API key*
5. Click **OK** on all windows to save.
6. **Important:** Restart VS Code (or your IDE) to ensure the new variable is successfully loaded.
  
