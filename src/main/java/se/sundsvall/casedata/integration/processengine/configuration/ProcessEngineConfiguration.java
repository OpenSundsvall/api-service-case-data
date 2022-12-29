package se.sundsvall.casedata.integration.processengine.configuration;


import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

@Import(FeignConfiguration.class)
public class ProcessEngineConfiguration {
    public static final String REGISTRATION_ID = "process-engine";

    private final ProcessEngineProperties processEngineProperties;

    public ProcessEngineConfiguration(ProcessEngineProperties processEngineProperties) {
        this.processEngineProperties = processEngineProperties;
    }

    @Bean
    FeignBuilderCustomizer feignBuilderCustomizer() {
        return FeignMultiCustomizer.create()
                .withErrorDecoder(new ProblemErrorDecoder(REGISTRATION_ID))
                .withRetryableOAuth2InterceptorForClientRegistration(ClientRegistration.withRegistrationId(REGISTRATION_ID)
                        .tokenUri(processEngineProperties.getOauth2TokenUrl())
                        .clientId(processEngineProperties.getOauth2ClientId())
                        .clientSecret(processEngineProperties.getOauth2ClientSecret())
                        .authorizationGrantType(new AuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()))
                        .build())
                .composeCustomizersToOne();
    }

}