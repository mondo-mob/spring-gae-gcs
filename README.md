# spring-gae-gcs
A library for integrating Google AppEngine and Spring Boot applications with the JSON Api of Google Cloud Storage to upload and retrieve files.

**Please note that this library is in an early stage and we cannot guarantee non-breaking changes just yet between releases.**

Overview
--------

This library allows you to generate urls to interact with Google Cloud Storage from a Springboot GAE application.

To do so, you can request an upload url for a file, and then upload the file directly from the browser to GCS. 
Later you could request a signed download url for that file. 

Install
-------
This library is not published yet. To use it follow these steps:

1. Clone this repo and navigate to it.
2. Run `./gradlew install` 
3. In your project, add this to your gradle dependencies: `compile 'com.threewks.spring:spring-gae-gcs:1.0-SNAPSHOT'`


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
- `GcsJsonApiClient`: Lower level client for interacting with the JSON api. You mostly will use the client.

Configuration
-------------

The following **mandatory** configuration options must be set in your application.properties file:

- `app.host` - a complete URL to the host (eg: `http://www.example.com`).
- `gcs.defaultBucket` - The gcs bucket that should be used to upload the files (eg: `example.appspot.com`).

The following **optional** configuration options can be set in your application.properties file:
- `gcs.attachmentFolder` - The folder that will be used to upload the files. If not provided, `attachments` will be used.
- `devCredentialsFile` - The name of the file with the service account credentials for local dev (See below).
If not provided, `/dev-gcs-credentials.json` will be used.
- `gcs.gaeProfileName` - The name of the [Spring Profile](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html) to indicate that the app is running in the Google cloud platform.
By default `gae` will be used. `!gae` will be used to indicate local development.


Local development
------------------
To be able to upload files from your local environment, you need to generate a JSON key from the default service account of the AppEngine project.
The key should be stored in the resources folder with the name: `dev-gcs-credentials.json`

To obtain the JSON key follow this [guide](https://cloud.google.com/iam/docs/creating-managing-service-account-keys)

Dependencies
------------

The library requires two dependencies. If they are not provided, default ones for AppEngine will be used:

- `com.google.api.client.http.HttpTransport` If not provided, will use: `com.google.api.client.extensions.appengine.http.UrlFetchTransport`
- `com.google.api.client.json.JsonFactory` If not provided, it will use: `com.google.api.client.json.gson.GsonFactory`


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

@RequestMapping(path = "/downloadUrl", method = POST)
public DownloadFileResponseDto downloadFile(@RequestBody DownloadFileRequestDto dto) {
    String download = gcsJsonApiService.getDownloadUrl(dto.getGcsName());
    return new DownloadFileResponseDto(download);
}
```                

In the previous example, the `gcsName` used to generate the downloadUrl, is obtained from the GCS response after uploading the file.

Here is an example of the GCS response. **You should use the property `name` to generate the download url**.

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
