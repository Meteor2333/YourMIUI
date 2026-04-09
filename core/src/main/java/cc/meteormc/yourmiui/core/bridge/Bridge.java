package cc.meteormc.yourmiui.core.bridge;

import java.util.Collections;
import java.util.List;

// These method is hooked in cc.meteormc.yourmiui.xposed.XposedEntry#handleLoadPackage()
public interface Bridge {
    static String getApiName() {
        return null;
    }

    static int getApiVersion() {
        return -1;
    }

    static boolean isModuleActivated() {
        return false;
    }

    static <T> List<T> getScopes() {
        return Collections.emptyList();
    }
}