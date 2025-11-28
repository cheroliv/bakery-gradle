package site

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
abstract class BaseUITest {

    protected lateinit var playwright: Playwright
    protected lateinit var browser: Browser
    protected lateinit var context: BrowserContext
    protected lateinit var page: Page

    companion object {
        const val serverUrl = "http://localhost:8820/"
    }

    @BeforeAll
    fun setupAll() {
        playwright = Playwright.create()
        browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
    }

    @AfterAll
    fun teardownAll() {
        browser.close()
        playwright.close()
    }

    @BeforeEach
    fun setup() {
        context = browser.newContext()
        page = context.newPage()
        page.navigate(serverUrl)
    }

    @AfterEach
    fun tearDown() {
        page.close()
        context.close()
    }
}
