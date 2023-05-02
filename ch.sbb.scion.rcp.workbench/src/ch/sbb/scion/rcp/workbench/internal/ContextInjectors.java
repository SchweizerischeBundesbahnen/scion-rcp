package ch.sbb.scion.rcp.workbench.internal;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import ch.sbb.scion.rcp.workbench.Activator;

public interface ContextInjectors {

  public static void inject(final Object object) {
    BundleContext bundleContext = FrameworkUtil.getBundle(Activator.class).getBundleContext();
    IEclipseContext context = EclipseContextFactory.getServiceContext(bundleContext);
    ContextInjectionFactory.inject(object, context);
  }
}
