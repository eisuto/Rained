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
        return new ClassSearcher().doPath(new File(searchPath),packageName, predicate,true);
    }

        /**
         * 从包package中获取所有的Class
         *
         * @param packageName
         * @return
         */
        public Set<Class<?>> getClasses(String packageName) throws Exception {

            // 第一个class类的集合
            //List<Class<?>> classes = new ArrayList<Class<?>>();
            Set<Class<?>> classes = new HashSet<>();
            // 是否循环迭代
            boolean recursive = true;
            // 获取包的名字 并进行替换
            String packageDirName = packageName.replace('.', '/');
            // 定义一个枚举的集合 并进行循环来处理这个目录下的things
            Enumeration<URL> dirs;
            try {
                dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
                // 循环迭代下去
                while (dirs.hasMoreElements()) {
                    // 获取下一个元素
                    URL url = dirs.nextElement();
                    // 得到协议的名称
                    String protocol = url.getProtocol();
                    // 如果是以文件的形式保存在服务器上
                    if ("file".equals(protocol)) {
                        // 获取包的物理路径
                        String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                        // 以文件的方式扫描整个包下的文件 并添加到集合中
                        addClass(classes, filePath, packageName);
                    } else if ("jar".equals(protocol)) {
                        // 如果是jar包文件
                        // 定义一个JarFile
                        JarFile jar;
                        try {
                            // 获取jar
                            jar = ((JarURLConnection) url.openConnection()).getJarFile();
                            // 从此jar包 得到一个枚举类
                            Enumeration<JarEntry> entries = jar.entries();
                            // 同样的进行循环迭代
                            while (entries.hasMoreElements()) {
                                // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                                JarEntry entry = entries.nextElement();
                                String name = entry.getName();
                                // 如果是以/开头的
                                if (name.charAt(0) == '/') {
                                    // 获取后面的字符串
                                    name = name.substring(1);
                                }
                                // 如果前半部分和定义的包名相同
                                if (name.startsWith(packageDirName)) {
                                    int idx = name.lastIndexOf('/');
                                    // 如果以"/"结尾 是一个包
                                    if (idx != -1) {
                                        // 获取包名 把"/"替换成"."
                                        packageName = name.substring(0, idx).replace('/', '.');
                                    }
                                    // 如果可以迭代下去 并且是一个包
                                    if ((idx != -1) || recursive) {
                                        // 如果是一个.class文件 而且不是目录
                                        if (name.endsWith(".class") && !entry.isDirectory()) {
                                            // 去掉后面的".class" 获取真正的类名
                                            String className = name.substring(packageName.length() + 1, name.length() - 6);
                                            try {
                                                // 添加到classes
                                                classes.add(Class.forName(packageName + '.' + className));
                                            } catch (ClassNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return classes;
        }

        public void addClass(Set<Class<?>> classes, String filePath, String packageName) throws Exception {
            File[] files = new File(filePath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
            assert files != null;
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile()) {
                    String className = fileName.substring(0, fileName.lastIndexOf("."));
                    if (!packageName.isEmpty()) {
                        className = packageName + "." + className;
                    }
                    doAddClass(classes, className);
                }

            }
        }

        public void doAddClass(Set<Class<?>> classes, final String classsName) throws Exception {
            ClassLoader classLoader = new ClassLoader() {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    return super.loadClass(name);
                }
            };
            classes.add(classLoader.loadClass(classsName));
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
//            Set<Class<?>> clsList = getClasses(packageName);
            Set<Class<?>> clsList = search(packageName,null);
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
            Set<Class<?>> clsList = getClasses(packageName);
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
                            Field fieldM = objectM.getClass().getField(field.getName());
                            fieldM.set(objectM, objectS);
//                        BeanHelper.setProperty(objectM,field.getName(),objectS);
                            System.out.println();
                        }
                    }
                }
            }
        }

    }
