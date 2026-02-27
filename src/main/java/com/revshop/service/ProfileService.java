package com.revshop.service;

import com.revshop.dto.profile.ProfileResponse;
import com.revshop.dto.profile.UpdateBuyerProfileRequest;
import com.revshop.dto.profile.UpdateSellerProfileRequest;

public interface ProfileService {

    ProfileResponse getMyProfile(String email);

    ProfileResponse updateBuyerProfile(String email, UpdateBuyerProfileRequest request);

    ProfileResponse updateSellerProfile(String email, UpdateSellerProfileRequest request);
}
