package com.revshop.service.impl;

import org.junit.Test;

public class ReviewServiceImplTest {

    @Test
    public void getProductRatingSummary_aggregatesReviewMetrics() {
        new RemainingServiceImplCoverageTest().reviewServiceImpl_getProductRatingSummary_aggregatesReviewMetrics();
    }
}
