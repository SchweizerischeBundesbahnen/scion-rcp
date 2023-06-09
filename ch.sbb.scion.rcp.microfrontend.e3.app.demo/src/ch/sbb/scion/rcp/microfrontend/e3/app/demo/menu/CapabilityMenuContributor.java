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

import ch.sbb.scion.rcp.microfrontend.ManifestService;
import ch.sbb.scion.rcp.microfrontend.IntentClient;
import ch.sbb.scion.rcp.microfrontend.e3.app.demo.ContextInjectors;
import ch.sbb.scion.rcp.microfrontend.model.Capability;
import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.microfrontend.model.ManifestObjectFilter;
import ch.sbb.scion.rcp.microfrontend.subscriber.ISubscription;

/**
 * Contributes menu items for capabilities of a given type.
 */
public abstract class CapabilityMenuContributor extends ContributionItem {

  private ISubscription subscription;

  @Inject
  private ManifestService sciManifestService;

  @Inject
  private IntentClient sciIntentClient;

  public CapabilityMenuContributor() {
    ContextInjectors.inject(this);
  }

  @Override
  public void fill(final Menu menu, final int index) {
    subscription = sciManifestService.lookupCapabilities(ManifestObjectFilter.builder().type(getCapabilityType()).build(), capabilities -> {
      capabilities.forEach(capability -> {
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
        menuItem.setText(Optional.ofNullable(getText(capability)).orElse("?"));
        menuItem.setToolTipText(getToolTipText(capability));
        menuItem.addSelectionListener(new SelectionAdapter() {

          @Override
          public void widgetSelected(final SelectionEvent event) {
            sciIntentClient.publish(Intent.builder().type(capability.type()).qualifier(capability.qualifier()).build()).exceptionally(e -> {
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