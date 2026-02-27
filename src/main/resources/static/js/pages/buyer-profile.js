"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = window.RevShopApp;
    if (!app.ensureRole("BUYER")) return;

    app.mountShell({
        active: "buyer-profile",
        title: "Buyer Profile",
        subtitle: "Keep your personal details updated for seamless checkout."
    });

    const emailText = document.getElementById("emailText");
    const roleText = document.getElementById("roleText");
    const statusText = document.getElementById("statusText");

    const form = document.getElementById("buyerProfileForm");
    const firstNameInput = document.getElementById("firstNameInput");
    const lastNameInput = document.getElementById("lastNameInput");
    const phoneInput = document.getElementById("phoneInput");
    const addressInput = document.getElementById("addressInput");

    function renderProfile(profile) {
        emailText.textContent = profile.email || "-";
        roleText.textContent = profile.role || "-";
        statusText.textContent = profile.active ? "Active" : "Inactive";

        firstNameInput.value = profile.firstName || "";
        lastNameInput.value = profile.lastName || "";
        phoneInput.value = profile.phone || "";
        addressInput.value = profile.address || "";
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
            firstName: firstNameInput.value.trim(),
            lastName: lastNameInput.value.trim(),
            phone: phoneInput.value.trim() || null,
            address: addressInput.value.trim() || null
        };

        if (!payload.firstName || !payload.lastName) {
            app.showToast("First name and last name are required", "error");
            return;
        }

        try {
            const profile = await app.api("/profile/buyer", {
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
