package io.github.libxposed.api

/**
 * Interface for module initialization.
 */
interface XposedModuleInterface {
    /**
     * Wraps information about the process in which the module is loaded.
     * This information only indicates the state at the time of loading and will not be updated.
     */
    interface ModuleLoadedParam {
        /**
         * Returns whether the current process is system server.
         * 
         * @return `true` if the current process is system server
         */
        val isSystemServer: Boolean

        /**
         * Gets the process name.
         * 
         * @return The process name
         */
        val processName: String
    }

    /**
     * Wraps information about the package being loaded.
     * This information only indicates the state at the time of loading and will not be updated.
     */
    interface PackageLoadedParam {
        /**
         * Gets the package name of the current package.
         * 
         * @return The package name.
         */
        val packageName: String

        /**
         * Gets the default classloader of the current package. This is the classloader that loads
         * the package's code, resources and custom AppComponentFactory.
         */
        val defaultClassLoader: ClassLoader
    }
}
