# spring-gae-gcs
A library for integrating Google AppEngine and Spring Boot applications with the JSON Api of Google Cloud Storage to upload and retrieve files.

**ALPHA VERSION: Please note that this library is in active development and its not guaranteed to work correctly.**

Overview
--------

This library allows you to generate urls to interact with Google Cloud Storage from a Springboot GAE application.

To do so, you can request an upload url for a file, and then upload the file directly from the browser to GCS. 
Later you could request a signed download url for that file. 

Install
-------
Maven: 
```xml
<dependency>
  <groupId>com.threewks.spring</groupId>
  <artifactId>spring-gae-gcs</artifactId>
  <version>1.0.0-alpha-3</version>
</dependency>
```

Gradle:
```groovy
compile 'com.threewks.spring:spring-gae-gcs:1.0.0-alpha-2'
```


Setup
-----

To use the library, import the configuration `@Import(SpringGaeGcsConfiguration.class)` on any config file:

    ...
    @SpringBootApplication
    @Import(SpringGaeGcsConfiguration.class)
    public class Application {
    ...

This will create the following bean that you can inject:
- `GcsJsonApiService`: It will provide methods to generate the upload and download URL.
- `GcsJsonApiClient`: Lower level client for interacting with the JSON api. You will mostly use the Service above.

Configuration
-------------

The following **mandatory** configuration options must be set in your application.properties file:

- `app.host` - a complete URL to the host (eg: `http://www.example.com`).
- `gcs.defaultBucket` - The gcs bucket that should be used to upload the files (eg: `example.appspot.com`).

The following **optional** configuration options can be set in your application.properties file:
- `gcs.attachmentFolder` - The folder that will be used to upload the files. If not provided, `attachments` will be used.
- `gcs.devCredentialsFile` - The name of the file with the service account credentials for local dev (See below).
If not provided, `/dev-gcs-credentials.json` will be used.


Local development
------------------
To be able to upload files from your local environment, you need to generate a JSON key from the default service account of the AppEngine project.
The key should be stored in the resources folder with the name: `dev-gcs-credentials.json`

To obtain the JSON key follow this [guide](https://cloud.google.com/iam/docs/creating-managing-service-account-keys)

Spring profiles
-----------------
The library expects the `gae` profile to be active when the app is running in Google Cloud. `!gae` will be used to indicate local development. 

Dependencies
------------

The library requires two dependencies. If they are not provided, default ones for AppEngine will be used:

- `com.google.api.client.http.HttpTransport` If not provided, will use: `com.google.api.client.extensions.appengine.http.UrlFetchTransport`
- `com.google.api.client.json.JsonFactory` If not provided, it will use: `com.google.api.client.json.jackson2.JacksonFactory`


Usage
-----

You can create endpoints in you Controller to fetch the Upload and Download urls.

eg:
     
```$java                
@Autowired
GcsJsonApiService gcsJsonApiService; //<= Injected
 
@RequestMapping(path = "/uploadUrl", method = POST)
public String generateUploadUrl(@RequestBody StorageUrlRequestDto fileUploadRequest) {
    String uploadUrl = gcsJsonApiService.getUploadUrl(fileUploadRequest.getType(), fileUploadRequest.getName(), null);
    return uploadUrl;
}

@RequestMapping(path = "/downloadUrl/{gcsName}", method = POST)
public DownloadFileResponseDto downloadFile(@PathParameter String gcsName) {
    String download = gcsJsonApiService.getDownloadUrl(gcsName);
    return new DownloadFileResponseDto(download);
}
```                

In the previous example, the `gcsName` used to generate the downloadUrl, is obtained from the GCS response after uploading the file.

Things to consider when uploading from the browser
---------------------------------------------------
If you are uploading the file to GCS directly from the browser consider this:
- In case you have a content security policy, add `https://www.googleapis.com/` to the allowed `connect-src` sources. e.g.
  
  ```html
      <meta http-equiv="Content-Security-Policy" content="default-src 'self'; connect-src 'self' https://www.googleapis.com/; script-src 'self'"> 
  ```
   
- The request to the upload url should be a `PUT`. e.g.
```javascript
      return fetch(gcsUploadUrl, {
          method: 'PUT',
          headers: { 'Content-Type': file.type },
          body: file,
          mode: 'cors',
      }).then({
        //...
      });
```

- Here is an example of the GCS upload response. **You should use the property `name` to generate the download url**.

```$json
{  
   "kind":"storage#object",
   "id":"myexample.appspot.com/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/report.pdf/1525152367670967",
   "selfLink":"https://www.googleapis.com/storage/v1/b/myexample.appspot.com/o/attachments%2F7ca3794f-ca91-473a-b4b0-07f17e5f8c74%2Freport.pdf",
   "name":"attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/report.pdf",
   "bucket":"myexample.appspot.com",
   "generation":"1525152367670967",
   "metageneration":"1",
   "contentType":"application/pdf",
   "timeCreated":"2018-05-01T05:26:07.668Z",
   "updated":"2018-05-01T05:26:07.668Z",
   "storageClass":"STANDARD",
   "timeStorageClassUpdated":"2018-05-01T05:26:07.668Z",
   "size":"1017701",
   "md5Hash":"i+fuDpdrLYJ+iaVCzCLsVw==",
   "mediaLink":"https://www.googleapis.com/download/storage/v1/b/myexample.appspot.com/o/attachments%2F7ca3794f-ca91-473a-b4b0-07f17e5f8c74%2Freport.pdf?generation=1525152367670967&alt=media",
   "crc32c":"lCnLNg==",
   "etag":"CLft6ajj49oCEAE="
}
```
