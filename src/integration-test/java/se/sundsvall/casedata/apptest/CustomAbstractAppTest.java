package se.sundsvall.casedata.apptest;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import se.sundsvall.dept44.test.AbstractAppTest;

import java.util.List;

public class CustomAbstractAppTest extends AbstractAppTest {

    @Override
    public void verifyAllStubs() {
        List<LoggedRequest> unmatchedRequests = this.wiremock.findAllUnmatchedRequests();
        if (!unmatchedRequests.isEmpty()) {
            List<String> unmatchedUrls = unmatchedRequests.stream().map(LoggedRequest::getUrl).toList();
            throw new AssertionError(String.format("The following requests was not matched: %s", unmatchedUrls));
        }
    }
}
