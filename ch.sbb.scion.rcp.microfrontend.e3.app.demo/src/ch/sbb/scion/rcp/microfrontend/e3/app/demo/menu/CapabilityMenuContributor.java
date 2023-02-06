package ch.sbb.scion.rcp.microfrontend.e3.app.demo.menu;

import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.sbb.scion.rcp.microfrontend.SciIntentClient;
import ch.sbb.scion.rcp.microfrontend.SciManifestService;
import ch.sbb.scion.rcp.microfrontend.SciManifestService.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.ISubscription;
import ch.sbb.scion.rcp.microfrontend.model.Intent;

/**
 * Contributes menu items for capabilities of a given type.
 */
public abstract class CapabilityMenuContributor extends ContributionItem {

  private ISubscription subscription;

  @Inject
  private SciManifestService sciManifestService;

  @Inject
  private SciIntentClient sciIntentClient;

  public CapabilityMenuContributor() {
    ContextInjectors.inject(this);
  }

  @Override
  public void fill(Menu menu, int index) {
    subscription = sciManifestService.lookupCapabilities(new ManifestObjectFilter().type(getCapabilityType()), capabilities -> {
      capabilities.forEach(capability -> {
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
        menuItem.setText(Optional.ofNullable(getText(capability)).orElse("?"));
        menuItem.setToolTipText(getToolTipText(capability));
        menuItem.addSelectionListener(new SelectionAdapter() {

          public void widgetSelected(SelectionEvent event) {
            sciIntentClient.publish(new Intent().type(capability.type).qualifier(capability.qualifier)).exceptionally(e -> {
              Platform.getLog(CapabilityMenuContributor.class).error("Failed to send intent", e);
              return null;
            });
          }
        });
      });
    });
  }

  protected abstract String getCapabilityType();

  protected abstract String getText(Capability capability);

  protected abstract String getToolTipText(Capability capability);

  @Override
  public void dispose() {
    subscription.unsubscribe();
    super.dispose();
  }
}