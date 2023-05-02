package ch.sbb.scion.rcp.microfrontend;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

  // The plug-in ID
  public static final String PLUGIN_ID = "ch.sbb.scion.rcp.microfrontend"; //$NON-NLS-1$

  // The shared instance
  private static Activator plugin;

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  public static Activator getDefault() {
    return plugin;
  }

}
