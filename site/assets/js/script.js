// ===============================================================================================================
// E3PO - Enhanced Experience Engine for Personal Websites
// ===============================================================================================================
// This script manages theme switching, smooth scrolling, dynamic navbar adjustments, and other UX enhancements.
// Each class is designed to be self-contained and initialized safely to prevent site-wide script failures.
// ===============================================================================================================

/**
 * Manages the website's theme (light, dark, high-contrast).
 * - Loads theme from localStorage or system preference.
 * - Updates UI elements to reflect the current theme.
 * - Handles user interaction for theme switching.
 */
class ThemeManager {
    constructor() {
        this.themes = {
            light: { name: 'Light', icon: 'bi-sun', label: 'Light' },
            dark: { name: 'Dark', icon: 'bi-moon', label: 'Dark' },
            'high-contrast': { name: 'High Contrast', icon: 'bi-circle-half', label: 'High Contrast' }
        };
        this.currentTheme = 'light';
        this.init();
    }

    init() {
        this.loadTheme();
        this.setupEventListeners();
        this.updateThemeUI();
        this.listenForSystemThemeChanges();
    }

    loadTheme() {
        const savedTheme = localStorage.getItem('preferred-theme');
        if (savedTheme && this.themes[savedTheme]) {
            this.currentTheme = savedTheme;
        } else {
            this.currentTheme = window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
        this.applyTheme(this.currentTheme);
    }

    setupEventListeners() {
        document.querySelectorAll('.theme-option').forEach(option => {
            option.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchTheme(e.currentTarget.dataset.theme);
            });
        });
    }

    switchTheme(theme) {
        if (!this.themes[theme]) return;
        this.currentTheme = theme;
        this.applyTheme(theme);
        localStorage.setItem('preferred-theme', theme);
        this.updateThemeUI();

        // Re-focus the dropdown button to prevent page jump
        const themeButton = document.getElementById('themeDropdown');
        if (themeButton) {
            themeButton.focus();
        }
    }

    applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
        this.updateCodeTheme(theme);
        this.updateHeroSvg(theme);
    }

    updateHeroSvg(theme) {
        const heroSvg = document.querySelector('.hero-section .hero-svg-icon');
        if (!heroSvg) return;

        let svgPath = '';
        switch (theme) {
            case 'dark':
                svgPath = '/img/undraw_road_to_knowledge_dark.svg'; // Placeholder for dark theme SVG
                break;
            case 'high-contrast':
                svgPath = '/img/undraw_road_to_knowledge_high_contrast.svg'; // Placeholder for high-contrast theme SVG
                break;
            case 'light':
            default:
                svgPath = '/img/undraw_road_to_knowledge_light.svg'; // Default light theme SVG
                break;
        }
        heroSvg.src = svgPath;
    }

    updateCodeTheme(theme) {
        const highlightThemeLink = document.getElementById('highlight-js-theme');
        if (!highlightThemeLink) return;

        const lightTheme = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/github.min.css';
        const darkTheme = 'https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.11.1/styles/github-dark.min.css';

        highlightThemeLink.href = (theme === 'dark' || theme === 'high-contrast') ? darkTheme : lightTheme;
    }

    updateThemeUI() {
        const currentThemeData = this.themes[this.currentTheme];
        const themeButton = document.getElementById('themeDropdown');
        if (themeButton && currentThemeData) {
            themeButton.setAttribute('aria-label', `Thème actuel : ${currentThemeData.label}`);
            const iconElement = themeButton.querySelector('i');
            if (iconElement) {
                iconElement.className = `bi ${currentThemeData.icon} me-1`;
            }
            // The text content of #current-theme is now visually hidden but still updated for screen readers
            const currentThemeSpan = themeButton.querySelector('#current-theme');
            if (currentThemeSpan) {
                currentThemeSpan.textContent = currentThemeData.label;
            }
        }
        document.querySelectorAll('.theme-option').forEach(option => {
            option.classList.toggle('active', option.dataset.theme === this.currentTheme);
        });
    }

    listenForSystemThemeChanges() {
        window.matchMedia?.('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
            if (!localStorage.getItem('preferred-theme')) {
                this.switchTheme(e.matches ? 'dark' : 'light');
            }
        });
    }
}

/**
 * Manages the "Scroll to Top" button.
 * - Shows the button when the user scrolls down.
 * - Adjusts its position to avoid overlapping with the footer.
 */
class ScrollToTopButton {
    constructor(button) {
        this.button = button;
        this.footer = document.querySelector('footer');
        this.init();
    }

    init() {
        window.addEventListener('scroll', () => this.handleScroll(), { passive: true });
        this.button.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    }

    handleScroll() {
        const scrollY = window.scrollY;
        this.button.classList.toggle('show', scrollY > 300);

        if (this.footer) {
            const footerRect = this.footer.getBoundingClientRect();
            if (footerRect.top < window.innerHeight) {
                this.button.style.bottom = `${window.innerHeight - footerRect.top + 20}px`;
            } else {
                this.button.style.bottom = '20px';
            }
        }
    }
}

/**
 * Handles automatic closing of the mobile navigation menu using event delegation.
 * - When a navigation link or a dropdown item is clicked, the expanded mobile menu collapses.
 * - It specifically ignores clicks on dropdown toggles to allow submenus to open.
 */
class MobileMenuManager {
    constructor(navbar) {
        this.navbarCollapse = navbar.querySelector('.navbar-collapse');
        this.init();
    }

    init() {
        if (!this.navbarCollapse) return;
        const bsCollapse = new bootstrap.Collapse(this.navbarCollapse, { toggle: false });

        this.navbarCollapse.addEventListener('click', (event) => {
            const target = event.target;
            // Check if the clicked element is a nav-link (but not a dropdown toggle) OR a dropdown-item
            const isSimpleNavLink = target.matches('.nav-link') && !target.matches('[data-bs-toggle="dropdown"]');
            const isDropdownItem = target.matches('.dropdown-item');

            if (isSimpleNavLink || isDropdownItem) {
                if (this.navbarCollapse.classList.contains('show')) {
                    bsCollapse.hide();
                }
            }
        });
    }
}

/**
 * Handles smooth scrolling to anchor links with an offset for fixed headers.
 * - Intercepts clicks on internal anchor links.
 * - Calculates scroll position considering the fixed navbar height.
 * - Performs a smooth scroll to the target.
 */
class SmoothScrollWithOffset {
    constructor(navbarSelector = '.navbar') {
        this.navbar = document.querySelector(navbarSelector);
        this.init();
    }

    init() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', this.handleAnchorClick.bind(this));
        });
    }

    handleAnchorClick(event) {
        const anchor = event.currentTarget;
        const href = anchor.getAttribute('href');

        // Ignore dropdown toggles to prevent smooth scroll interference
        if (anchor.matches('[data-bs-toggle="dropdown"]')) {
            return;
        }

        if (href === '#' || href === '#home') { // Handle #home or empty hash specifically
            event.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        const targetId = href.substring(1);
        const targetElement = document.getElementById(targetId);

        if (targetElement) {
            event.preventDefault();
            const navbarCollapse = this.navbar.querySelector('.navbar-collapse');
            const isMobileMenuOpen = navbarCollapse && navbarCollapse.classList.contains('show');

            if (isMobileMenuOpen) {
                // Wait for the collapse animation to finish before scrolling
                navbarCollapse.addEventListener('hidden.bs.collapse', () => {
                    targetElement.scrollIntoView({ behavior: 'smooth' });
                }, { once: true });
            } else {
                targetElement.scrollIntoView({ behavior: 'smooth' });
            }
        }
    }
}

/**
 * Dynamically updates the --navbar-height CSS variable based on the actual navbar height.
 * This ensures scroll-margin-top is always accurate for fixed headers.
 */
class NavbarHeightUpdater {
    constructor(navbar) {
        this.navbar = navbar;
        this.init();
    }

    init() {
        if (!this.navbar || !('ResizeObserver' in window)) return;
        const resizeObserver = new ResizeObserver(() => this.updateHeight());
        resizeObserver.observe(this.navbar);
        this.updateHeight(); // Initial update
    }

    updateHeight() {
        const brandElement = this.navbar.querySelector('.navbar-brand');
        let actualNavbarHeight = 0;

        if (brandElement) {
            actualNavbarHeight = brandElement.offsetHeight;
        }

        const newHeight = `${actualNavbarHeight}px`;
        document.documentElement.style.setProperty('--navbar-height', newHeight);
        console.log('NavbarHeightUpdater - New --navbar-height set to:', newHeight, 'from element:', brandElement);
    }
}

/**
 * Dynamically switches the navbar to burger mode when menu items no longer fit.
 * This is achieved by adding/removing the .navbar-expand-lg class, letting Bootstrap handle the UI changes.
 */
class DynamicNavbarBreakpoint {
    constructor(navbar) {
        this.navbar = navbar;
        this.container = navbar.querySelector('.container');
        this.brand = navbar.querySelector('.navbar-brand');
        this.navbarNav = navbar.querySelector('.navbar-nav');
        this.themeSelector = navbar.querySelector('#themeDropdown');
        this.init();
    }

    init() {
        if (!this.container || !this.brand || !this.navbarNav || !this.themeSelector) {
            console.warn('DynamicNavbarBreakpoint: Missing required elements for measurement.');
            return;
        }

        const resizeObserver = new ResizeObserver(() => this.checkBreakpoint());
        resizeObserver.observe(this.container);
        
        // Also observe the navbar itself in case its contents change dynamically
        resizeObserver.observe(this.navbarNav);

        // Initial check after a short delay to ensure rendering is complete
        setTimeout(() => this.checkBreakpoint(), 150);
    }

    checkBreakpoint() {
        // Temporarily force the navbar to be expanded to measure its true width
        this.navbar.classList.add('navbar-expand-lg');

        const brandWidth = this.brand.offsetWidth;
        const navbarNavWidth = this.navbarNav.offsetWidth;
        const themeSelectorWidth = this.themeSelector.offsetWidth;
        
        // Calculate the gap between flex items. We assume the gap is consistent.
        const navbarCollapse = this.navbar.querySelector('.navbar-collapse');
        const gapStyle = navbarCollapse ? getComputedStyle(navbarCollapse).gap : '0px';
        const gapWidth = parseFloat(gapStyle) || 8; // Default to 8px if gap is not found

        // Total required width is the sum of elements plus the gaps between them
        const requiredWidth = brandWidth + navbarNavWidth + themeSelectorWidth + (2 * gapWidth);

        // Available width is the container's inner width
        const availableWidth = this.container.clientWidth;
        
        // A small buffer to prevent elements from touching the edge
        const buffer = 5;

        if (requiredWidth + buffer > availableWidth) {
            // Not enough space, remove the class to collapse into burger mode
            this.navbar.classList.remove('navbar-expand-lg');
        } else {
            // Enough space, the class is already added, so Bootstrap handles expansion
            // This ensures the class remains for the expanded view
            this.navbar.classList.add('navbar-expand-lg');
        }

        // Re-initialize the dropdown
        if (this.themeSelector) {
            new bootstrap.Dropdown(this.themeSelector);
        }
    }
}

/**
 * Adds a "Copy" button to all code blocks for easy copying.
 * - Finds all <pre><code> blocks.
 * - Injects a button into each.
 * - Handles the click event to copy the code content to the clipboard.
 * - Provides user feedback on successful copy.
 */
class CodeBlockManager {
    constructor() {
        this.init();
    }

    init() {
        document.querySelectorAll('pre code').forEach(codeBlock => {
            const preElement = codeBlock.parentElement;
            if (preElement) {
                const copyButton = this.createCopyButton();
                preElement.appendChild(copyButton);
                this.setupCopyEventListener(copyButton, codeBlock);
            }
        });
    }

    createCopyButton() {
        const button = document.createElement('button');
        button.className = 'copy-code-btn';
        button.innerHTML = '<i class="bi bi-clipboard"></i> Copier';
        button.setAttribute('aria-label', 'Copier le code');
        return button;
    }

    setupCopyEventListener(button, codeBlock) {
        button.addEventListener('click', () => {
            const codeToCopy = codeBlock.innerText;
            navigator.clipboard.writeText(codeToCopy).then(() => {
                button.innerHTML = '<i class="bi bi-check-lg"></i> Copié !';
                button.classList.add('copied');
                setTimeout(() => {
                    button.innerHTML = '<i class="bi bi-clipboard"></i> Copier';
                    button.classList.remove('copied');
                }, 2000);
            }).catch(err => {
                console.error('Failed to copy code: ', err);
                button.innerText = 'Erreur';
            });
        });
    }
}


/**
 * Initializes all Bootstrap tooltips on the page.
 * - Finds all elements with `data-bs-toggle="tooltip"`.
 * - Creates a new Bootstrap Tooltip instance for each.
 */
class TooltipManager {
    constructor() {
        this.init();
    }

    init() {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
}

/**
 * Manages connection and interactions with Supabase.
 * - Initializes the Supabase client using globally injected credentials.
 * - Provides methods for interacting with Supabase services (e.g., database).
 */
class SupabaseManager {
    constructor() {
        this.supabase = null;
        this.init();
    }

    init() {
        // Check if a mock SupabaseManager is provided globally (for testing)
        if (typeof window.MockSupabaseManager !== 'undefined') {
            console.log('Using MockSupabaseManager for testing.');
            // Assign a mock instance directly
            this.supabase = new window.MockSupabaseManager();
            return;
        }

        // Check if Supabase client library is loaded
        if (typeof supabase === 'undefined') {
            console.error('Supabase client library (supabase-js) is not loaded.');
            return;
        }
        // Check if globally injected credentials exist
        if (typeof SUPABASE_URL === 'undefined' || typeof SUPABASE_KEY === 'undefined') {
            console.error('Supabase URL or Key is not defined. Make sure they are injected correctly in the HTML.');
            return;
        }

        try {
            // Initialize the client with the global variables
            this.supabase = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);
            console.log('Supabase client initialized successfully.');
        } catch (error) {
            console.error('Failed to initialize Supabase client:', error);
        }
    }

    /**
     * Submits the contact form data to the Supabase RPC function 'handle_contact_form'.
     * @param {string} name - The sender's name.
     * @param {string} email - The sender's email.
     * @param {string} subject - The message subject.
     * @param {string} message - The message content.
     * @returns {Promise<{data: any, error: any}>} - The result from the RPC call.
     */
    async submitContactForm(name, email, subject, message) {
        if (!this.supabase) {
            console.error('Supabase client is not initialized.');
            return { error: { message: 'Supabase client not available.' } };
        }

        const { data, error } = await this.supabase.rpc('handle_contact_form', {
            p_name: name,
            p_email: email,
            p_subject: subject,
            p_message: message
        });

        if (error) {
            console.error('Error calling handle_contact_form RPC:', error);
            return { error };
        }

        console.log('RPC handle_contact_form called successfully:', data);
        return { data };
    }
}

/**
 * Manages the contact form submission.
 * - Listens for the form's submit event.
 * - Prevents the default browser submission.
 * - Calls the SupabaseManager to send the data.
 * - (Will be updated to) Provide visual feedback to the user.
 */
class ContactFormHandler {
    constructor(form, supabaseManager) {
        this.form = form;
        this.supabaseManager = supabaseManager;
        this.init();
    }

    init() {
        if (!this.form || !this.supabaseManager) {
            console.warn('ContactFormHandler: Form or SupabaseManager not provided.');
            return;
        }

        this.form.addEventListener('submit', async (event) => {
            event.preventDefault();
            event.stopPropagation();

            // Add was-validated class to trigger Bootstrap's styles
            this.form.classList.add('was-validated');

            // Check validity
            if (!this.form.checkValidity()) {
                console.log('Form is invalid. Submission stopped.');
                return; // Stop if the form is invalid
            }

            console.log('Contact form is valid, proceeding with submission.');

            const formData = new FormData(this.form);
            const name = formData.get('name');
            const email = formData.get('email');
            const subject = formData.get('subject');
            const message = formData.get('message');

            const submitButton = this.form.querySelector('button[type="submit"]');
            const originalButtonHtml = submitButton.innerHTML;

            // Disable button and show loading state
            submitButton.disabled = true;
            submitButton.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Envoi en cours...`;

            try {
                const { error } = await this.supabaseManager.submitContactForm(name, email, subject, message);

                if (error) {
                    alert(`Une erreur est survenue: ${error.message}`);
                } else {
                    alert('Message envoyé avec succès !');
                    this.form.reset();
                    this.form.classList.remove('was-validated');
                }
            } catch (e) {
                console.error("Submission failed", e);
                alert(`Une erreur inattendue est survenue.`);
            } finally {
                // Restore button state
                submitButton.disabled = false;
                submitButton.innerHTML = originalButtonHtml;
            }
        });
    }
}

/**
 * Manages the international telephone input field.
 * - Initializes the intl-tel-input library on a given input field.
 */
class PhoneInputManager {
    constructor(inputElement) {
        this.inputElement = inputElement;
        this.init();
    }

    init() {
        if (typeof intlTelInput === 'undefined') {
            console.error('intl-tel-input library is not loaded.');
            return;
        }
        try {
            window.intlTelInput(this.inputElement, {
                initialCountry: "fr",
                utilsScript: "https://cdn.jsdelivr.net/npm/intl-tel-input@25.10.1/build/js/utils.js",
            });
            console.log('PhoneInputManager initialized successfully.');
        } catch (error) {
            console.error('Failed to initialize intl-tel-input:', error);
        }
    }
}


// ======================
// Main Initialization
// ======================
document.addEventListener('DOMContentLoaded', () => {
    // Safely initialize ThemeManager
    if (document.getElementById('themeDropdown')) {
        try {
            new ThemeManager();
        } catch (e) {
            console.error("Failed to initialize ThemeManager:", e);
        }
    }

    // Safely initialize Navbar features
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        try {
            new MobileMenuManager(navbar);
        } catch (e) {
            console.error("Failed to initialize MobileMenuManager:", e);
        }
        try {
            new NavbarHeightUpdater(navbar); // Initialize NavbarHeightUpdater
        } catch (e) {
            console.error("Failed to initialize NavbarHeightUpdater:", e);
        }
        try {
            new DynamicNavbarBreakpoint(navbar); // Initialize the dynamic breakpoint logic
        } catch (e) {
            console.error("Failed to initialize DynamicNavbarBreakpoint:", e);
        }
    }

    // Safely initialize ScrollToTopButton
    const scrollToTopBtn = document.getElementById('scrollToTopBtn');
    if (scrollToTopBtn) {
        try {
            new ScrollToTopButton(scrollToTopBtn);
        } catch (e) {
            console.error("Failed to initialize ScrollToTopButton:", e);
        }
    }

    // Safely initialize SmoothScrollWithOffset
    try {
        new SmoothScrollWithOffset();
    } catch (e) {
        console.error("Failed to initialize SmoothScrollWithOffset:", e);
    }

    // Safely initialize CodeBlockManager
    try {
        new CodeBlockManager();
    } catch (e) {
        console.error("Failed to initialize CodeBlockManager:", e);
    }

    // Safely initialize TooltipManager
    try {
        new TooltipManager();
    } catch (e) {
        console.error("Failed to initialize TooltipManager:", e);
    }

    // Initialize SupabaseManager and ContactFormHandler together
    const contactForm = document.getElementById('contact-form');
    if (contactForm) {
        try {
            const supabaseManager = new SupabaseManager();
            new ContactFormHandler(contactForm, supabaseManager);
        } catch (e) {
            console.error("Failed to initialize SupabaseManager or ContactFormHandler:", e);
        }
    }

    // Safely initialize PhoneInputManager
    const phoneInputField = document.querySelector("#phone");
    if (phoneInputField) {
        try {
            new PhoneInputManager(phoneInputField);
        } catch (e) {
            console.error("Failed to initialize PhoneInputManager:", e);
        }
    }
});