package com.threeweeks.spring.cloudstorage;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrated from the com.threewks.spring:spring-boot-gae library.
 */
public class CloudStorageService {

    private static final String PUBLIC_CACHE_CONTROL = "public, max-age=0";

    private final String defaultBucketName;
    private final Storage storage;

    public CloudStorageService(String defaultBucketName) {
        this.defaultBucketName = defaultBucketName;
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public CloudStorageService(String defaultBucketName, String credentialsFile, String projectId) {
        Credentials googleCredential;
        InputStream inputStream = StorageOptions.class.getResourceAsStream(credentialsFile);

        try {
            googleCredential = ServiceAccountCredentials.fromStream(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Can not read local gcs credentials file %s", credentialsFile), e);
        }

        this.storage = StorageOptions.newBuilder().setCredentials(googleCredential)
                .setProjectId(projectId).build().getService();
        this.defaultBucketName = defaultBucketName;
    }

    public Blob writeFile(byte[] data, String objectName) {
        return writeFile(defaultBucketName, data, objectName);
    }

    public Blob writeFile(String bucketName, byte[] data, String objectName) {
        return writeFile(bucketName, data, objectName, false);
    }

    public Blob writeFile(byte[] data, String objectName, boolean publicReadable) {
        return writeFile(defaultBucketName, data, objectName, publicReadable);
    }

    public Blob writeFile(String bucketName, byte[] data, String objectName, boolean publicReadable) {
        BlobId gcsFilename = blobId(bucketName, objectName);
        BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(gcsFilename);
        List<Storage.BlobTargetOption> blobTargetOptions = new ArrayList<>();

        if (publicReadable) {
            blobTargetOptions.add(Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
            blobInfoBuilder.setCacheControl(PUBLIC_CACHE_CONTROL);
        }

        return storage.create(
                blobInfoBuilder.build(),
                data,
                blobTargetOptions.toArray(new Storage.BlobTargetOption[0]));
    }

    public InputStream readFile(String objectName) {
        return readFile(defaultBucketName, objectName);
    }

    public InputStream readFile(String bucketName, String objectName) {
        BlobId gcsFilename = blobId(bucketName, objectName);
        ReadChannel readChannel = storage.reader(gcsFilename);
        return Channels.newInputStream(readChannel);
    }

    public void copyFile(String fromObjectName, String toObjectName, boolean publicReadable) {
        copyFile(defaultBucketName, fromObjectName, toObjectName, publicReadable);
    }

    public void copyFile(String bucketName, String fromObjectName, String toObjectName, boolean publicReadable) {
        copyFile(bucketName, fromObjectName, bucketName, toObjectName, publicReadable);
    }

    public void copyFile(String fromBucketName, String fromObjectName, String toBucketName, String toObjectName, boolean publicReadable) {
        BlobId sourceObject = blobId(fromBucketName, fromObjectName);
        BlobId targetObject = blobId(toBucketName, toObjectName);
        storage.copy(Storage.CopyRequest.of(sourceObject, targetObject));

        if (publicReadable) {
            makePublic(targetObject);
        }
    }

    public void moveFile(String fromObjectName, String toObjectName, boolean publicReadable) {
        copyFile(fromObjectName, toObjectName, publicReadable);
        deleteFile(fromObjectName);
    }

    public boolean fileExists(String objectName) {
        return fileExists(defaultBucketName, objectName);
    }

    public boolean fileExists(String bucketName, String objectName) {
        Blob blob = storage.get(blobId(bucketName, objectName));
        return blob != null && blob.exists();
    }

    public boolean deleteFile(String objectName) {
        return deleteFile(defaultBucketName, objectName);
    }

    public boolean deleteFile(String bucketName, String objectName) {
        return storage.delete(blobId(bucketName, objectName));
    }

    public Blob getBlob(String objectName) {
        return getBlob(defaultBucketName, objectName);
    }

    public Blob getBlob(String bucketName, String objectName) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        return storage.get(blobId);
    }

    private Blob makePublic(BlobId target) {
        return storage.update(
                BlobInfo.newBuilder(target).setCacheControl(PUBLIC_CACHE_CONTROL).build(),
                Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));
    }

    private BlobId blobId(String bucketName, String objectName) {
        return BlobId.of(bucketName, objectName);
    }
}

