package com.rained.container;

import java.util.Set;

public interface Container {
    /**
     * 根据 Class 获取Bean
     */
    public <T> T getBean(Class<T> clazz);

    /**
     * 根据 Name 获取Bean
     */
    public <T> T getBeanByName(String name);

    /**
     * 注册 Bean 到容器中
     */
    public Object registerBean(Object bean);

    /**
     * 注册 Class 到容器中
     */
    public Object registerBean(Class<?> clazz);

    /**
     * 注册 带Name的Bean 到容器中
     */
    public Object registerBean(String name, Object bean);

    /**
     * 删除 根据 Class
     */
    public void remove(Class<?> clazz);

    /**
     * 删除 根据 Name
     */
    public void removeByName(String name);

    /**
     * 返回所有Bean对象的Name
     */
    public Set<String> getBeanNames();

    /**
     * 初始化装配
     */
    public void initWired();
}
