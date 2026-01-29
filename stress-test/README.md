# General Stress Test Informations

---
### Requirement for running any stress test:
1. Navigate to the docker-compose.yml file located in the root directory of the project.
2. Replace the environment variable  
`- SPRING_PROFILES_ACTIVE=cluster_manager`  
inside the cluster-manager service with  
`- SPRING_PROFILES_ACTIVE=cluster_manager,test-runner`

### Running the stress test:
1. Make sure you have followed the Requirement above for running any stress test.
1. Select a test from the `/stress-test` folder you want to run
   - Each test has its own folder with a descriptive folder name, starting with `test-`
   - Each test folder contains its own README.md file with specific instructions.
1. After selecting the test, run  
`docker compose --profile [folder-name] up --build`  
where `[folder-name]` is the name of the test folder you selected in step 2.
1. After running the test, you can find the results in the `/stress-test/[folder-name]/results` folder.