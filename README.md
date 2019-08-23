# Getting Started

- Add service account JSON file by putting [this file](https://drive.google.com/drive/u/1/folders/1nUd5jOuo0glhTWgcYE18OuFVeg46WfVT) on your local machine and add below to your environment variable. Note: You need to request permission to access the file.
```
export GOOGLE_APPLICATION_CREDENTIALS="<PATH TO THE SERVICE ACCOUNT JSON>/RocketDev-xxxx.json"
```

- Start with *dev* Spring profile (default profile)
```shell
./gradlew bootRun
```

- Start with *prod* Spring profile
```shell
./gradlew bootRun --args='--spring.profiles.active=prod'
```

