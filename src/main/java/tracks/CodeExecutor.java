package tracks;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 主执行类，演示如何动态编译和执行一个字符串形式的 Java 代码。
 */
public class CodeExecutor {

    public static void main(String[] args) throws IOException {
        // 1. 模拟从前端接收到的用户代码字符串
        // 假设我们约定用户提交的类名为 "UserSolution"
        // 并且需要实现一个名为 "solve" 的方法，该方法接收一个字符串参数并返回一个整数。
        String userCode = "package com.example;\n\n"
                + "public class UserSolution {\n"
                + "    public int solve(String s) {\n"
                + "        if (s == null) {\n"
                + "            return 0;\n"
                + "        }\n"
                + "        System.out.println(\"用户的方法被调用了，输入参数是: \" + s);\n"
                + "        return s.length();\n"
                + "    }\n"
                + "}\n";

        // 预计的完整类名 (包含包名)
        final String fullClassName = "com.example.UserSolution";

        // 获取系统 Java 编译器
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("找不到 Java 编译器。请确保您使用的是 JDK 而不是 JRE。");
            return;
        }

        // 用于收集编译错误的诊断监听器
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        // 用于管理编译后字节码的文件管理器
        InMemoryFileManager fileManager = new InMemoryFileManager(compiler.getStandardFileManager(null, null, null));

        // 准备要编译的源文件对象
        JavaFileObject sourceFile = new StringJavaFileObject(fullClassName, userCode);
        Iterable<? extends JavaFileObject> compilationUnits = Collections.singletonList(sourceFile);

        // 2. 执行编译任务
        System.out.println("开始动态编译...");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        boolean success = task.call();

        if (success) {
            System.out.println("编译成功！");
            try {
                // 3. 创建自定义类加载器来加载内存中的字节码
                InMemoryClassLoader classLoader = new InMemoryClassLoader(fileManager.getByteCodeMap());

                // 4. 加载用户类
                System.out.println("正在加载类: " + fullClassName);
                Class<?> userClass = classLoader.loadClass(fullClassName);

                // 5. 通过反射创建实例并调用方法
                System.out.println("正在创建实例并调用 'solve' 方法...");
                Object instance = userClass.getDeclaredConstructor().newInstance();
                Method method = userClass.getMethod("solve", String.class);

                // 准备测试用的参数
                String testInput = "Hello, Dynamic World!";
                Object result = method.invoke(instance, testInput);

                System.out.println("方法执行完毕！");
                System.out.println("=====================================");
                System.out.println("最终执行结果: " + result);
                System.out.println("=====================================");

            } catch (Exception e) {
                System.err.println("执行用户代码时出错:");
                e.printStackTrace();
            }
        } else {
            System.err.println("编译失败！");
            // 6. 如果编译失败，打印详细错误
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                System.err.format("错误: %s\n行号: %d\n源码: %s\n",
                        diagnostic.getMessage(null),
                        diagnostic.getLineNumber(),
                        diagnostic.getSource().getCharContent(true));
            }
        }
    }
}

/**
 * 用于从字符串读取 Java 源代码的 JavaFileObject 实现。
 */
class StringJavaFileObject extends SimpleJavaFileObject {
    private final String code;

    StringJavaFileObject(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}

/**
 * 用于将编译后的字节码保存在内存中的 JavaFileObject 实现。
 */
class BytecodeJavaFileObject extends SimpleJavaFileObject {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    BytecodeJavaFileObject(String name, Kind kind) {
        super(URI.create("memory:///" + name.replace('.', '/') + kind.extension), kind);
    }

    byte[] getBytes() {
        return outputStream.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() {
        return outputStream;
    }
}

/**
 * 自定义文件管理器，用于管理内存中的源文件和字节码。
 */
class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, BytecodeJavaFileObject> byteCodeMap = new HashMap<>();

    InMemoryFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    public Map<String, BytecodeJavaFileObject> getByteCodeMap() {
        return byteCodeMap;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            BytecodeJavaFileObject fileObject = new BytecodeJavaFileObject(className, kind);
            byteCodeMap.put(className, fileObject);
            return fileObject;
        }
        return super.getJavaFileForOutput(location, className, kind, sibling);
    }
}

/**
 * 自定义类加载器，从内存中的字节码加载类。
 */
class InMemoryClassLoader extends ClassLoader {
    private final Map<String, BytecodeJavaFileObject> byteCodeMap;

    InMemoryClassLoader(Map<String, BytecodeJavaFileObject> byteCodeMap) {
        this.byteCodeMap = byteCodeMap;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        BytecodeJavaFileObject fileObject = byteCodeMap.get(name);
        if (fileObject != null) {
            byte[] bytes = fileObject.getBytes();
            // defineClass 是将字节码转换为 Class 对象的关键方法
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
