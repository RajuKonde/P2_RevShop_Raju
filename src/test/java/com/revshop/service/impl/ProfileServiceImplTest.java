package com.revshop.service.impl;

import org.junit.Test;

public class ProfileServiceImplTest {

    @Test
    public void getMyProfile_normalizesPublicImageUrl() {
        new RemainingServiceImplCoverageTest().profileServiceImpl_getMyProfile_normalizesPublicImageUrl();
    }
}
