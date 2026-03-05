package com.revshop.service.impl;

import org.junit.Test;

public class NotificationServiceImplTest {

    @Test
    public void getUnreadCount_returnsDaoCount() {
        new RemainingServiceImplCoverageTest().notificationServiceImpl_getUnreadCount_returnsDaoCount();
    }
}
