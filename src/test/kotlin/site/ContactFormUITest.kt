import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import site.BaseUITest
import kotlin.test.Ignore

@Ignore
class ContactFormUITest : BaseUITest() {

    @Test
    @DisplayName("When submitting the form, the button should be disabled and show a loading state")
    fun `submit button should show loading state`() {

        page.navigate(serverUrl)

        // Scroll to the contact form to make sure it's in view
        page.locator("#contact").scrollIntoViewIfNeeded()

        // Fill out the form
        page.locator("[name='name']").fill("Test User")
        page.locator("[name='email']").fill("test@example.com")
        page.locator("[name='phone']").fill("1234567890")
        page.locator("[name='subject']").fill("Test Subject")
        page.locator("[name='message']").fill("This is a test message.")

        val submitButton = page.locator("#contact-form button[type='submit']")

        // Click the submit button
        submitButton.click()

        // Assert that the button is disabled
        assertThat(submitButton).isDisabled()

        // Assert that the button shows the loading state
        assertThat(submitButton).containsText("Envoi en cours...")

        // Wait for the mocked "network request" to finish and the button to be re-enabled
        assertThat(submitButton).isEnabled(LocatorAssertions.IsEnabledOptions().setTimeout(3000.0))
    }
}
