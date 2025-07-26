package org.example.autoexport;

import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;

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

    private static RECT lastRect = null;
    public static boolean hasWindowSizeChanged(HWND hwnd) {
        if (hwnd == null) return false;

        RECT currentRect = new RECT();
        User32.INSTANCE.GetWindowRect(hwnd, currentRect);

        if (lastRect == null) {
            lastRect = new RECT();
            lastRect.left = currentRect.left;
            lastRect.top = currentRect.top;
            lastRect.right = currentRect.right;
            lastRect.bottom = currentRect.bottom;
            return false; // 第一次检查，认为没有变化
        }

        boolean changed = !rectEquals(lastRect, currentRect);
        if (changed) {
            // 更新为当前尺寸
            lastRect.left = currentRect.left;
            lastRect.top = currentRect.top;
            lastRect.right = currentRect.right;
            lastRect.bottom = currentRect.bottom;
        }

        return changed;
    }
    private static boolean rectEquals(RECT r1, RECT r2) {
        return r1.left == r2.left &&
                r1.top == r2.top &&
                r1.right == r2.right &&
                r1.bottom == r2.bottom;
    }

    public static void restartProcessFromHwnd() throws IOException, InterruptedException {
        HWND hwnd = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "剪映专业版");

        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));
        String exePath = props.getProperty("exePath");

        if (hwnd == null) {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd", "/c", "start", "\"\"", "\"" + exePath + "\""
            );
            builder.start();
        } else {
            int pid = getProcessIdFromHwnd(hwnd);

            String runningExePath = getExePathByPid(pid);
            Runtime.getRuntime().exec("taskkill /PID " + pid + " /F").waitFor();
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd", "/c", "start", "\"\"", "\"" + runningExePath + "\""
            );
            builder.start();
        }
    }

    public static void closeAllJianyingPro() throws Exception {
        HWND hwnd = User32.INSTANCE.FindWindow("Qt622QWindowIcon", "剪映专业版");
        int pid = getProcessIdFromHwnd(hwnd);
        Runtime.getRuntime().exec("taskkill /PID " + pid + " /F").waitFor();
    }


    public static boolean deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return false;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // 递归删除子目录
                } else {
                    file.delete();         // 删除文件
                }
            }
        }

        return dir.delete(); // 最后删除空目录自身
    }
}
