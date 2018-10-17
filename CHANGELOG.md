1.2.0-SNAPSHOT
----------------------
- Migrated CloudStorageService from `com.threewks.spring:spring-boot-gae` library to this library.

1.0.0-alpha-6-SNAPSHOT
----------------------
- Fix download url which was broken with Spring Boot 2 due to change with org.springframework.web.util.UriUtils

1.0.0-alpha-5-SNAPSHOT
----------------------
- Fix download url for files containing url-special characters

1.0.0-alpha-4-SNAPSHOT
----------------------
- Updated guava to version 20
- Refactored code

1.0.0-alpha-4
--------------------------
- File names is preserved exactly when uploaded to storage
- Use Jackson instead of Gson 