package com.darts.servlet;

import com.darts.annotation.*;
import com.darts.controller.TestController;
import com.sun.org.apache.xpath.internal.Arg;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatchServlet extends HttpServlet {

    List<String> classNames = new ArrayList<String>();

    Map<String,Object> beans = new HashMap<String ,Object>();

    Map<String,Object> handleMap = new HashMap<String, Object>();

    /**
     * 初始化tomcat启动时 实例化map ioc
     * @param config
     */
    public void init(ServletConfig config){
        basePackageScan("com.darts");

        //对classNames进行实例化
        doInstance();
        //实现自动注入
        doAutowired();
        //初始化url和方法映射关系
        doUrlMapping();//进行url和方法关系准备
    }

    /**
     * 循环map中所有的实例，并判断是否是控制类
     */
    private void doUrlMapping() {
        for(Map.Entry<String,Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(DartsController.class)) {
                DartsRequestMapping mapping = clazz.getAnnotation(DartsRequestMapping.class);
                String classPath = mapping.value();

                //提取实例所有的方法，并循环
                Method[] methods = clazz.getMethods();
                for(Method method : methods){
                    if(method.isAnnotationPresent(DartsRequestMapping.class)){
                        DartsRequestMapping mapping1 = method.getAnnotation(DartsRequestMapping.class);
                        String methodPath = mapping1.value();// query

                        String requestPath = classPath + "/" + methodPath;// /darts/query  -->method
                        //放入到handleMap中
                        handleMap.put(requestPath,method);
                    }
                }
            }
        }
    }

    /**
     * 实现类实例的注入
     */
    private void doAutowired() {
        for(Map.Entry<String,Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if(clazz.isAnnotationPresent(DartsController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for(Field field : fields){
                    if(field.isAnnotationPresent(DartsAutowired.class)){
                        DartsAutowired auto = field.getAnnotation(DartsAutowired.class);
                        String key = auto.value();
                        Object bean = beans.get(key);
                        field.setAccessible(true);
                        try {
                            field.set(instance,bean);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        continue;
                    }
                }
            }else{
                continue;
            }
        }
    }

    private void doInstance() {
        for(String className : classNames){
            //com.darts.......TestService.java
            String cn = className.replace(".class","");
            try {
                Class<?> clazz = Class.forName(cn);
                if(clazz.isAnnotationPresent(DartsController.class)){
                    //控制类
                    Object instance = clazz.newInstance();
                    DartsRequestMapping mapping = clazz.getAnnotation(DartsRequestMapping.class);
                    String key = mapping.value();

                    //添加实例到map
                    beans.put(key,instance);
                }else if(clazz.isAnnotationPresent(DartsService.class)){
                    //服务类
                    Object instance = clazz.newInstance();
                    DartsService service = clazz.getAnnotation(DartsService.class);
                    String key = service.value();

                    //添加实例到map
                    beans.put(key,instance);
                }else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    private void basePackageScan(String basePackage) {
        //扫描编译好的类路径  class
        URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);

        String[] filesStr = file.list();

        for(String path : filesStr){
            File filePath = new File(fileStr + path);
            if(filePath.isDirectory()){
                basePackageScan(basePackage+"." + path);
            }else{
                classNames.add(basePackage+"." + filePath.getName());
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();//取得请求路径  /darts-Mvc/darts/query

        String context = req.getContextPath(); //  /darts-mvc

        String path = uri.replace(context,"");

        Method method = (Method) handleMap.get(path);

        TestController instance = (TestController) beans.get("/" + path.split("/")[1]);
        Object[] args = hand(req,resp,method);

        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Object[] hand(HttpServletRequest request,HttpServletResponse response,Method method){
        //拿到当前待执行的方法有哪些参数
        Class<?>[] paramClazzs = method.getParameterTypes();
        //根据参数的个数 new一个参数的数组，将方法里的所有参数赋值到args中来
        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;
        for(Class<?> paramClazz : paramClazzs){
            if(ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = request;
            }
            if(ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = request;
            }
            //从0-3判断有没有requestParam注解，很明显paramClazz为0和1时，不是
            //当2和3为@requestParam，需要解析
            //[@com.darts.annotation.DartsRequestParam(value=name)]
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if(paramAns.length>0){
                for(Annotation paramAn : paramAns){
                    if(DartsRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        DartsRequestParam rp = (DartsRequestParam) paramAn;
                        //找到注解里的name和age
                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
        }
        return args;
    }
}
