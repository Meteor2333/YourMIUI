package cc.meteormc.yourmiui.core;

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

    static Object[] getScopes(Class<Scope> interfaceClass) {
        return new Object[0];
    }
}