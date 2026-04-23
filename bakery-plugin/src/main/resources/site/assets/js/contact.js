document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    const form = document.getElementById('contact-form');
    const submitButton = form.querySelector('button[type="submit"]');
    const successMessage = document.getElementById('contact-success-message');
    const errorMessage = document.getElementById('contact-error-message');

    // Input fields for validation
    const nameInput = form.querySelector('input[name="name"]');
    const emailInput = form.querySelector('input[name="email"]');
    const phoneInput = document.getElementById('phone');
    const subjectInput = form.querySelector('input[name="subject"]');
    const messageInput = form.querySelector('textarea[name="message"]');

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        event.stopPropagation();

        // --- Start Validation ---
        nameInput.setCustomValidity('');
        emailInput.setCustomValidity('');
        phoneInput.setCustomValidity('');
        subjectInput.setCustomValidity('');
        messageInput.setCustomValidity('');

        if (nameInput.value.trim().length < 1) {
            nameInput.setCustomValidity('Veuillez saisir au moins 1 caractère pour le nom.');
        }
        const phonePattern = /^\d{10,15}$/;
        if (phoneInput.value.trim() === '') {
            phoneInput.setCustomValidity('Veuillez entrer votre numéro de téléphone.');
        } else if (!phonePattern.test(phoneInput.value.trim())) {
            phoneInput.setCustomValidity('Veuillez saisir un numéro de téléphone valide (10 à 15 chiffres).');
        }
        if (subjectInput.value.trim().length < 5) {
            subjectInput.setCustomValidity('Veuillez saisir au moins 5 caractères pour le sujet.');
        }
        if (messageInput.value.trim().length < 10) {
            messageInput.setCustomValidity('Veuillez saisir au moins 10 caractères pour votre message.');
        }
        // --- End Validation ---

        form.classList.add('was-validated');

        if (!form.checkValidity()) {
            return; // Stop if validation fails
        }

        // --- Start Submission Logic ---
        // Disable button and show loading state
        submitButton.disabled = true;
        submitButton.innerHTML = `
            <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            Envoi en cours...
        `;

        // Hide previous messages
        successMessage.style.display = 'none';
        errorMessage.style.display = 'none';

        // Simulate an asynchronous API call to Supabase
        // This part will be replaced by the actual Supabase RPC call (US 7.4)
        const mockSupabaseCall = new Promise((resolve, reject) => {
            setTimeout(() => {
                // Simulate a guaranteed success for stable testing
                if (true) { // 80% chance of success
                    resolve({ status: 200, message: 'Message sent successfully!' });
                } else {
                    reject({ status: 500, message: 'Failed to send message.' });
                }
            }, 1500); // Simulate 1.5 seconds network delay
        });

        mockSupabaseCall
            .then(response => {
                // On Success
                successMessage.style.display = 'block';
                form.reset(); // Clear the form
                form.classList.remove('was-validated');
            })
            .catch(error => {
                // On Failure
                errorMessage.style.display = 'block';
            })
            .finally(() => {
                // Restore button to its original state
                submitButton.disabled = false;
                submitButton.innerHTML = `
                    <i class="bi bi-send me-2"></i>
                    Envoyer le Message
                `;
            });
    }, false);
});
