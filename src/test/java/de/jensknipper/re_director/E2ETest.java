package de.jensknipper.re_director;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static de.jensknipper.re_director.database.tables.Redirects.REDIRECTS;

import com.microsoft.playwright.*;
import de.jensknipper.re_director.db.RedirectRepository;
import de.jensknipper.re_director.db.entity.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class E2ETest {

  public static final Path RECORD_VIDEO_DIR = Paths.get("target/playwright");

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @RegisterExtension
  private final TestWatcher testWatcher =
      new TestWatcher() {
        @Override
        public void testSuccessful(ExtensionContext context) {
          try {
            Files.deleteIfExists(page.video().path());
            Files.deleteIfExists(page.video().path().getParent());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      };

  @LocalServerPort private int port;

  @Autowired private DSLContext dsl;
  @Autowired private RedirectRepository redirectRepository;

  private static Playwright playwright;
  private static Browser browser;

  BrowserContext context;
  Page page;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void closeBrowser() {
    playwright.close();
  }

  @BeforeEach
  void createContextAndPage(TestInfo testInfo) {
    String baseURL = "http://localhost:" + port;
    Path recordVideoDir =
        RECORD_VIDEO_DIR
            .resolve(testInfo.getTestClass().map(Class::getSimpleName).orElse("UNKNOWN"))
            .resolve(testInfo.getDisplayName().replaceAll("\\(\\)", ""));

    var options =
        new Browser.NewContextOptions()
            .setBaseURL(baseURL)
            .setRecordVideoDir(recordVideoDir)
            .setRecordVideoSize(800, 600);
    context = browser.newContext(options);
    page = context.newPage();

    dsl.deleteFrom(REDIRECTS).execute();
  }

  @AfterEach
  void cleanup() {
    context.close();
  }

  @Test
  void mainPageLoads() {
    page.navigate("/");
    assertThat(page.locator("h6")).hasText("Manage Redirects Like a Pro");
  }

  @Test
  void redirectsPageLoads() {
    page.navigate("/redirects");
    assertThat(page.locator("main").locator("h1")).hasText("Your Redirects");
  }

  @Test
  void createRedirectWorks() {
    page.navigate("/redirects");

    // create-modal is closed and table is empty
    assertThat(page.locator("#modal-create-redirect")).not().hasAttribute("open", "");
    assertThat(page.locator("#table-element-0")).hasCount(0);

    // clicking create opens the create-modal
    page.locator("#create-button").click();
    assertThat(page.locator("#modal-create-redirect")).hasAttribute("open", "");

    // modal can be filled out
    page.locator("#source-input-create-modal").fill("http://source");
    page.locator("#target-input-create-modal").fill("http://target");
    page.locator("#status-code-input-create-modal").selectOption("302");

    Locator createButton = page.locator("#confirm-button-create-modal");
    assertThat(createButton).hasText("Confirm");

    // clicking create closes the modal
    createButton.click();
    assertThat(page.locator("#modal-create-redirect")).not().hasAttribute("open", "");

    // a new element should be in the table
    Locator tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#source")).hasText("http://source");
    assertThat(tableLine.locator("#target")).hasText("http://target");
    assertThat(tableLine.locator("#httpStatusCode")).hasText("302");
  }

  @Test
  void editWorks() {
    redirectRepository.create(
        "source", "target", Status.ACTIVE, RedirectRepository.DEFAULT_REDIRECT);

    page.navigate("/redirects");

    // there is an element as specified and modal is closed
    Locator tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#source")).hasText("source");
    assertThat(tableLine.locator("#target")).hasText("target");
    assertThat(tableLine.locator("#httpStatusCode")).hasText("301");
    assertThat(page.locator("#modal-update-redirect-1")).not().hasAttribute("open", "");

    // clicking on edit opens the edit-modal
    tableLine.locator("#edit-button").click();
    Locator modal = page.locator("#modal-update-redirect-1");
    assertThat(modal).hasAttribute("open", "");

    // modal can be filled
    page.locator("#source-input-edit-modal").fill("http://source");
    page.locator("#target-input-edit-modal").fill("http://target");
    page.locator("#status-code-input-edit-modal").selectOption("302");

    Locator createButton = page.locator("#confirm-button-edit-modal");
    assertThat(createButton).hasText("Confirm");

    // clicking create closes the modal
    createButton.click();
    assertThat(page.locator("#modal-update-redirect-1")).not().hasAttribute("open", "");

    // a new element should be in the table
    tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#source")).hasText("http://source");
    assertThat(tableLine.locator("#target")).hasText("http://target");
    assertThat(tableLine.locator("#httpStatusCode")).hasText("302");
  }

  @Test
  void deactivateAndActivateWorks() {
    redirectRepository.create(
        "source", "target", Status.ACTIVE, RedirectRepository.DEFAULT_REDIRECT);

    page.navigate("/redirects");

    // there is an element which is activated
    Locator tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#status").locator("span").locator("i"))
        .hasAttribute("title", "Active");

    // clicking pause deactivates it
    tableLine.locator("#deactivate-button").click();
    tableLine = page.locator("#table-element-0");
    assertThat(tableLine.locator("#status").locator("span").locator("i"))
        .hasAttribute("title", "Inactive");

    // clicking resume activates it
    tableLine.locator("#activate-button").click();
    tableLine = page.locator("#table-element-0");
    assertThat(tableLine.locator("#status").locator("span").locator("i"))
        .hasAttribute("title", "Active");
  }

  @Test
  void deleteWorks() {
    redirectRepository.create(
        "source", "target", Status.ACTIVE, RedirectRepository.DEFAULT_REDIRECT);

    page.navigate("/redirects");

    // there is an element
    Locator tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);

    // there is none after clicking delete
    tableLine.locator("#delete-button").click();
    assertThat(page.locator("#table-element-0")).hasCount(0);
  }

  @Test
  void filterWorks() {
    redirectRepository.create(
        "source1", "target", Status.ACTIVE, RedirectRepository.DEFAULT_REDIRECT);
    redirectRepository.create(
        "source2", "target", Status.INACTIVE, RedirectRepository.DEFAULT_REDIRECT);
    redirectRepository.create(
        "source3", "target", Status.INACTIVE, RedirectRepository.DEFAULT_REDIRECT);

    page.navigate("/redirects?status=INACTIVE&search=3");

    // filter contains values
    assertThat(page.locator("#status-input-filter")).hasValue("INACTIVE");
    assertThat(page.locator("#search-input-filter")).hasValue("3");

    // there is one element
    Locator tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#source")).hasText("source3");

    // change filter
    page.locator("#status-input-filter").selectOption("ACTIVE");
    page.locator("#search-input-filter").fill("1");
    page.locator("#button-filter").click();

    // filter contains values
    assertThat(page.locator("#status-input-filter")).hasValue("ACTIVE");
    assertThat(page.locator("#search-input-filter")).hasValue("1");

    // lists matching elements
    tableLine = page.locator("#table-element-0");
    assertThat(tableLine).hasCount(1);
    assertThat(tableLine.locator("#source")).hasText("source1");

    // url contains new filter
    assertThat(page).hasURL("/redirects?status=ACTIVE&search=1");
  }
  // TODO 301 default works
  // TODO create, edit, delete, activate/deactivate  should preserve filter
  // TODO validation
}
