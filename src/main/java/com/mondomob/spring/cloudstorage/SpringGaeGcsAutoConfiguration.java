package com.mondomob.spring.cloudstorage;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.mondomob.spring.cloudstorage.apiclient.GcsJsonApiClient;
import com.mondomob.spring.cloudstorage.apiclient.LocalGcsJsonApiClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class SpringGaeGcsAutoConfiguration {
    private static final List<String> STORAGE_SCOPES = singletonList("https://www.googleapis.com/auth/devstorage.full_control");
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringGaeGcsAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(HttpTransport.class)
    public HttpTransport getHttpTransport() {
        LOGGER.info("HttpTransport bean not found. Default UrlFetchTransport.getDefaultInstance()");
        return UrlFetchTransport.getDefaultInstance();
    }


    @Profile("!gae")
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @ConditionalOnMissingBean
    @ConditionalOnResource(resources = "classpath:${gcs.dev-credentials-file:/dev-gcs-credentials.json}")
    public GcsJsonApiClient getLocalGcsClient(HttpTransport httpTransport,
            @Value("${gcs.dev-credentials-file:/dev-gcs-credentials.json}")
                    String devCredentialsFile) {
        LOGGER.info("Starting gcs configuration in Local env.");
        ServiceAccountCredentials credentials = getCredentialsFromFile(devCredentialsFile);

        return new LocalGcsJsonApiClient(getHttpRequestFactory(credentials, httpTransport), getAppIdentityService(), credentials);
    }

    @Bean
    @ConditionalOnMissingBean
    public GcsJsonApiClient getGaeGcsClient(HttpTransport httpTransport) {
        LOGGER.info("Starting gcs configuration in GAE env.");
        GoogleCredentials googleCredential = getGaeGoogleCredential();

        return new GcsJsonApiClient(getHttpRequestFactory(googleCredential, httpTransport), getAppIdentityService());
    }

    @Bean
    @ConditionalOnProperty("gcs.default-bucket")
    @ConditionalOnMissingBean
    public GcsJsonApiService gcsJsonApiService(GcsJsonApiClient cloudStorage,
            @Value("${gcs.default-bucket}") String defaultBucket,
            @Value("${app.host}") String host,
            @Value("#{'${gcs.attachment-folder:attachments}'}")
                    String gcsAttachmentFolder) {
        Assert.isTrue(StringUtils.isNotBlank(host), "${app.host} must have a value");

        return new GcsJsonApiService(cloudStorage, defaultBucket, host, gcsAttachmentFolder);
    }


    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @ConditionalOnMissingBean
    @ConditionalOnProperty("gcs.default-bucket")
    @ConditionalOnResource(resources = "classpath:${gcs.dev-credentials-file:/dev-gcs-credentials.json}")
    @Profile({"!gae"})
    public CloudStorageService localCloudStorageService(@Value("${gcs.default-bucket}") String defaultBucket,
            @Value("${gcs.dev-credentials-file:/dev-gcs-credentials.json}") String gcsCredentials,
            @Value("${app.id}") String projectId) {
        Assert.isTrue(StringUtils.isNotBlank(projectId), "${app.id} must have a value");

        return new CloudStorageService(defaultBucket, gcsCredentials, projectId);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("gcs.default-bucket")
    public CloudStorageService cloudStorageService(@Value("${gcs.default-bucket}") String defaultBucket) {
        return new CloudStorageService(defaultBucket);
    }

    private ServiceAccountCredentials getCredentialsFromFile(@Value("${gcs.dev-credentials-file:/dev-gcs-credentials.json}") String devCredentialsFile) {
        try (InputStream jsonCredentials = getClass().getResourceAsStream(devCredentialsFile)) {
            return ServiceAccountCredentials.fromStream(jsonCredentials);
        } catch (IOException e) {
            String msg = String.format("Cloud storage client configuration failed. Ensure you have a local credentials file created in src/main/resources/%s." +
                            "See https://developers.google.com/identity/protocols/application-default-credentials. Alternatively you can remove " +
                            "this %s from your ApplicationModule if you do not require Google Cloud Storage (e.g. file uploads).",
                    devCredentialsFile, getClass().getSimpleName());
            throw new RuntimeException(msg, e);
        }
    }


    private GoogleCredentials getGaeGoogleCredential() {
        try {
            return ServiceAccountCredentials.getApplicationDefault()
                    .createScoped(STORAGE_SCOPES);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cloud storage client configuration failed: %s", e.getMessage()), e);
        }
    }

    private AppIdentityService getAppIdentityService() {
        return AppIdentityServiceFactory.getAppIdentityService();
    }

    private HttpRequestFactory getHttpRequestFactory(Credentials credentials, HttpTransport httpTransport) {
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
        return httpTransport.createRequestFactory(requestInitializer);
    }

}
