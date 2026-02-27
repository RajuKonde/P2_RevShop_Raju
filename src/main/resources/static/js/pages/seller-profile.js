"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = window.RevShopApp;
    if (!app.ensureRole("SELLER")) return;

    app.mountShell({
        active: "seller-profile",
        title: "Seller Profile",
        subtitle: "Keep business identity and contact details up to date."
    });

    const emailText = document.getElementById("emailText");
    const roleText = document.getElementById("roleText");
    const statusText = document.getElementById("statusText");

    const form = document.getElementById("sellerProfileForm");
    const businessNameInput = document.getElementById("businessNameInput");
    const gstNumberInput = document.getElementById("gstNumberInput");
    const phoneInput = document.getElementById("phoneInput");
    const businessAddressInput = document.getElementById("businessAddressInput");

    function renderProfile(profile) {
        emailText.textContent = profile.email || "-";
        roleText.textContent = profile.role || "-";
        statusText.textContent = profile.active ? "Active" : "Inactive";

        businessNameInput.value = profile.businessName || "";
        gstNumberInput.value = profile.gstNumber || "";
        phoneInput.value = profile.phone || "";
        businessAddressInput.value = profile.businessAddress || "";
    }

    async function loadProfile() {
        try {
            const profile = await app.api("/profile/me");
            renderProfile(profile);
        } catch (error) {
            app.showToast(error.message || "Failed to load profile", "error");
        }
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            businessName: businessNameInput.value.trim(),
            gstNumber: gstNumberInput.value.trim() || null,
            phone: phoneInput.value.trim() || null,
            businessAddress: businessAddressInput.value.trim() || null
        };

        if (!payload.businessName) {
            app.showToast("Business name is required", "error");
            return;
        }

        try {
            const profile = await app.api("/profile/seller", {
                method: "PUT",
                body: payload
            });
            renderProfile(profile);
            app.showToast("Profile updated successfully", "success");
        } catch (error) {
            app.showToast(error.message || "Failed to update profile", "error");
        }
    });

    loadProfile();
});
