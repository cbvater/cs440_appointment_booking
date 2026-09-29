Group Members: Charles, Gavin, Jacob, Ryan

**FIRST:**
`git clone https://github.com/cbvater/cs440_appointment_booking.git`
`cd` into that folder

**Each time you start new work:**
git checkout main
`git pull origin main` pull latest version of main
`git checkout -b feature/<description>` description being the feature to implement; ex: "user-login" or "search-appointments"
(if a branch was needed to be created to make a fix instead of a new feature, it would be `git checkout -b fix/<description>`


**Branching strategy:**
Each person creates a branch for the feature they are working on. 
Always pull before you create a branch*
Work, commit, push that branch
`git add .`                             # stage all changed files
`git commit -m "message"`               # commit with a clear message
`git push origin feature/<description>`
Open a Pull Request (PR) into main when it's done.
One teammate reviews/approves before merging.

**Opening a pull request:**
On Github, go to the Pull requests tab → New pull request → set base: main, compare: feature/<description>
Give it a title and description, what the change does
Click Create pull request
Once approved, click Merge Pull Request(use "squash and merge" for clean single commit history)
Make sure you sync back up on your machine with the new version of main.

**One gotcha to watch for:** if main has moved forward since you branched (teammates merged their stuff), 
you may need to merge those changes into your branch before your PR, or resolve conflicts:
`git checkout feature/user-login`
`git merge main`
