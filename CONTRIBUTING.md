# Contributing to Expense Tracker 🚀

Thank you for contributing! Whether you are fixing a bug, adding a new feature, or improving documentation, follow the guidelines below.

---

## 🛠️ Contribution Workflow

### 1. Find or Create an Issue
Check existing issues on GitHub or create a new one to discuss the bug or feature before opening a pull request.

### 2. Fork the Repository
Click **Fork** at the top right of the repository page to create a copy under your GitHub account.

### 3. Clone Your Fork
Clone your forked repository locally:
```bash
git clone https://github.com/YOUR-USERNAME/Android-Expense-Tracker.git
cd Android-Expense-Tracker
```

### 4. Create a Feature Branch
Create a new branch off `master`:
```bash
git checkout master
git pull origin master
git checkout -b feat/your-feature-name
```
*(Use `fix/` for bug fixes, `feat/` for new features, `chore/` for maintenance)*

### 5. Code & Style Guidelines
- **Offline-First & Standalone**: Never add network permissions, cloud dependencies, or tracking. The app must remain 100% functional locally.
- **Architecture**: Adhere to Clean Architecture and MVVM (Data -> Domain -> Presentation).
- **Design System**: Follow the Organic Forest Luxury design aesthetic (radiant canopy, high contrast, WCAG AA touch targets).
- **Quality**: Ensure new business logic and ViewModel changes include unit tests.

### 6. Verify Build & Tests
Before committing, make sure all tests pass and the APK builds without errors:
```bash
# Run unit and stress tests
./gradlew testDebugUnitTest

# Assemble debug build
./gradlew assembleDebug
```

### 7. Commit & Push
Write clear, concise commit messages following Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`):
```bash
git add .
git commit -m "feat: brief description of changes"
git push origin feat/your-feature-name
```

### 8. Open a Pull Request
1. Go to the original repository on GitHub.
2. Click **New Pull Request**.
3. Set the base branch to **`master`** and compare against your feature branch.
4. Fill out the PR template with description and checklist.
