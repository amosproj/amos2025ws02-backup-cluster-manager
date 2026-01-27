# Requirements

---
- Append `test-runner` to the cluster-manager environment variable called `SPRING_PROFILES_ACTIVE` in the global docker-compose.yml
- Run test-runner with 'docker-compose --profile test-runner up --build'
- Adjust number of backups, iterations in docker-compose.yml
- Find the results in /results folder