"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const app = window.RevShopApp;
    if (!app.ensureRole("BUYER")) return;

    app.mountShell({
        active: "orders",
        title: "Order History",
        subtitle: "Track order and payment lifecycle for every purchase."
    });

    const ordersList = document.getElementById("ordersList");
    const emptyOrdersState = document.getElementById("emptyOrdersState");

    function statusClass(status) {
        if (!status) return "warning";
        if (status === "DELIVERED" || status === "CONFIRMED") return "success";
        if (status === "CANCELLED" || status === "FAILED") return "danger";
        return "warning";
    }

    function renderOrders(orders) {
        ordersList.innerHTML = "";
        if (!orders || orders.length === 0) {
            emptyOrdersState.classList.remove("d-none");
            return;
        }

        emptyOrdersState.classList.add("d-none");
        orders.forEach((order) => {
            const card = document.createElement("article");
            card.className = "market-card flat p-3 mb-3";
            card.innerHTML = `
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2">
                    <div>
                        <div class="fw-semibold">${app.escapeHtml(order.orderNumber)}</div>
                        <div class="market-muted small">${app.formatDateTime(order.createdAt)}</div>
                    </div>
                    <div class="text-end">
                        <span class="pill ${statusClass(order.status)}">${app.escapeHtml(order.status)}</span>
                        <div class="fw-semibold mt-1">${app.formatCurrency(order.totalAmount)}</div>
                    </div>
                </div>
                <div class="mt-2 small market-muted">
                    Payment: ${app.escapeHtml(order.paymentMethod || "-")}
                </div>
                <div class="mt-2">
                    <div class="small"><strong>Shipping:</strong> ${app.escapeHtml(order.shippingAddress || "-")}</div>
                    <div class="small"><strong>Billing:</strong> ${app.escapeHtml(order.billingAddress || "-")}</div>
                </div>
                <div class="mt-3">
                    <div class="small fw-semibold mb-1">Items</div>
                    ${(order.items || []).map(item => `
                        <div class="d-flex justify-content-between border-top py-2 small">
                            <span>${app.escapeHtml(item.productName)} x ${item.quantity}</span>
                            <span>${app.formatCurrency(item.lineTotal)}</span>
                        </div>
                    `).join("")}
                </div>
                <div class="mt-3 d-flex justify-content-end">
                    <button class="btn btn-outline-primary market-btn btn-sm payment-check-btn"
                            data-order-id="${order.orderId}">
                        Check Payment
                    </button>
                </div>
                <div class="small mt-2 market-muted payment-result" data-order-id="${order.orderId}"></div>
            `;
            ordersList.appendChild(card);
        });
    }

    async function loadOrders() {
        try {
            const orders = await app.api("/orders/my");
            renderOrders(orders);
        } catch (error) {
            app.showToast(error.message || "Failed to fetch orders", "error");
        }
    }

    ordersList.addEventListener("click", async (event) => {
        const button = event.target.closest(".payment-check-btn");
        if (!button) return;

        const orderId = Number(button.dataset.orderId);
        const resultNode = ordersList.querySelector(`.payment-result[data-order-id="${orderId}"]`);
        try {
            const payment = await app.api(`/payments/order/${orderId}`);
            resultNode.textContent = `Payment Status: ${payment.paymentStatus} | Order Status: ${payment.orderStatus}`;
            app.showToast("Payment fetched", "success");
        } catch (error) {
            resultNode.textContent = error.message || "Payment not available";
            app.showToast(error.message || "Payment fetch failed", "error");
        }
    });

    loadOrders();
});
