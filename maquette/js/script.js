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
    }

    applyTheme(theme) {
        document.documentElement.setAttribute('data-bs-theme', theme);
    }

    updateThemeUI() {
        const currentThemeData = this.themes[this.currentTheme];
        const themeButton = document.getElementById('themeDropdown');
        if (themeButton && currentThemeData) {
            themeButton.setAttribute('aria-label', `Thème actuel : ${currentThemeData.label}`);
            const iconElement = themeButton.querySelector('i');
            if (iconElement) {
                iconElement.className = `bi ${currentThemeData.icon}`;
            }
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
        const href = event.currentTarget.getAttribute('href');
        if (href === '#' || href === '#home') {
            event.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        const targetId = href.substring(1);
        const targetElement = document.getElementById(targetId);

        if (targetElement) {
            event.preventDefault();
            targetElement.scrollIntoView({ behavior: 'smooth' });
        }
    }
}

/**
 * Dynamically switches the navbar to burger mode when menu items no longer fit.
 */
class DynamicNavbarBreakpoint {
    constructor(navbar) {
        this.navbar = navbar;
        this.container = navbar.querySelector('.container');
        this.brand = navbar.querySelector('.navbar-brand');
        this.navbarNav = navbar.querySelector('.navbar-nav');
        this.themeSelector = navbar.querySelector('.theme-dropdown-btn');
        this.init();
    }

    init() {
        if (!this.container || !this.brand || !this.navbarNav || !this.themeSelector) return;
        const resizeObserver = new ResizeObserver(() => this.checkBreakpoint());
        resizeObserver.observe(this.container);
        resizeObserver.observe(this.navbarNav);
        setTimeout(() => this.checkBreakpoint(), 150);
    }

    checkBreakpoint() {
        this.navbar.classList.add('navbar-expand-lg');
        const brandWidth = this.brand.offsetWidth;
        const navbarNavWidth = this.navbarNav.offsetWidth;
        const themeSelectorWidth = this.themeSelector.offsetWidth;
        const gapStyle = getComputedStyle(this.navbar.querySelector('.navbar-collapse')).gap;
        const gapWidth = parseFloat(gapStyle) || 8;
        const requiredWidth = brandWidth + navbarNavWidth + themeSelectorWidth + (2 * gapWidth);
        const availableWidth = this.container.clientWidth;
        const buffer = 5;

        if (requiredWidth + buffer > availableWidth) {
            this.navbar.classList.remove('navbar-expand-lg');
        } else {
            this.navbar.classList.add('navbar-expand-lg');
        }
    }
}

/**
 * Adds a "Copy" button to all code blocks.
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
            navigator.clipboard.writeText(codeBlock.innerText).then(() => {
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

// ======================
// Main Initialization
// ======================
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('themeDropdown')) {
        try { new ThemeManager(); } catch (e) { console.error("Failed to initialize ThemeManager:", e); }
    }

    const navbar = document.querySelector('.navbar');
    if (navbar) {
        try { new MobileMenuManager(navbar); } catch (e) { console.error("Failed to initialize MobileMenuManager:", e); }
        try { new DynamicNavbarBreakpoint(navbar); } catch (e) { console.error("Failed to initialize DynamicNavbarBreakpoint:", e); }
    }

    const scrollToTopBtn = document.getElementById('scrollToTopBtn');
    if (scrollToTopBtn) {
        try { new ScrollToTopButton(scrollToTopBtn); } catch (e) { console.error("Failed to initialize ScrollToTopButton:", e); }
    }

    try { new SmoothScrollWithOffset(); } catch (e) { console.error("Failed to initialize SmoothScrollWithOffset:", e); }
    try { new CodeBlockManager(); } catch (e) { console.error("Failed to initialize CodeBlockManager:", e); }
    try { new TooltipManager(); } catch (e) { console.error("Failed to initialize TooltipManager:", e); }
});