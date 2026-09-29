# Git Workflow

**Group Members:** Charles, Gavin, Jacob, Ryan

## First-Time Setup

```bash
git clone https://github.com/cbvater/cs440_appointment_booking.git
cd cs440_appointment_booking
```

## Each Time You Start New Work

1. Switch to main and pull the latest version:
```bash
   git checkout main
   git pull origin main
```
2. Create a new branch:
```bash
   git checkout -b feature/<description>
```
   - `<description>` is the feature you're implementing, e.g. `user-login` or `search-appointments`
   - If you're making a fix instead of a new feature, use:
```bash
     git checkout -b fix/<description>
```

## Branching Strategy

- Each person creates their own branch for the feature they're working on
- **Always pull before you create a branch**
- Work, commit, and push that branch:
```bash
  git add .                                # stage all changed files
  git commit -m "message"                  # commit with a clear message
  git push origin feature/<description>
```
- Open a Pull Request (PR) into `main` when the work is done
- One teammate reviews and approves before merging

## Opening a Pull Request

1. On GitHub, go to the **Pull requests** tab → **New pull request**
2. Set **base:** `main` and **compare:** `feature/<description>`
3. Give it a title and a description of what the change does
4. Click **Create pull request**
5. Once approved, click **Merge pull request**
   - Use **Squash and merge** for a clean, single-commit history
6. Sync your machine with the new version of main:
```bash
   git checkout main
   git pull origin main
```

## Gotcha: Main Moved Forward

If teammates merged their work into `main` after you branched, you may need to merge those changes into your branch before your PR (and resolve any conflicts):

```bash
git checkout feature/user-login
git merge main
```
