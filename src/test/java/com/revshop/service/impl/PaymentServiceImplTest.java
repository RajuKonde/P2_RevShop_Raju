package com.revshop.service.impl;

import org.junit.Test;

public class PaymentServiceImplTest {

    @Test
    public void getBuyerPayments_mapsStoredPayments() {
        new RemainingServiceImplCoverageTest().paymentServiceImpl_getBuyerPayments_mapsStoredPayments();
    }
}
