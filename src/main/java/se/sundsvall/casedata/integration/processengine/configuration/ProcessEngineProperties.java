package se.sundsvall.casedata.integration.processengine.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "integration.process-engine")
public class ProcessEngineProperties {
    private String url;
    private String oauth2TokenUrl;
    private String oauth2ClientId;
    private String oauth2ClientSecret;
}
