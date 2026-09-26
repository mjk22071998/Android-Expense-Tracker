# Contributing to Expense Tracker 🚀

Thank you for contributing! Whether you are taking on an open issue or participating in **Hacktoberfest**, follow the steps below to contribute.

> [!IMPORTANT]
> All contributions **MUST** originate from and target the **`hacktoberfest`** branch (not `main`).

---

## 🛠️ Contribution Workflow

### 1. Find or Pick an Issue
Look for open issues in the repository or pick up a task tagged for Hacktoberfest. Feel free to comment on an issue or create a new one if you have an idea or bug report.

### 2. Fork the Repository
Click **Fork** at the top right of the repository page to create a copy under your GitHub account.

### 3. Clone Your Fork
Clone your forked repository to your local machine:
```bash
git clone https://github.com/YOUR-USERNAME/Android-Expense-Tracker.git
cd Android-Expense-Tracker
```

### 4. Checkout to `hacktoberfest`
Switch to the `hacktoberfest` branch before starting your work:
```bash
git checkout hacktoberfest
```
*(Optionally, create a feature branch from `hacktoberfest`: `git checkout -b fix/my-feature`)*

### 5. Do Your Changes
Open the project in Android Studio and implement your changes. Keep your edits focused and clean.

### 6. Make Sure It Builds Successfully
Verify that the project builds cleanly without errors:
- **In Android Studio:** `Build > Make Project`
- **Via Terminal:**
  ```bash
  # Linux / macOS
  ./gradlew assembleDebug

  # Windows
  gradlew.bat assembleDebug
  ```

### 7. Push Your Changes
Commit and push your changes to your fork on GitHub:
```bash
git add .
git commit -m "feat: brief description of changes"
git push origin hacktoberfest
```

### 8. Make a Pull Request (PR)
1. Navigate to the original repository on GitHub.
2. Click **New Pull Request**.
3. Ensure the **base branch** is set to **`hacktoberfest`**.
4. Fill out the PR template with details about your changes.

---

Happy Coding! 🎃
