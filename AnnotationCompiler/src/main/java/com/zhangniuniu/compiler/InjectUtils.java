package com.zhangniuniu.compiler;

import java.lang.reflect.InvocationTargetException;

/**
 * @author：zhangyong
 * @email：zhangyonglncn@gmail.com
 * @create_time: 05/11/2018 17:27
 * @description：
 */
public class InjectUtils {

    private static final String SUFFIX = "$$STUDY";

    public static void inject(Object object) {
        try {
            IViewInject iViewInject = (IViewInject) Class.forName(object.getClass().getName() + SUFFIX).getConstructor().newInstance();
            iViewInject.inject(object);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
