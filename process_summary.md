# Sprint Development and Release Process

This document outlines the key processes to follow during each sprint to ensure clean code management, proper collaboration, and consistent release practices.

---

## Code Management

Only working code should be pushed.  
Before pushing any changes, make sure that:

- All tests pass locally.  
- The application builds successfully.  
- No debugging or temporary code remains.  

---

## Merge Requests (MR)

- Always open a **Merge Request (MR)** when you want to push your changes.  
- Preferably do it with the help of the GitHub UI.
- Each MR **must be reviewed and approved by at least two different Software Developers (SDs)** before it can be merged.  
- Once approved:
  - Resolve any comments or conflicts.  
  - Merge the MR into the `main` branch.  

---

## Release Manager

If you are the **Release Manager** for the sprint:

### 1. Creating the Release Candidate (RC)

The **Release Candidate (RC)** should be tagged in Git as follows:

```bash
git checkout main
git pull
git tag -a sprint-x-release-candidate -m "Release Candidate for Sprint <number>"
git push origin sprint-x-release-candidate
```

### 2. Creating the Final Release
The final release should be created via the GitHub Releases UI.
In the GitHub repository:
Navigate to the Releases section.
Draft a new release. Enter the title `sprint-x-release` and choose `main` as the target branch. 
Create a new tag and name it `sprint-x-release`. 
Afterwards you can also click on the `Generate Release Notes` button. 
Click the `Save draft` button.
You can then do the release live during the Team Meeting.


### Notes
Using the GitHub Releases UI ensures that each release includes automatically generated changelogs, commit references, and attached build artifacts when applicable.
This approach provides better visibility for the team, clear version tracking, and more consistent documentation for each sprint release.