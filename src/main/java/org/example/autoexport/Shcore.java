package org.example.autoexport;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface Shcore extends StdCallLibrary {
    Shcore INSTANCE = Native.load("Shcore", Shcore.class);
    int SetProcessDpiAwareness(int value);
}
