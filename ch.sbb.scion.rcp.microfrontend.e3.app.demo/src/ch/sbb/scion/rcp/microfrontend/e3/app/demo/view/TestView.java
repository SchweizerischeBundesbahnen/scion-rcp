/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.microfrontend.e3.app.demo.view;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.sbb.scion.rcp.microfrontend.model.Intent;
import ch.sbb.scion.rcp.workbench.ISciWorkbenchViewInput;

/**
 * A test view for showcasing the injection of a view input.
 */
public class TestView {

  @Inject
  @Optional
  private ISciWorkbenchViewInput viewInput;

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @PostConstruct
  public void createComposite(final Composite parent) {
    var area = CompositeFactory.newComposite(SWT.NONE).create(parent);
    GridLayoutFactory.fillDefaults().applyTo(area);

    LabelFactory.newLabel(SWT.NONE).text("Parameters:").create(area);
    var paramsText = TextFactory.newText(SWT.BORDER | SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL).create(area);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(paramsText);
    paramsText.setText(getParamsAsPrettyString());
  }

  private String getParamsAsPrettyString() {
    if (viewInput == null) {
      return "no view input";
    }
    return gson.toJson(getCombinedParams(viewInput.getIntent()));
  }

  private static Map<String, Object> getCombinedParams(final Intent intent) {
    var params = new HashMap<String, Object>();
    if (intent.getParams() != null) {
      intent.getParams().forEach(params::put);
    }
    if (intent.getQualifier() != null) {
      intent.getQualifier().entries.forEach(params::put);
    }
    return params;
  }

}
