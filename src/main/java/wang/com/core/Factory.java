package wang.com.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import wang.com.type.Autowired;
import wang.com.type.Component;

public class Factory {
    private static Map<Class<?>, Object> _clazzes = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clazz) {
        if (_clazzes.get(clazz) == null)
            throw new RuntimeException("No class [" + clazz.getName() + "] exists.");

        return (T)_clazzes.get(clazz);
	}

    public Factory build(String packageName){
        if (packageName == null) throw new IllegalArgumentException("Null parameter");
        
        List<Class<?>> lst = getClassesInPackage(packageName);

        lst.stream().forEach(el ->{
            _clazzes.put(el, createInstance(el));
        });

        lst.stream().forEach(el ->{
            try{
                for (Field f : el.getDeclaredFields()){
                    if (f.getAnnotation(Autowired.class) != null){
                        if (isBeanClass(f.getType())){
                            Object o = _clazzes.get(f.getType());
                            f.setAccessible(true);
                            f.set(_clazzes.get(el), o);
                        }
                    }
                }
            }catch(Exception e) {throw new RuntimeException(e);}
        });

        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) {
        Enhancer ehance = new Enhancer();
		ehance.setSuperclass(clazz);
        ehance.setCallback(new Interceptor());

        return (T) (ehance.create());
    }

    private List<Class<?>> getClassesInPackage(String packageName){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = packageName.replace('.', File.separatorChar);

        ArrayList<Class<?>> classes = new ArrayList<>();

        try{
            Enumeration<URL> resources = classLoader.getResources(path);
        
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                dirs.add(new File(URLDecoder.decode(resources.nextElement().getFile(), "utf8")));
            }

            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName));
            }
        }catch (Exception e){ throw new RuntimeException(e);}

        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        
        if (!directory.exists()) {
            return classes;
        }

        try{
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                    if (isBeanClass(clazz)) classes.add(clazz);
                }
            }
        }catch (Exception e) { throw new RuntimeException(e);}
            
        return classes;
    }

    private boolean isBeanClass(Class<?> clazz){
        return clazz.getAnnotation(Component.class) != null;
    }
}

class Interceptor implements MethodInterceptor {
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        return proxy.invokeSuper(obj, args);
    }
}