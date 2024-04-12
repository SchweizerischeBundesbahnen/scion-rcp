/*
 * Project: RCS - Rail Control System
 *
 * © Copyright by SBB AG, Alle Rechte vorbehalten
 */
package ch.sbb.scion.rcp.workbench.internal;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * A general selection provider implementation.
 */
public class SelectionProvider implements ISelectionProvider {

  private final ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<>();
  private ISelection currentSelection;

  @Override
  public void addSelectionChangedListener(final ISelectionChangedListener listener) {
    selectionChangedListeners.add(listener);
  }

  @Override
  public ISelection getSelection() {
    return currentSelection;
  }

  @Override
  public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
    selectionChangedListeners.remove(listener);
  }

  @Override
  public void setSelection(final ISelection selection) {
    this.currentSelection = selection;
    // TODO: Should we only fire a selection changed event if the selection has actually changed? How to compare selections?
    var event = new SelectionChangedEvent(this, selection);
    selectionChangedListeners.forEach(l -> {
      // Borrowed from org.eclipse.jface.viewers.StructuredViewer:
      SafeRunnable.run(new SafeRunnable() {

        @Override
        public void run() {
          l.selectionChanged(event);
        }
      });
    });
  }
}
