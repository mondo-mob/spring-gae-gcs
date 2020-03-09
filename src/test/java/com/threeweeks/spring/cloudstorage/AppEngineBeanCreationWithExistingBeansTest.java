package com.threeweeks.spring.cloudstorage;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExistingBeansConfig.class, SpringGaeGcsAutoConfiguration.class})
@ActiveProfiles(profiles = "gae")
public class AppEngineBeanCreationWithExistingBeansTest {

    @Autowired
    private HttpTransport httpTransport;

    @Test
    public void testExistingHttpTransportIsUsed() {
        assertTrue(httpTransport instanceof MockHttpTransport);
    }


}

@Configuration
@TestPropertySource("classpath:application.properties")
class ExistingBeansConfig {

    @Bean
    public HttpTransport getHttpTransport() {
        return new MockHttpTransport();
    }

}

class MockHttpTransport extends HttpTransport {

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return null;
    }
}
