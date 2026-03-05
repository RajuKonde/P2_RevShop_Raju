package com.revshop.service.impl;

import org.junit.Test;

public class WishlistServiceImplTest {

    @Test
    public void getWishlistStatus_detectsActiveWishlistItem() {
        new RemainingServiceImplCoverageTest().wishlistServiceImpl_getWishlistStatus_detectsActiveWishlistItem();
    }
}
