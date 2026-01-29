# Information - Test Backup Volume Read

---
This test stresses the backup volume read functionality by performing multiple, concurrent read operations on the backup getter endpoint.
There is only one single node used.

### Test Steps
1. First `INITIAL_BACKUPS` will be created.
2. Then the backup volume read endpoint `GET /api/v1/cm/backups` will be called concurrently.
   - First it will start to warm up with limited users.
   - Then it will increase the number of users until a certain threshold is reached.
   - Afterwards it will start to decrease the number of users again.
3. If `ITERATIONS` is not reached, new `BACKUP_INCREMENT` backups will be created, and the test will continue from step 2.
4. After `ITERATIONS` iterations the test will stop.

### Config
The following environment variables for the `test-backup-volume-read` service in the main `docker-compose.yml` at the root can be used to configure the test:
- `INITIAL_BACKUPS` - Number of initial backups to create.
- `BACKUP_INCREMENT` - Number of backups to create after each iteration.
- `ITERATIONS` - Number of iterations to perform.  
---
For additional control over the test, the `options`object in the `backup-stress-test.js`file can be adjusted according to the [Grafana K6 Docs](https://grafana.com/docs/k6/latest/)