## 3.0.3 (2020-03-09)
- Local GCS config needs to return client email from service account credentials for signing urls
- Local development without JSON key file is not recommended for now

## 3.0.2 (2020-03-09)
- Another config fix for default bucket

## 3.0.1 (2020-03-09)
- **Skip this version and go to** `3.0.2`
- Extending on from `3.0.0` with configuration fixes
- "zero config". Auto configure according to properties where bean definitions don't already exist. No more need for redundant `@Configuration` classes.

## 3.0.0 (2020-03-06)
- **Do not use**: There were issues with this release, resolved in `3.0.1`
- Local instances can use the local user's gcloud credentials to connect to a server, so locally we don't need to enforce using JSON credentials. The config for json credentials will only attempt to do so if the credentials file exists, otherwise falling back to using appengine default service credentials
- Improved bean tests to get them workin again
- Bumped internal lib versions
- **BREAKING:** Signature of constructors has changed. If existing projects did not manually construct objects there should be no impact with any luck.

## 2.0.0 (2020-03-06)
Custom expiry for signed URLs is now supplied as a `Duration` instead of an `int` and a new method in `GcsJsonApiClient` returns
the `OffsetDateTime` of the expiration as well as the URL.

**Breaking:** Call to `GcsJsonApliClient.generateSignedUrl()` last parameter changed from `Integer` representing minutes to `Duration`.
Simple fix for existing calls is to replace last parameter with `Duration.ofMinutes(xxx)`.

## 1.4.0 (2020-01-22)
- Convenience method for moving a file from a source `Blob`

## 1.3.0 (2019-11-11)
- Adds ability to list files and bumps up internal dependency versions. There should be no breaking changes.

## 1.2.1 (2019-05-21)
- Included method to move gcs object from one bucket to another

## 1.2.0 (2018-10-17)
- Migrated CloudStorageService from `com.threewks.spring:spring-boot-gae` library to this library.

## 1.0.0-alpha-6 (2018-07-03)
- Fix download url which was broken with Spring Boot 2 due to change with org.springframework.web.util.UriUtils

## 1.0.0-alpha-5 (2018-06-18)
- Fix download url for files containing url-special characters

## 1.0.0-alpha-4 (2018-05-18)
- Updated guava to version 20
- Refactored code
