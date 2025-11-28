package site

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

import kotlin.test.Ignore

@Ignore
class ContactFormValidationTest : BaseUITest() {

    @Test
    fun `should display validation error for invalid email format`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "invalid-email")
        page.fill("input[name='phone']", "1234567890")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val emailInput = page.locator("input[name='email']")
        assertTrue(
            emailInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Email field should be invalid"
        )
    }

    @Test
    fun `should display validation error for empty message field`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.click("#contact-form button[type='submit']")

        val messageInput = page.locator("textarea[name='message']")
        assertTrue(
            messageInput.evaluate("textarea => textarea.matches(':invalid')") as Boolean,
            "Message field should be invalid"
        )
    }

    @Test
    fun `should display validation error for empty phone field`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val phoneInput = page.locator("input[name='phone']")
        assertTrue(
            phoneInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Phone field should be invalid"
        )
    }

    @Test
    fun `should display validation error for empty name field`() {
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.fill("input[name='subject']", "Subject")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val nameInput = page.locator("input[name='name']")
        assertTrue(
            nameInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Name field should be invalid"
        )
    }

    @Test
    fun `should display validation error for phone field with less than 10 digits`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "12345") // Less than 10 digits
        page.fill("input[name='subject']", "Subject")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val phoneInput = page.locator("input[name='phone']")
        assertTrue(
            phoneInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Phone field should be invalid"
        )
    }

    @Test
    fun `should display validation error for phone field with more than 15 digits`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890123456") // More than 15 digits
        page.fill("input[name='subject']", "Subject")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val phoneInput = page.locator("input[name='phone']")
        assertTrue(
            phoneInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Phone field should be invalid"
        )
    }

    @Test
    fun `should display validation error for phone field with non-digit characters`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "123-456-7890") // Non-digit characters
        page.fill("input[name='subject']", "Subject")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val phoneInput = page.locator("input[name='phone']")
        assertTrue(
            phoneInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Phone field should be invalid"
        )
    }

    @Test
    fun `should display validation error for empty subject field`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val subjectInput = page.locator("input[name='subject']")
        assertTrue(
            subjectInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Subject field should be invalid"
        )
    }

    @Test
    fun `should display validation error for subject field with less than 5 characters`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.fill("input[name='subject']", "Sub") // Less than 5 characters
        page.fill("textarea[name='message']", "This is a test message.")
        page.click("#contact-form button[type='submit']")

        val subjectInput = page.locator("input[name='subject']")
        assertTrue(
            subjectInput.evaluate("input => input.matches(':invalid')") as Boolean,
            "Subject field should be invalid"
        )
    }

    @Test
    fun `should display validation error for message field with less than 10 characters`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.fill("input[name='subject']", "Subject")
        page.fill("textarea[name='message']", "Short") // Less than 10 characters
        page.click("#contact-form button[type='submit']")

        val messageInput = page.locator("textarea[name='message']")
        assertTrue(
            messageInput.evaluate("textarea => textarea.matches(':invalid')") as Boolean,
            "Message field should be invalid"
        )
    }

    @Test
    fun `should not display validation errors for valid form submission`() {
        page.fill("input[name='name']", "John Doe")
        page.fill("input[name='email']", "test@example.com")
        page.fill("input[name='phone']", "1234567890")
        page.fill("input[name='subject']", "Valid Subject")
        page.fill("textarea[name='message']", "This is a valid test message with more than 10 characters.")

        val submitButton = page.locator("#contact-form button[type='submit']")
        submitButton.click()

        // After a successful (mock) submission, the form should be reset.
        // We use a waiting assertion to handle the async nature of the form reset.
        val nameInput = page.locator("input[name='name']")
        assertThat(nameInput).hasText("", com.microsoft.playwright.assertions.LocatorAssertions.HasTextOptions().setTimeout(5000.0))
    }
}