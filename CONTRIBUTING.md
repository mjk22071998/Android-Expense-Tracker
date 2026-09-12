# Contributing to Expense Tracker 🚀

Welcome to the **Expense Tracker** repository! We are thrilled that you are interested in contributing, especially if you are participating in **Hacktoberfest** or making your very first open-source contribution.

This guide will walk you through everything you need to know, step by step. No prior open-source experience is required!

---

## 📚 Key Concepts Explained

If you are new to GitHub and Git, here are a few basic terms used throughout this guide:

- **Repository (Repo):** The project folder containing all code, history, and documentation (like a digital storage box for the project).
- **Fork:** A personal copy of someone else's GitHub repository that lives on your own GitHub account. It lets you make changes safely without affecting the original project.
- **Upstream:** The original repository from which you created your fork (in this case, [`mjk22071998/Android-Expense-Tracker`](https://github.com/mjk22071998/Android-Expense-Tracker)).
- **Branch:** An isolated workspace within a repository where you can work on a specific feature or bug fix without messing up the main codebase.
- **Pull Request (PR):** A request you submit to ask the maintainers of the upstream repository to review and merge your changes from your branch into the project.
- **`hacktoberfest` Branch:** The dedicated branch in this repository reserved exclusively as the starting point and target for all Hacktoberfest contributor contributions.

---

## ⚠️ IMPORTANT WARNING ABOUT BRANCHES

### Please do not start Hacktoberfest work from `main`.

> [!IMPORTANT]
> **Why is this so important?**
> The `main` branch contains stable production code and ongoing internal development. The `hacktoberfest` branch is created specifically to provide a stable, consistent contribution base for all community contributions during Hacktoberfest.
>
> If you start your work from `main` or submit a Pull Request targeting `main`, your PR will contain unrelated differences between `main` and `hacktoberfest`, which can disrupt the project workflow.

**What happens if a PR targets `main`?**
Don't panic! We won't punish you or give you a hard time. However, we will politely ask you to update your PR's target branch or recreate your contribution from `hacktoberfest` before we can review and accept it.

---

## 🛠️ Step-by-Step Contribution Guide

Follow these steps to make your contribution smooth and successful:

### STEP 1 — Find an Issue

1. Go to the [Issues tab](https://github.com/mjk22071998/Android-Expense-Tracker/issues) of the upstream repository.
2. Look for issues labeled with `good first issue`, `help wanted`, or `hacktoberfest`.
3. Read the issue description carefully before starting any work.
4. **Avoid working on an issue that is already assigned to someone else**, unless you are explicitly invited to collaborate.
5. If you have questions or want to clarify the proposed fix, leave a comment on the issue first!

---

### STEP 2 — Fork the Repository

1. Open the upstream repository on GitHub: [`mjk22071998/Android-Expense-Tracker`](https://github.com/mjk22071998/Android-Expense-Tracker).
2. Click the **Fork** button in the upper-right corner of the page.
3. Create the fork under your personal GitHub account.
4. Your fork is your personal playground—you can push commits and make changes here without worrying about breaking the original project.

---

### STEP 3 — Make Sure the `hacktoberfest` Branch Exists in Your Fork

> [!CAUTION]
> GitHub often displays the default branch (`main`) when you open a repository or fork. **DO NOT start your work from `main`!**

1. Navigate to your forked repository on GitHub (`https://github.com/YOUR-USERNAME/Android-Expense-Tracker`).
2. Click the branch selector dropdown menu (usually showing `main` or `master`).
3. Search for or select **`hacktoberfest`**.
4. Make sure you switch to the **`hacktoberfest`** branch in the GitHub UI before proceeding.

---

### STEP 4 — Clone Your Fork to Your Computer

Open your terminal or command prompt and run one of the following commands (replace `YOUR-USERNAME` with your actual GitHub username):

**HTTPS:**
```bash
git clone https://github.com/YOUR-USERNAME/Android-Expense-Tracker.git
cd Android-Expense-Tracker
```

**SSH:**
```bash
git clone git@github.com:YOUR-USERNAME/Android-Expense-Tracker.git
cd Android-Expense-Tracker
```

---

### STEP 5 — Add the Upstream Repository

To keep your fork in sync with the original project, add the original repository as a remote named `upstream`:

```bash
git remote add upstream https://github.com/mjk22071998/Android-Expense-Tracker.git
```

Verify your remotes by running:
```bash
git remote -v
```

You should see output similar to this:
```text
origin    https://github.com/YOUR-USERNAME/Android-Expense-Tracker.git (fetch)
origin    https://github.com/YOUR-USERNAME/Android-Expense-Tracker.git (push)
upstream  https://github.com/mjk22071998/Android-Expense-Tracker.git (fetch)
upstream  https://github.com/mjk22071998/Android-Expense-Tracker.git (push)
```

- `origin` points to **your fork**.
- `upstream` points to the **original project repository**.

---

### STEP 6 — Check Out the `hacktoberfest` Branch

Fetch all branches from `upstream` and switch to `hacktoberfest`:

```bash
git fetch upstream
git checkout hacktoberfest
```

*Or using the modern Git syntax:*
```bash
git fetch upstream
git switch hacktoberfest
```

---

### STEP 7 — Create a Contribution Branch FROM `hacktoberfest`

> [!IMPORTANT]
> Always create your feature or fix branch **directly from `hacktoberfest`**.

Run one of the following commands:

```bash
git checkout -b fix/expense-calculation
```

*Or using modern Git syntax:*
```bash
git switch -c fix/expense-calculation
```

> **Note:** The command above creates your new branch from whichever branch you are currently on. **Make sure you are on `hacktoberfest` before running it!**

**Branch Structure Example:**
```text
hacktoberfest (base)
└── fix/expense-calculation (your feature branch)
```

---

### STEP 8 — Make Your Changes

- Open the project in **Android Studio**.
- Keep your changes focused on solving the specific issue or feature.
- Avoid making unrelated formatting or style changes to code you aren't touching.
- Follow the existing Kotlin and Android coding conventions used in the project.
- Do not rewrite working code unnecessarily.
- **UI Changes:** Take screenshots or screen recordings of the updated UI.
- **Bug Fixes:** Verify how the bug was reproduced and confirm your fix resolves it.

---

### STEP 9 — Build and Lint the Application

Before committing, verify that your changes build cleanly and pass static analysis.

**Open Android Studio:**
Use **Build > Make Project** (`Ctrl+F9` / `Cmd+F9`) or run the project on an emulator/device.

**Command Line (Terminal):**

On Linux / macOS:
```bash
./gradlew assembleDebug
./gradlew lint
```

On Windows (Command Prompt / PowerShell):
```cmd
gradlew.bat assembleDebug
gradlew.bat lint
```

> [!NOTE]
> This project currently does not have a mandatory automated unit test suite. However, writing tests is highly encouraged and welcomed as a contribution!

---

### STEP 10 — Commit Your Changes

Check modified files and commit with a clear message:

```bash
git status
git add .
git commit -m "fix: correct expense calculation logic in transaction repository"
```

Use descriptive commit messages that clearly explain *what* and *why*.

---

### STEP 11 — Push the Branch to Your Fork

Push your feature branch to your fork on GitHub (`origin`):

```bash
git push -u origin fix/expense-calculation
```

---

### STEP 12 — Open the Pull Request (PR)

1. Go to your fork on GitHub (`https://github.com/YOUR-USERNAME/Android-Expense-Tracker`).
2. You should see a banner saying **"Compare & pull request"**. Click it!
3. **CRITICAL CHECK:** Look at the branch selection header at the top of the PR page. Ensure the fields match the following:

```text
base repository: mjk22071998/Android-Expense-Tracker
base branch:     hacktoberfest   <--- MUST BE hacktoberfest (NOT main)

head repository: YOUR-USERNAME/Android-Expense-Tracker
compare branch:  fix/expense-calculation
```

> [!WARNING]
> If `base branch` says `main`, `master`, or anything else, click the dropdown and change it to **`hacktoberfest`**.

---

### STEP 13 — Complete the PR Description

Fill out the Pull Request template completely:
- Describe what changed and why.
- Reference the issue you are fixing (e.g., `Fixes #12`).
- Confirm that `./gradlew assembleDebug` and `./gradlew lint` passed.
- Attach screenshots or videos if you made UI changes.

---

### STEP 14 — Wait for CI and Code Review

- GitHub Actions will automatically trigger and run build & lint checks on your Pull Request.
- Project maintainers will review your PR. They may:
  - Approve and merge your contribution 🎉
  - Ask clarifying questions
  - Request minor changes or improvements
- **Tone & Culture:** We aim to be friendly, supportive, and welcoming. Don't worry if you make a mistake—we are here to help you learn!

---

### STEP 15 — Responding to Requested Changes

If maintainers ask for changes:
1. You **DO NOT** need to close your PR or open a new one.
2. Simply make the requested edits in your local code on the same feature branch (`fix/expense-calculation`).
3. Commit and push the new changes:
   ```bash
   git add .
   git commit -m "address PR review comments"
   git push origin fix/expense-calculation
   ```
4. Your Pull Request on GitHub will automatically update with your new commits!

---

## ❓ Need Help?

If you get stuck at any step, feel free to ask for help in an issue or directly in your Pull Request description. Happy hacking! 🎃
