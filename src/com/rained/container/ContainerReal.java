package com.rained.container;

import com.rained.annotation.Ingredient;
import sun.reflect.misc.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器实现
 *
 * @author eisuto
 */
@SuppressWarnings("unchecked")
public class ContainerReal implements Container {

    /**
     * 保存Bean的空间
     * {全类名 : @object}
     */
    private Map<String, Object> beans;

    /**
     * 保存Bean和Name的空间
     * {全类名 : Name}
     */
    private Map<String, String> beanKeys;


    public ContainerReal() {
        this.beans = new ConcurrentHashMap<>();
        this.beanKeys = new ConcurrentHashMap<>();
    }

    /**
     * 根据 Class 获取Bean
     */
    @Override
    public <T> T getBean(Class<T> clazz) {
        String name = clazz.getName();
        Object obj = beans.get(name);
        if (null != obj) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 根据 Name 获取Bean
     */
    @Override
    public <T> T getBeanByName(String name) {
        String className = beanKeys.get(name);
        Object obj = beans.get(className);
        if (null != obj) {
            return (T) obj;
        }
        return null;
    }

    /**
     * 注册 Bean 到容器中
     */
    @Override
    public Object registerBean(Object bean) {
        String name = bean.getClass().getName();
        beanKeys.put(name, name);
        beans.put(name, bean);
        return bean;
    }

    /**
     * 注册 Class 到容器中
     */
    @Override
    public Object registerBean(Class<?> clazz) {
        String name = clazz.getName();
        beanKeys.put(name, name);
        Object bean;
        try {
            bean = ReflectUtil.newInstance(clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        beans.put(name, bean);
        return bean;
    }

    /**
     * 注册 带Name的Bean 到容器中
     */
    @Override
    public Object registerBean(String name, Object bean) {
        String className = bean.getClass().getName();
        beanKeys.put(name, className);
        beans.put(className, bean);
        return bean;
    }

    /**
     * 删除 根据 Class
     */
    @Override
    public void remove(Class<?> clazz) {
        String className = clazz.getName();
        if (null != className && !"".equals(className)) {
            beanKeys.remove(className);
            beans.remove(className);
        }
    }

    /**
     * 删除 根据 Name
     */
    @Override
    public void removeByName(String name) {
        String className = beanKeys.get(name);
        if(null != className && !"".equals(className)){
            beanKeys.remove(name);
            beans.remove(className);
        }
    }

    /**
     * 返回所有Bean对象的Name
     */
    @Override
    public Set<String> getBeanNames() {
        return beanKeys.keySet();
    }

    /**
     * 初始化装配
     */
    @Override
    public void initWired() {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object object = entry.getValue();
            injection(object);
        }
    }
    /**
     * 注入对象
     */
    private void injection(Object object) {
        // 所有字段
        try {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 需要注入的字段
                Ingredient autoWired = field.getAnnotation(Ingredient.class);
                if (null != autoWired) {
                    // 要注入的字段
                    Object autoWiredField = null;
                    String name = autoWired.name();
                    if(!"".equals(name)){
                        String className = beanKeys.get(name);
                        if(null != className && !"".equals(className)){
                            autoWiredField = beans.get(className);
                        }
                        if (null == autoWiredField) {
                            throw new RuntimeException("Unable to load " + name);
                        }
                    } else {
                        if(autoWired.value() == Class.class){
                            autoWiredField = recursiveAssembly(field.getType());
                        } else {
                            // 指定装配的类
                            autoWiredField = this.getBean(autoWired.value());
                            if (null == autoWiredField) {
                                autoWiredField = recursiveAssembly(autoWired.value());
                            }
                        }
                    }
                    if (null == autoWiredField) {
                        throw new RuntimeException("Unable to load " + field.getType().getCanonicalName());
                    }
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(object, autoWiredField);
                    field.setAccessible(accessible);
                }
            }
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Object recursiveAssembly(Class<?> clazz){
        if(null != clazz){
            return this.registerBean(clazz);
        }
        return null;
    }
}
