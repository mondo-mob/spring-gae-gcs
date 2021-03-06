# spring-gae-gcs
A library for integrating Google AppEngine and Spring Boot applications with the JSON Api of Google Cloud Storage to upload and retrieve files.

## Release history
See the [Change Log](./CHANGELOG.md).

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
  <groupId>com.mondomob.spring</groupId>
  <artifactId>spring-gae-gcs</artifactId>
  <version>VERSION-HERE</version>
</dependency>
```

Gradle:
```groovy
compile 'com.mondomob.spring:spring-gae-gcs:VERSION-HERE'
```


Setup
-----

This library automatically configures some beans based on configuration that you supply. Simply including the dependency and configuring properties
will be enough to get you going.

This will create the following bean that you can inject. All beans are only created if one does not already exist of the same type:
- `GcsJsonApiClient`: Lower level client for interacting with the JSON api. This does not 
  require any properties to be set for the bean to be created.
- `GcsJsonApiService`: Provides methods to generate upload and download URLs with a simpler interface to the raw client. This requires `app.host` and 
   `gcs.default-bucket` properties before the bean is created.
- `CloudStorageService`: Service for dealing with files in GCS. This requires `gcs.default-bucket` before the bean is created.

Configuration
-------------

The configuration properties used are described below:
- `app.host` - a complete URL to the host (eg: `http://www.example.com`).
- `gcs.default-bucket` - The gcs bucket that should be used to upload the files (eg: `example.appspot.com`).
- `gcs.attachment-folder` - The folder that will be used to upload the files in API. If not provided, `attachments` will be used.
- `gcs.dev-credentials-file` - The name of the file with the service account credentials for local dev (See below). 
   If not provided, `/dev-gcs-credentials.json` will be the file that the library attempts to look for this file in.


Local development
------------------
**BETA:** To be able to upload files from your local environment you should have [Google Cloud Tools SDK](https://cloud.google.com/sdk/install) installed and ideally
be authenticated with a user that has read/write access to a storage bucket in a GCP project. The local application will automatically use the credentials
of the local user to interact with the bucket.

If you prefer to have explicit JSON credentials for the local application to use then you need to generate a JSON key from the default service account of the AppEngine project.
The key should be stored in the resources folder with the name: `dev-gcs-credentials.json`. To obtain the JSON key follow this [guide](https://cloud.google.com/iam/docs/creating-managing-service-account-keys).

_For now you should use the JSON key method as the other method can have issues with signing URLs and is experimental._
The auto configuration will search for a local credentials file and if it does not find one it will configure with the assumption that the local credentials
are setup in the SDK as per the first option described above.

Spring profiles
-----------------
The library expects the `gae` profile to be active when the app is running in Google Cloud. `!gae` will be used to indicate local development. 

Dependencies
------------

The library requires two dependencies. If they are not provided, default ones for AppEngine will be used:

- `com.google.api.client.http.HttpTransport` If not provided, will use: `com.google.api.client.extensions.appengine.http.UrlFetchTransport`


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


Utility to upload files from the browser
-----------------------------------------
https://gist.github.com/afcastano/1826c65f0e75e571186666d1653d784a

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

## Misc

### Release to jCenter and Maven Central
We firstly release to bintray's jecenter so that it's available immediately to those who use this (better) repository. We have also setup
sync to maven central. Sync direct to maven central is a bit more cumbersome and there is a roughly 2-hourly job that syncs it up there.
Jcenter also has CDN among other improvements listed here: [Why should I use jcenter over Maven Central?](https://jfrog.com/knowledge-base/why-should-i-use-jcenter-over-maven-central/) 

Privileged users will have system properties for `bintray.user` and `bintray.key` defined to do this.

```
./gradlew clean bintrayUpload --info
```
The above command fails silently (as of version `1.8.0` of `gradle-bintray-plugin` so just make sure it didn't skip upload due to undefined key). 

If you are setup with correct privileges then that's it. No manual steps - sync to maven central also triggered.

**Note:** Be sure to update the README to reference the latest version in all places.

### Installing the Library
To install the library to your local maven repository, run the following:

```
./gradlew clean install
```
