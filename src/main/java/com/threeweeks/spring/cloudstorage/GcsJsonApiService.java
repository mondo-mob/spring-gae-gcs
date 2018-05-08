package com.threeweeks.spring.cloudstorage;

import com.google.api.client.util.Strings;
import com.google.common.net.UrlEscapers;
import com.threeweeks.spring.cloudstorage.apiclient.GcsJsonApiClient;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

public class GcsJsonApiService {
    public static final int DEFAULT_LINK_EXPIRY_DURATION_MINUTES = 2;

    private final GcsJsonApiClient cloudStorage;
    private final String gcsDefaultBucket;
    private final String host;
    private final String defaultAttachmentsFolder;

    public GcsJsonApiService(GcsJsonApiClient cloudStorage, String gcsDefaultBucket, String host,
                             String defaultAttachmentsFolder) {
        this.cloudStorage = cloudStorage;
        this.gcsDefaultBucket = gcsDefaultBucket;
        this.host = host;
        this.defaultAttachmentsFolder = defaultAttachmentsFolder;
    }

    /**
     * Generate a cloud storage upload url under the default attachments folder.
     *
     * @param type   File MIME type.
     * @param origin Upload origin. (The system that the file will be uploaded from).
     * @return Upload url.
     */
    public String getUploadUrl(String type, String filename, String origin) {
        return getUploadUrl(defaultAttachmentsFolder, type, filename, origin);
    }

    /**
     * Generate a cloud storage upload url.
     *
     * @param folder Folder to store the attachment under.
     * @param type   File MIME type.
     * @param filename The file name to store the file as and ultimately download it as.
     * @param origin Upload origin. (The system that the file will be uploaded from).
     * @return Upload url.
     */
    public String getUploadUrl(String folder, String type, String filename, String origin) {
        Validate.notBlank(folder, "folder required");
        Validate.notBlank(filename, "filename required");
        String originHost = Strings.isNullOrEmpty(origin) ? host : origin;
        String base = buildBasePath(folder);
        return cloudStorage.initiateResumableUpload(gcsDefaultBucket, base, filename, type, originHost);

    }

    /**
     * Get a signed url to view an attachment. The url will last 24 hours. The file name portion of the id is url escaped internally.
     *
     * @param gcsObjectName The id of the attachment.
     * @return Url
     */
    public String getDownloadUrl(String gcsObjectName) {
        return cloudStorage.generateSignedUrl(gcsDefaultBucket, getFullPathFromObjectName(gcsObjectName), DEFAULT_LINK_EXPIRY_DURATION_MINUTES);
    }

    /**
     * Build a unique base path. This is {@code protected} to allow any sub-classes to override strategy if they extend. By default it uses
     * the specified base path, with a UUID "sub-folder". The base path is unique so that we can preserve the filename that the user specified.
     *
     * @param folder
     * @return unique base path.
     */
    String buildBasePath(String folder) {
        return String.format("%s/%s", folder, UUID.randomUUID().toString());
    }

    String getGcsDefaultBucket() {
        return gcsDefaultBucket;
    }

    String getDefaultAttachmentsFolder() {
        return defaultAttachmentsFolder;
    }

    static String getFullPathFromObjectName(String gcsObjectName) {
        String filename = FilenameUtils.getName(gcsObjectName);
        String path = FilenameUtils.getFullPath(gcsObjectName);

        return FilenameUtils.concat(path, UrlEscapers.urlFragmentEscaper().escape(filename));
    }

}
