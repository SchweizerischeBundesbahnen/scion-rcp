package ch.sbb.scion.rcp.microfrontend.keyboard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.osgi.service.component.annotations.Component;

/**
 * For browser key values, see https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values
 */
@Component(service = KeyboardEventMapper.class)
public class KeyboardEventMapper {

  public Event mapKeyboardEvent(JavaScriptKeyboardEvent keyboardEvent) {
    var event = new Event();
    event.stateMask = stateMask(keyboardEvent);
    event.character = character(keyboardEvent);
    event.keyCode = keycode(keyboardEvent);
    event.type = type(keyboardEvent);
    return event;
  }

  private int type(JavaScriptKeyboardEvent keyboardEvent) {
    switch (keyboardEvent.type()) {
    case "keydown": {
      return SWT.KeyDown;
    }
    case "keyup": {
      return SWT.KeyUp;
    }
    default:
      return SWT.None;
    }
  }

  private int stateMask(JavaScriptKeyboardEvent keyboardEvent) {
    int stateMask = 0;
    if (keyboardEvent.ctrlKey()) {
      stateMask |= SWT.CTRL;
    }
    if (keyboardEvent.shiftKey()) {
      stateMask |= SWT.SHIFT;
    }
    if (keyboardEvent.altKey()) {
      stateMask |= SWT.ALT;
    }
    if (keyboardEvent.metaKey()) {
      stateMask |= SWT.COMMAND;
    }
    return stateMask;
  }

  static char character(JavaScriptKeyboardEvent keyboardEvent) {
    String key = keyboardEvent.key();
    switch (key) {
    case "Backspace":
      return SWT.BS;
    case "Enter":
      return SWT.LF;
    case "Delete":
      return SWT.DEL;
    case "Escape":
      return SWT.ESC;
    case "Tab":
      return SWT.TAB;
    case " ":
      return SWT.SPACE;
    default:
      if (key.length() == 1) {
        return key.charAt(0);
      }
      return 0;
    }
  }

  static int keycode(JavaScriptKeyboardEvent keyboardEvent) {
    var key = keyboardEvent.key();
    switch (key) {
    case "ArrowUp":
      return SWT.ARROW_UP;
    case "ArroDown":
      return SWT.ARROW_DOWN;
    case "ArrowLeft":
      return SWT.ARROW_LEFT;
    case "ArrowRight":
      return SWT.ARROW_RIGHT;
    case "PageUp":
      return SWT.PAGE_UP;
    case "PageDown":
      return SWT.PAGE_DOWN;
    case "Home":
      return SWT.HOME;
    case "End":
      return SWT.END;
    case "Insert":
      return SWT.INSERT;
    case "F1":
      return SWT.F1;
    case "F2":
      return SWT.F2;
    case "F3":
      return SWT.F3;
    case "F4":
      return SWT.F4;
    case "F5":
      return SWT.F5;
    case "F6":
      return SWT.F6;
    case "F7":
      return SWT.F7;
    case "F8":
      return SWT.F8;
    case "F9":
      return SWT.F9;
    case "F10":
      return SWT.F10;
    case "F11":
      return SWT.F11;
    case "F12":
      return SWT.F12;
    case "F13":
      return SWT.F13;
    case "F14":
      return SWT.F14;
    case "F15":
      return SWT.F15;
    case "F16":
      return SWT.F16;
    case "F17":
      return SWT.F17;
    case "F18":
      return SWT.F18;
    case "F19":
      return SWT.F19;
    case "F20":
      return SWT.F20;
    case "Multiply":
      return SWT.KEYPAD_MULTIPLY;
    case "Add":
      return SWT.KEYPAD_ADD;
    case "Subtract":
      return SWT.KEYPAD_SUBTRACT;
    case "Decimal":
      return SWT.KEYPAD_DECIMAL;
    case "Divide":
      return SWT.KEYPAD_DIVIDE;
    case "0":
      return SWT.KEYPAD_0;
    case "1":
      return SWT.KEYPAD_1;
    case "2":
      return SWT.KEYPAD_2;
    case "3":
      return SWT.KEYPAD_3;
    case "4":
      return SWT.KEYPAD_4;
    case "5":
      return SWT.KEYPAD_5;
    case "6":
      return SWT.KEYPAD_6;
    case "7":
      return SWT.KEYPAD_7;
    case "8":
      return SWT.KEYPAD_8;
    case "9":
      return SWT.KEYPAD_9;
    case "Help":
      return SWT.HELP;
    case "ScrollLock":
      return SWT.SCROLL_LOCK;
    case "Pause":
      return SWT.PAUSE;
    case "PrintScreen":
      return SWT.PRINT_SCREEN;
    default: {
      if (key.length() == 1) {
        return key.toLowerCase().charAt(0);
      }
      return 0;
    }
    }
  }
}
