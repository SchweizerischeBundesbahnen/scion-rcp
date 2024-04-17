/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.host;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;

/**
 * TODO Klasse dokumentieren
 */
@Component(service = FocusEventEmulatorInstaller.class)
public class FocusEventEmulatorInstaller {

  public void install(final Display display) {
    //    display.addFilter(SWT.FocusIn, event -> {
    //      Platform.getLog(getClass()).info("Focus in at widget=%s".formatted(event.widget));
    //    });
    //    display.addFilter(SWT.FocusOut, event -> {
    //      Platform.getLog(getClass()).info("Focus out at widget=%s".formatted(event.widget));
    //    });
    display.addFilter(SWT.Activate, event -> {
      Platform.getLog(getClass()).info("Activate at widget=%s".formatted(event.widget));
    });
    display.addFilter(SWT.Deactivate, event -> {
      Platform.getLog(getClass()).info("Deactivate at widget=%s".formatted(event.widget));
    });
  }
}
