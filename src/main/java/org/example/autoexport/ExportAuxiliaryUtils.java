package org.example.autoexport;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ExportAuxiliaryUtils {
    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP = 0x0202;

    private static WinDef.LPARAM MAKELPARAM(long x, long y) {
        return new WinDef.LPARAM((y << 16) | (x & 0xFFFF));
    }

    public static void click(HWND hwnd,int Dx,int Dy, int Fx, int Fy) {
        Shcore.INSTANCE.SetProcessDpiAwareness(2);
        User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
        WinDef.RECT rect = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(hwnd, rect);
        int temporary_X = rect.right - rect.left;
        int temporary_Y = rect.bottom - rect.top;

        long x = (long)((double)Fx/(double)Dx*temporary_X);
        long y = (long)((double)Fy/(double)Dy*temporary_Y);

        WinDef.LPARAM lParam = MAKELPARAM(x, y);
        // 模拟点击：先按下，再松开
        MyUser32.INSTANCE.PostMessage(hwnd, WM_LBUTTONDOWN, new WinDef.WPARAM(1), lParam);
        MyUser32.INSTANCE.PostMessage(hwnd, WM_LBUTTONUP, new WinDef.WPARAM(0), lParam);
    }

    private static int getProcessIdFromHwnd(WinDef.HWND hwnd) {
        IntByReference pid = new IntByReference();
        MyUser32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
        return pid.getValue();
    }

    private static String getExePathByPid(int pid) throws IOException {
        Process process = Runtime.getRuntime().exec(
                new String[]{"powershell.exe", "-Command",
                        "(Get-Process -Id " + pid + ").Path"});
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank() && line.endsWith(".exe")) {
                return line.trim();
            }
        }
        return null;
    }


    public static void restartProcessFromHwnd() throws IOException, InterruptedException {
        HWND hwnd = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "剪映专业版");
        int pid = getProcessIdFromHwnd(hwnd);

        String exePath = getExePathByPid(pid);
        Runtime.getRuntime().exec("taskkill /PID " + pid + " /F").waitFor();
        Path newPath = Paths.get(exePath).getParent().resolve("VEDetector.exe");
        File veDetectorFile = newPath.toFile();
        if (veDetectorFile.exists()) {
            veDetectorFile.delete();
        }

        Runtime.getRuntime().exec(new String[]{
                "cmd", "/c", "start", "\"\"", exePath
        });
    }

}
