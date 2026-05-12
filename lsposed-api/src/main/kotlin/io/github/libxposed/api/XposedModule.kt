package io.github.libxposed.api

import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

/**
 * Super class which all Xposed module entry classes should extend.<br></br>
 * Entry classes will be instantiated exactly once for each process.
 */
abstract class XposedModule : XposedModuleInterface {
    /**
     * Instantiates a new Xposed module.
     */
    constructor()

    /**
     * Instantiates a new Xposed module.<br></br>
     * When the module is loaded into the target process, the constructor will be called.
     * 
     * @param base  The base context provided by the framework, should not be used by the module
     * @param param Information about the process in which the module is loaded
     */
    @Suppress("unused")
    constructor(base: XposedInterface, param: ModuleLoadedParam)

    /**
     * Gets notified when the module is loaded into the target process.<br></br>
     * This callback is guaranteed to be called exactly once for a process.
     * 
     * @param param Information about the process in which the module is loaded
     * @throws RuntimeException Everything the callback throws is caught and logged.
     */
    open fun onModuleLoaded(param: ModuleLoadedParam) {
    }

    /**
     * Gets notified when a android.R.attr#hasCode package is loaded into the process.
     * This is the time when the default classloader is ready but before the instantiation of
     * AppComponentFactory.
     * 
     * 
     * This callback is invoked only once for each package name loaded into the process,
     * note that a process may load multiple packages, such as android.R.attr#sharedUserId
     * and Context#createPackageContext(String, int) with Context#CONTEXT_INCLUDE_CODE.
     * 
     * @param param Information about the package being loaded
     * @throws RuntimeException Everything the callback throws is caught and logged.
     */
    open fun onPackageLoaded(param: PackageLoadedParam) {
    }
}
