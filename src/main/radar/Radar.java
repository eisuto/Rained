package main.radar;


import main.container.Container;
import main.container.ContainerReal;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Radar {
    public static Container container = new ContainerReal();
    private static final String CLASS_SUFFIX = ".class";
    private String defaultClassPath = Radar.class.getResource("/").getPath();

    public String getDefaultClassPath() {
        return defaultClassPath;
    }

    public void setDefaultClassPath(String defaultClassPath) {
        this.defaultClassPath = defaultClassPath;
    }

    private static class ClassSearcher {
        private Set<Class<?>> classPaths = new HashSet<>();

        private Set<Class<?>> doPath(File file, String packageName, Predicate<Class<?>> predicate, boolean flag) {

            if (file.isDirectory()) {
                //文件夹我们就递归
                File[] files = file.listFiles();
                if (!flag) {
                    packageName = packageName + "." + file.getName();
                }

                for (File f1 : files) {
                    doPath(f1, packageName, predicate, false);
                }
            } else {//标准文件
                //标准文件我们就判断是否是class文件
                if (file.getName().endsWith(CLASS_SUFFIX)) {
                    //如果是class文件我们就放入我们的集合中。
                    try {
                        Class<?> clazz = Class.forName(packageName + "." + file.getName().substring(0, file.getName().lastIndexOf(".")));
                        if (predicate == null || predicate.test(clazz)) {
                            classPaths.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return classPaths;
        }
    }

    public Set<Class<?>> search(String packageName, Predicate<Class<?>> predicate) {
        //先把包名转换为路径,首先得到项目的classpath
        String classpath = defaultClassPath;
        //然后把我们的包名basPack转换为路径名
        String basePackPath = packageName.replace(".", File.separator);
        String searchPath = classpath + basePackPath;
        return new ClassSearcher().doPath(new File(searchPath), packageName, predicate, true);
    }


    /**
     * 自动扫描包 注册类型
     *
     * @param packageName
     * @param annotationClass
     * @param <A>
     * @throws Exception
     */
    public <A extends Annotation> void getAnnotationClasses(String packageName, Class<A> annotationClass) throws Exception {
        // 寻找用了annotationClass注解的类，注册
        Set<Class<?>> clsList = search(packageName, null);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (cls.getAnnotation(annotationClass) != null && !cls.isInterface()) {
                    container.registerBean(cls);
                }

            }
        }
        container.initWired();
    }

    /**
     * 自动注入
     *
     * @param packageName
     * @param annotationClass
     * @param <A>
     * @throws Exception
     */
    public <A extends Annotation> void automaticInjection(String packageName, Class<A> annotationClass) throws Exception {
        Set<Class<?>> clsList = search(packageName, null);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                // 寻找用了annotationClass注解的字段，将注册过的字段类注入到此字段
                Field[] declaredFields = cls.getDeclaredFields();
                for (Field field : declaredFields) {
                    if (field.getAnnotation(annotationClass) != null) {
                        field.setAccessible(true);
                        Class<?> fieldClass = field.getType();
                        // 要注入的对象
                        Object objectS = container.getBean(fieldClass);
                        // 被注入的对象
                        Object objectM = container.getBean(cls);
                        Field fieldM = objectM.getClass().getDeclaredField(field.getName());
                        fieldM.setAccessible(true);
                        fieldM.set(objectM, objectS);
                        System.out.println();
                    }
                }
            }
        }
    }

}
