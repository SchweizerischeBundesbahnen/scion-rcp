package ch.sbb.scion.rcp.microfrontend.e3.app.demo;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.sbb.scion.rcp.microfrontend.Activator;

public interface ContextInjectors {

  public static void inject(Object object) {
    BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
    IEclipseContext context = EclipseContextFactory.getServiceContext(bundleContext);
    ContextInjectionFactory.inject(object, context);
  }
}
