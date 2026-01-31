# Information - Test Backup Volume Delete

---
This test will create a number of backups and then delete them concurrently, while measuring the delete performance.
This test will use all `node[number]-self-register` service nodes from the root `docker-compose.yml`.

### Test Steps
1. The test will run for `ITERATIONS` number of iterations.
2. In each iteration:
   1. A backup will be created for a random node of the selection
   2. The same backup just created will be deleted.
   3. This will happen concurrently - the concurrent requests will increase until the `MAX_VUS` limit is reached.


### Config
The following environment variables for the `test-backup-volume-delete` service in the main `docker-compose.yml` at the root can be used to configure the test:
- `MAX_VUS` - Number of concurrent requests.
- `ITERATIONS` - Number of iterations to perform.
