package com.revshop.controller;

import org.junit.Test;

public class TestControllerTest {

    @Test
    public void secureEndpoint_returnsSuccessPayload() {
        new RemainingControllerCoverageTest().testController_secureEndpoint_returnsSuccessPayload();
    }
}
