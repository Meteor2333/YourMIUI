package cc.meteormc.yourmiui.core.util

import java.lang.reflect.Proxy

/**
 * 由于 Xposed 特殊的模块加载机制
 * Xposed 这个项目模块 (以下简称模块) 和 App 模块的类不被同一个 ClassLoader 加载
 * 导致实际内容不互通 所以这里使用 Proxy 来优雅地解决这个问题

 * 下面把 App 模块的类的ClassLoader称为 `a` Xposed 模块的类的 ClassLoader 称为 `b` 以方便解释
 * 如上面所说 哪怕打开的是同一个软件 这里 a 与 b 也是不同的
 * 这里使用代理将实际的类型进行一层包装 使其可以强转为指定类型
 * 比方说 比如现在有一个被 b 加载的 `clz` 我要在 a 的 ClassLoader 环境下使用这个 clz 的对象
 * 如果直接使用 虽然它们在源码里看上去是同一个 Class 但运行时是两个完全不同的 Class 实例
 * 所以这时会进行强转进而导致 ClassCastException
 * (具体可参考 JVM 或 ART 类加载体系原理解析 本质大同小异)
 * 那么我们使 clz 实现一个接口 这个接口可以被两个 ClassLoader 分别加载
 * 然后这里将它进行一次包装 得到一个允许被 a 加载的 内部逻辑和 clz 类似的对象
 *
 * 最后 这个工具也仅仅达到了本项目的需求
 * 而且这种方案说到底还是一种打补丁的方案
 * 实际要想做到真正好用 还有很多的坑要踩~~
 */
fun proxyClass(
    // 期望的接口类型（此类被a加载）
    interfaceClass: Class<*>,
    // 真实对象（此对象的类被b加载 可对应上述clz的那个对象）
    target: Any
): Any {
    // 使用a创建代理
    // 让消费者"看起来"好像实现了这个接口
    return Proxy.newProxyInstance(
        interfaceClass.classLoader,
        arrayOf(interfaceClass)
    ) { _, method, args ->
        // 注意:
        // `method`是由a所加载的方法
        // `target`是由b所加载的对象
        // 二者互相不认识 所以不能直接 method.invoke(target)
        // 必须从method找到target的类中对应的真实方法并调用
        target.javaClass.getMethod(
            method.name,
            *method.parameterTypes
        ).invoke(
            target,
            *(args ?: emptyArray())
        )
    }
}