package com.threeweeks.spring.cloudstorage.apiclient;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.appengine.api.appidentity.AppIdentityService;
import org.apache.geronimo.mail.util.Base64;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


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
    private static final int TEN_MINUTES = 10;

    protected final HttpRequestFactory httpRequestFactory;
    protected final AppIdentityService appIdentityService;
    protected final GoogleCredential gcsCredential;

    public GcsJsonApiClient(HttpRequestFactory httpRequestFactory,
                            AppIdentityService appIdentityService,
                            GoogleCredential gcsCredential) {
        this.httpRequestFactory = httpRequestFactory;
        this.appIdentityService = appIdentityService;
        this.gcsCredential = gcsCredential;
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
     * expire after a set time period (default 10 minutes).
     *
     * @param bucket             the bucket where the resource is stored
     * @param name               the resource name
     * @param minutesTillExpires the number of minutes from now till link expires. Defaults to 10
     * @return a signed URL for accessing a resource
     * @see <a href="https://cloud.google.com/storage/docs/access-control/signed-urls#signing-gae">Signed URLs</a>
     */
    public String generateSignedUrl(String bucket, String name, Integer minutesTillExpires) {
        String canonicalizedResource = String.format("/%s/%s", bucket, name);
        long expires = getExpiration(minutesTillExpires);
        String signature = signRequest(canonicalizedResource, expires);
        String googleAccessId = getGoogleAccessId();

        String queryString = String.format("?GoogleAccessId=%s&Expires=%s&Signature=%s", encodeQueryParam(googleAccessId), expires, encodeQueryParam(signature));

        return String.format("%s%s%s",
            BASE_GOOGLE_STORAGE_URL,
            canonicalizedResource,
            queryString);
    }

    protected String encodeQueryParam(String val) {
        try {
            return UriUtils.encodeQueryParam(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getGoogleAccessId() {
        return appIdentityService.getServiceAccountName();
    }

    long getExpiration(Integer minutesTillExpires) {
        minutesTillExpires = minutesTillExpires == null ? TEN_MINUTES : minutesTillExpires;
        return LocalDateTime.now().plusMinutes(minutesTillExpires).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000;
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
