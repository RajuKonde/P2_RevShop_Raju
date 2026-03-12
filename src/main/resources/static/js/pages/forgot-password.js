"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = window.RevShopApp;

    const form = document.getElementById("forgotPasswordForm");
    const emailInput = document.getElementById("emailInput");
    const emailStatusBox = document.getElementById("emailStatusBox");
    const submitButton = form.querySelector('button[type="submit"]');
    const defaultButtonText = submitButton ? submitButton.textContent : "Send Reset Link";
    let isSubmitting = false;

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (isSubmitting) {
            return;
        }

        const email = emailInput.value.trim();
        if (!email) {
            app.showToast("Email is required", "error");
            return;
        }

        try {
            isSubmitting = true;
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "Sending...";
            }

            const data = await app.api("/auth/password/forgot", {
                method: "POST",
                auth: false,
                body: { email }
            });

            form.reset();
            emailStatusBox.classList.remove("d-none");
            app.showToast(
                (data && data.note) || "If the account exists, a reset link has been sent",
                "success"
            );
        } catch (error) {
            app.showToast(error.message || "Failed to send reset link", "error");
        } finally {
            isSubmitting = false;
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = defaultButtonText;
            }
        }
    });
});
