package uk.gov.companieshouse.lookup.helper;

import java.net.URI;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class LinkUtils {

    public Optional<String> resolveRelativeLink(String link) {
        if (isSafeRelativeLink(link)) {
            return Optional.of(link);
        }

        return Optional.empty();
    }

    public boolean isSafeRelativeLink(String link) {
        if (link == null || link.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(link);
            return uri.getScheme() == null
                    && uri.getHost() == null
                    && link.startsWith("/")
                    && !link.startsWith("//");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}


