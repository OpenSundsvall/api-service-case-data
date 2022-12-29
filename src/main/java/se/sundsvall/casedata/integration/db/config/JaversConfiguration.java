package se.sundsvall.casedata.integration.db.config;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sundsvall.casedata.api.filter.IncomingRequestFilter;

@Configuration
public class JaversConfiguration {

    private final IncomingRequestFilter incomingRequestFilter;

    public JaversConfiguration(IncomingRequestFilter incomingRequestFilter) {
        this.incomingRequestFilter = incomingRequestFilter;
    }

    @Bean
    public AuthorProvider provideJaversAuthor() {
        return new SimpleAuthorProvider(incomingRequestFilter);
    }

    private record SimpleAuthorProvider(IncomingRequestFilter incomingRequestFilter) implements AuthorProvider {

        @Override
            public String provide() {
                return incomingRequestFilter.getAdUser();
            }
        }
}
