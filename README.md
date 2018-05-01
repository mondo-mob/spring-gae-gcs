# spring-gae-gcs
A library for integrating Google AppEngine and Spring Boot applications with Google Cloud Storage to upload and retrieve files.



###Upload Url response:
```$json
{  
   "kind":"storage#object",
   "id":"busker-spring-react-dev.appspot.com/attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/report.pdf/1525152367670967",
   "selfLink":"https://www.googleapis.com/storage/v1/b/busker-spring-react-dev.appspot.com/o/attachments%2F7ca3794f-ca91-473a-b4b0-07f17e5f8c74%2Freport.pdf",
   "name":"attachments/7ca3794f-ca91-473a-b4b0-07f17e5f8c74/report.pdf",
   "bucket":"busker-spring-react-dev.appspot.com",
   "generation":"1525152367670967",
   "metageneration":"1",
   "contentType":"application/pdf",
   "timeCreated":"2018-05-01T05:26:07.668Z",
   "updated":"2018-05-01T05:26:07.668Z",
   "storageClass":"STANDARD",
   "timeStorageClassUpdated":"2018-05-01T05:26:07.668Z",
   "size":"1017701",
   "md5Hash":"i+fuDpdrLYJ+iaVCzCLsVw==",
   "mediaLink":"https://www.googleapis.com/download/storage/v1/b/busker-spring-react-dev.appspot.com/o/attachments%2F7ca3794f-ca91-473a-b4b0-07f17e5f8c74%2Freport.pdf?generation=1525152367670967&alt=media",
   "crc32c":"lCnLNg==",
   "etag":"CLft6ajj49oCEAE="
}
```