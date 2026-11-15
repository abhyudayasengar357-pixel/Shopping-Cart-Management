/**
 * Online Shopping Cart & Order Management System
 * Interactive Client-Side JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize tooltips and popovers if Bootstrap is present
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));
    }

    // 2. Demo Login Quick-Fill Helper
    const quickFillBtns = document.querySelectorAll('.quick-fill-btn');
    quickFillBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            const user = btn.getAttribute('data-user');
            const pass = btn.getAttribute('data-pass');

            if (usernameInput && passwordInput) {
                usernameInput.value = user;
                passwordInput.value = pass;
                usernameInput.classList.add('border-primary');
                passwordInput.classList.add('border-primary');
                setTimeout(() => {
                    usernameInput.classList.remove('border-primary');
                    passwordInput.classList.remove('border-primary');
                }, 1000);
            }
        });
    });

    // 3. Auto-dismiss alerts after 5 seconds
    const autoAlerts = document.querySelectorAll('.alert-dismissible');
    autoAlerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });

    // 4. Quantity Steppers
    const qtyInputs = document.querySelectorAll('.qty-stepper');
    qtyInputs.forEach(stepper => {
        const minusBtn = stepper.querySelector('.btn-qty-minus');
        const plusBtn = stepper.querySelector('.btn-qty-plus');
        const input = stepper.querySelector('input[type="number"]');

        if (minusBtn && plusBtn && input) {
            minusBtn.addEventListener('click', () => {
                let val = parseInt(input.value) || 1;
                if (val > 1) {
                    input.value = val - 1;
                    input.dispatchEvent(new Event('change'));
                }
            });

            plusBtn.addEventListener('click', () => {
                let val = parseInt(input.value) || 1;
                let max = parseInt(input.getAttribute('max')) || 999;
                if (val < max) {
                    input.value = val + 1;
                    input.dispatchEvent(new Event('change'));
                }
            });
        }
    });

    // 5. Delete Confirmation Modals / Prompts
    const deleteForms = document.querySelectorAll('form.confirm-delete');
    deleteForms.forEach(form => {
        form.addEventListener('submit', (e) => {
            const entity = form.getAttribute('data-entity') || 'item';
            if (!confirm(`Are you sure you want to permanently delete this ${entity}? This action cannot be undone.`)) {
                e.preventDefault();
            }
        });
    });
});
