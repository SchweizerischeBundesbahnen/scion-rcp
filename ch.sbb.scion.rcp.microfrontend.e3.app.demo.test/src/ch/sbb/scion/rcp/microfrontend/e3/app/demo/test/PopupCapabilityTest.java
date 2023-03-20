/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.e3.app.demo.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PopupCapabilityTest {

  private static final int TIMEOUT = 10000;
  private SWTBot bot;

  // TODO: When creating more tests, this beforeAll code might be better in some base/delegate test class
  @BeforeAll
  static void beforeAll() {
    // configure logging
    BasicConfigurator.configure();

    // might be required when running in headless mode
    // https://wiki.eclipse.org/SWTBot/Troubleshooting#No_active_Shell_when_running_SWTBot_tests_in_Xvfb
    UIThreadRunnable.syncExec(new VoidResult() {

      @Override
      public void run() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().forceActive();
      }
    });

  }

  @BeforeEach
  void beforeEach() throws Exception {
    bot = new SWTBot();
  }

  @Test
  void openPopup_withCloseOnFocusAndEscape_shouldCloseOnEscape() throws Exception {

    // GIVEN
    SWTBotMenu capabilitiesMenu = bot.menu("Capabilities");
    bot.waitUntil(Conditions.waitForMenu(bot.activeShell(), new MenuItemMatcher("Capabilities")), TIMEOUT);

    capabilitiesMenu.menu("Show View").menu("Workbench Popup Service").click();
    assertThat("Popup Window Tab not visible", bot.cTabItem("Workbench Popup Service").isVisible());

    // remove the pre-defined qualifier parameter and add our own
    SWTBotTable qualifierTable = bot.tableWithId("table-qualifier");
    qualifierTable.select("test");
    bot.buttonWithId("remove-qualifier").click();
    bot.text().setText("test");
    bot.text(1).setText("eclipse-popup");
    bot.buttonWithId("add-qualifier").click();

    // remove the id parameter
    SWTBotTable paramsTable = bot.tableWithId("table-params");
    paramsTable.select("id");
    bot.buttonWithId("remove-params").click();

    // open the popup
    bot.buttonWithId("popup-open").click();

    bot.waitUntil(Conditions.shellIsActive("Eclipse Test Popup"), TIMEOUT);
    assertThat(bot.activeShell().getText(), is("Eclipse Test Popup"));

    // WHEN
    bot.activeShell().pressShortcut(Keystrokes.ESC);

    // THEN
    assertThat(bot.activeShell().getText(), is(not("Eclipse Test Popup")));
    assertThat(bot.activeShell().getText(), is("RCP Application"));
  }

  final private static class MenuItemMatcher extends BaseMatcher<MenuItem> {

    private final String menuText;

    MenuItemMatcher(final String menuText) {
      this.menuText = menuText;
    }

    @Override
    public boolean matches(final Object item) {
      if (item instanceof MenuItem) {
        MenuItem menuItem = (MenuItem) item;
        return menuItem.getText().equals(menuText);
      }
      return false;
    }

    @Override
    public void describeTo(final Description description) {
    }
  }

  // additional tests should look like this...
  @Test
  void openPopup_withCloseOnEscape_shouldNotCloseOnFocusLos() throws Exception {
  }

  @Test
  void openPopup_withCloseOnEscape_shouldCloseOnEscape() throws Exception {
  }

  @Test
  void openPopup_withCloseOnFocus_shouldNotCloseOnEscape() throws Exception {
  }

  @Test
  void openPopup_withCloseOnFocus_shouldCloseOnFocusLost() throws Exception {
  }

  @Test
  void openPopup_withCloseOnFocusAndEscape_shouldCloseOnFocusLost() throws Exception {
  }

}