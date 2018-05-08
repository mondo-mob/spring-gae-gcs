package com.threeweeks.spring.cloudstorage;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExistingBeansConfig.class, SpringGaeGcsConfiguration.class})
@ActiveProfiles(profiles = "gae")
public class AppEngineBeanCreationWithExistingBeansTest {

    @Autowired
    private HttpTransport httpTransport;

    @Autowired
    private JsonFactory jsonFactory;

    @Test
    public void testExistingHttpTransportIsUsed() {
        assertTrue(httpTransport instanceof MockHttpTransport);
    }

    @Test
    public void testExistingJsonFactoryIsUsed() {
        assertTrue(jsonFactory instanceof MockJsonFactory);
    }

}

@Configuration
@TestPropertySource("classpath:application.properties")
class ExistingBeansConfig {

    @Bean
    public HttpTransport getHttpTransport() {
        return new MockHttpTransport();
    }

    @Bean
    public JsonFactory getJsonFactory() {
        return new MockJsonFactory();
    }

}

class MockHttpTransport extends HttpTransport {

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return null;
    }
}

class MockJsonFactory extends JsonFactory {

    @Override
    public JsonParser createJsonParser(InputStream in) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(InputStream in, Charset charset) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(String value) throws IOException {
        return null;
    }

    @Override
    public JsonParser createJsonParser(Reader reader) throws IOException {
        return null;
    }

    @Override
    public JsonGenerator createJsonGenerator(OutputStream out, Charset enc) throws IOException {
        return null;
    }

    @Override
    public JsonGenerator createJsonGenerator(Writer writer) throws IOException {
        return null;
    }
}
