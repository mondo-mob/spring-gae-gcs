package com.mondomob.spring.cloudstorage.apiclient;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.common.net.UrlEscapers;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.geronimo.mail.util.Base64;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;


/**
 * Google Cloud Storage client that operates via the much fuller-featured JSON API. Requests occur
 * via the {@link HttpRequestFactory} which must be configured via your
 * <code>ApplicationModule</code>.
 *
 * @see <a href="https://cloud.google.com/storage/docs/json_api/">https://cloud.google.com/storage/docs/json_api/</a>
 */
public class GcsJsonApiClient {
    private static final String BASE_GOOGLE_API_URL = "https://www.googleapis.com";
    private static final String BASE_GOOGLE_STORAGE_URL = "https://storage.googleapis.com";
    private static final String HTTP_METHOD = "GET";
    private static final int DEAFULT_EXPIRY_DURATION_MINUTES = 10;
    static final Duration DEFAULT_EXPIRY_DURATION = Duration.ofMinutes(DEAFULT_EXPIRY_DURATION_MINUTES);

    protected final HttpRequestFactory httpRequestFactory;
    protected final AppIdentityService appIdentityService;

    public GcsJsonApiClient(HttpRequestFactory httpRequestFactory, AppIdentityService appIdentityService) {
        this.httpRequestFactory = httpRequestFactory;
        this.appIdentityService = appIdentityService;
    }

    /**
     * Initiate a resumable upload direct to the cloud storage API.
     *
     * @param bucket      the cloud storage bucket to upload to
     * @param name        the name of the resource that will be uploaded
     * @param contentType the resource's content/mime type
     * @return the upload URL
     * @see <a href="https://cloud.google.com/storage/docs/json_api/v1/how-tos/resumable-upload">Google Cloud Storage JSON API Overview</a>
     */
    public String initiateResumableUpload(String bucket, String name, String contentType) {
        return initiateResumableUpload(bucket, name, contentType, null);
    }

    public String initiateResumableUpload(String bucket, String folder, String filename, String contentType, String origin) {
        return initiateResumableUpload(
                bucket,
                String.format("%s/%s", folder, filename),
                contentType,
                origin);
    }

    /**
     * Initiate a resumable upload direct to the cloud storage API. Providing an origin will enable
     * CORS requests to the upload URL from the specified origin.
     *
     * @param bucket      the cloud storage bucket to upload to
     * @param name        the name of the resource that will be uploaded
     * @param contentType the resource's content/mime type
     * @param origin      the origin to allow for CORS requests
     * @return the upload URL
     * @see <a href="https://cloud.google.com/storage/docs/json_api/v1/how-tos/resumable-upload">Performing a Resumable Upload</a>
     */
    public String initiateResumableUpload(String bucket, String name, String contentType, String origin) {
        String uploadUrl = String.format("%s/upload/storage/v1/b/%s/o", BASE_GOOGLE_API_URL, bucket);

        GenericUrl url = new GenericUrl(uploadUrl);
        url.put("uploadType", "resumable");
        url.put("name", name);

        HttpHeaders headers = new HttpHeaders();
        headers.put("X-Upload-Content-Type", contentType);
        if (origin != null) {
            headers.put("Origin", origin);  // Add origin header for CORS support
        }

        HttpResponse response;
        try {
            response = httpRequestFactory
                    .buildPostRequest(url, null)
                    .setHeaders(headers)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot initiate upload: %s", e.getMessage()), e);
        }

        return response.getHeaders().getLocation();
    }

    /**
     * Generate a signed URL which can be used to access a resource without a Google account. Links
     * expire after a set time period (default {@value DEAFULT_EXPIRY_DURATION_MINUTES} minutes).
     *
     * @param bucket             the bucket where the resource is stored
     * @param name               the resource name
     * @param expiryDuration     the duration until the link expires
     * @return a signed URL for accessing a resource
     */
    public String generateSignedUrl(String bucket, String name, Duration expiryDuration) {
        return generateSignedUrlWithExpiry(bucket, name, expiryDuration).getUrl();
    }

    /**
     * Generate a signed URL which can be used to access a resource without a Google account. Links
     * expire after a set time period (default {@value DEAFULT_EXPIRY_DURATION_MINUTES} minutes).
     *
     * @param bucket             the bucket where the resource is stored
     * @param name               the resource name
     * @param expiryDuration     the duration until the link expires
     * @return an object containing the signed url and the expiration date time
     * @see <a href="https://cloud.google.com/storage/docs/access-control/signed-urls#signing-gae">Signed URLs</a>
     */
    public SignedUrl generateSignedUrlWithExpiry(String bucket, String name, Duration expiryDuration) {
        String canonicalizedResource = String.format("/%s/%s", bucket, name);
        OffsetDateTime expiryDateTime = getExpiryDateTime(expiryDuration);
        long expires = expiryDateTime.toEpochSecond();
        String signature = signRequest(canonicalizedResource, expires);
        String googleAccessId = getGoogleAccessId();

        String queryString = String.format("?GoogleAccessId=%s&Expires=%s&Signature=%s", UrlEscapers.urlFormParameterEscaper().escape(googleAccessId), expires, UrlEscapers.urlFormParameterEscaper().escape(signature));

        String url = String.format("%s%s%s",
                BASE_GOOGLE_STORAGE_URL,
                canonicalizedResource,
                queryString);

        return new SignedUrl(url, expiryDateTime);
    }

    protected String getGoogleAccessId() {
        return appIdentityService.getServiceAccountName();
    }

    OffsetDateTime getExpiryDateTime(Duration expiryDuration) {
        return OffsetDateTime.now().plus(ObjectUtils.defaultIfNull(expiryDuration, DEFAULT_EXPIRY_DURATION));
    }

    private String signRequest(String canonicalizedResource, long expiration) {
        byte[] data = String.format(
                "%s\n%s\n%s\n%s\n%s",
                HTTP_METHOD,
                "",  // Content MD5 is not required but could added for extra security
                "",  // Content Type is optional and best left out as it isn't included by default in browser GET requests
                expiration,
                canonicalizedResource).getBytes();

        byte[] signature = sign(data);
        return new String(Base64.encode(signature));
    }

    protected byte[] sign(byte[] data) {
        byte[] signature;
        AppIdentityService.SigningResult signingResult = appIdentityService.signForApp(data);
        signature = signingResult.getSignature();
        return signature;
    }
}
