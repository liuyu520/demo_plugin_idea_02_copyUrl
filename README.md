# demo_plugin_idea
idea 插件学习


## 报错

idea 插件开发,运行 gradle任务 runIde 时,报错

```
JDK Required: tools.jar seems to be not in IDEA classpath
Please ensure JAVA_HOME points to JDK rather than JRE
```
解决方法:
/Library/Java/JavaVirtualMachines/jdk1.8.0_77.jdk/Contents/Home/lib 
 cp tools.jar /Users/whuanghkl/.gradle/caches/modules-2/files-2.1/com.jetbrains/jbre/jbrex8u152b1136.39_osx_x64/jdk/Contents/Home/jre/lib/

## 参考
[http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/prerequisites.html](http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/prerequisites.html)
