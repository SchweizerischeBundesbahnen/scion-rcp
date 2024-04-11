/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench;

import org.eclipse.ui.IWorkbenchPart;

/**
 * TODO Klasse dokumentieren
 */
public interface IMicrofrontendViewPart extends IWorkbenchPart {

  String ID = "ch.sbb.scion.rcp.workbench.editors.MicrofrontendViewEditor";

  String getViewId();
}
