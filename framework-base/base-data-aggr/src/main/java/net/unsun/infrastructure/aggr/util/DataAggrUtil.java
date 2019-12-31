package net.unsun.infrastructure.aggr.util;


import com.google.common.collect.Maps;
import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * aggregation util
 *
 * @author loken
 * @since 2019-12-30
 */
public class DataAggrUtil {


    public static <T> List<T> copy(List<T> masterDataList, String masterIdFiledName, Collection<?> slaveDataList, String slaveIdFiledName, String slaveFiledName) {
        List<T> result = masterDataList.stream().map(masterData -> slaveDataList.stream().filter(slaveData -> {
            Class masterClassType = masterData.getClass();
            Class slaveClassType = slaveData.getClass();
            try {
                String masterValue = invokeMethodAndReturn(masterClassType, masterData, masterIdFiledName);
                String slaveValue = invokeMethodAndReturn(slaveClassType, slaveData, slaveIdFiledName);
                return Objects.equals(masterValue, slaveValue);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;

        }).findFirst().map(slaveData -> {
            T result2 = (T) dynamicAddCloumn(masterData, slaveData, slaveFiledName);
            return result2;
        }).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
        return result;
    }

    /**
     * 执行方法并返回值
     *
     * @param clazz       类
     * @param t           执行的对戏
     * @param idFiledName 字段名称
     * @param <T>
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private static <T> String invokeMethodAndReturn(Class clazz, T t, String idFiledName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (t instanceof Map) {
            if (((Map) t).containsKey(idFiledName)) {
                return ((Map) t).get(idFiledName) + "";
            }
            throw new RuntimeException("idFiledName is not exist");
        } else {
            return clazz.getMethod("get" + upperHeadChar(idFiledName)).invoke(t) + "";
        }
    }

    /**
     * 首字母大写
     *
     * @param in
     * @return
     */
    public static String upperHeadChar(String in) {
        String head = in.substring(0, 1);
        String out = head.toUpperCase() + in.substring(1, in.length());
        return out;
    }

    /**
     * 首字母小写
     *
     * @param in
     * @return
     */
    public static String lowerHeadChar(String in) {
        String head = in.substring(0, 1);
        String out = head.toLowerCase() + in.substring(1, in.length());
        return out;
    }

    /**
     * 判断是否为空
     *
     *
     * @param obj
     * @return
     */
    private static boolean isEmpty(Object obj) {
        if (null == obj && "".equals(obj) && "null".equals(obj)) {
            return true;
        }
        return false;
    }

    /**
     * 动态添加字段
     *
     * @param dest     主对象
     * @param t        子对象
     * @param fileName 子对象放入主对象的名称
     * @param <T>
     * @return
     */
    public static <T> Object dynamicAddCloumn(Object dest, T t, String fileName) {
        if (dest instanceof Map) {
            if (!isEmpty(fileName)) {
                if (((Map) dest).containsKey(fileName)) {
                    throw new RuntimeException("fileName is exist");
                }
            }
            ((Map) dest).put(isEmpty(fileName) ? lowerHeadChar(t.getClass().getSimpleName()) : fileName, t);
            return dest;
        }
        // get property map
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(dest);
        Map<String, Class> propertyMap = Maps.newHashMap();
        for (PropertyDescriptor d : descriptors) {
            if (!"class".equalsIgnoreCase(d.getName())) {
                propertyMap.put(d.getName(), d.getPropertyType());
            }
        }
        String finalFileName = getFiledName(t, fileName, propertyMap);
        // add extra properties
        propertyMap.put(finalFileName, t.getClass());
        // new dynamic bean
        DynamicBean dynamicBean = new DynamicBean(dest.getClass(), propertyMap);
        // add old value
        propertyMap.forEach((k, v) -> {
            try {
                dynamicBean.setValue(k, propertyUtilsBean.getNestedProperty(dest, k));
            } catch (Exception e) {
            }
        });
        dynamicBean.setValue(finalFileName, t);
        Object target = dynamicBean.getTarget();
        return target;
    }

    /**
     * 获取动态添加类的名称
     *
     * @param t
     * @param fileName
     * @param propertyMap
     * @param <T>
     * @return
     */
    private static <T> String getFiledName(T t, String fileName, Map<String, Class> propertyMap) {
        String finalFileName;
        if (isEmpty(fileName)) {
            finalFileName = lowerHeadChar(t.getClass().getSimpleName());
        } else {
            if (propertyMap.containsKey(fileName)) {
                throw new RuntimeException("fileName is exist");
            }
            finalFileName = fileName;
        }
        return finalFileName;
    }

    /**
     * 动态对象类
     */
    public static class DynamicBean {
        /**
         * 目标对象
         */
        private Object target;

        /**
         * 属性集合
         */
        private BeanMap beanMap;

        public DynamicBean(Class superclass, Map<String, Class> propertyMap) {
            this.target = generateBean(superclass, propertyMap);
            this.beanMap = BeanMap.create(this.target);
        }


        /**
         * bean 添加属性和值
         *
         * @param property
         * @param value
         */
        public void setValue(String property, Object value) {
            beanMap.put(property, value);
        }

        /**
         * 获取属性值
         *
         * @param property
         * @return
         */
        public Object getValue(String property) {
            return beanMap.get(property);
        }

        /**
         * 获取对象
         *
         * @return
         */
        public Object getTarget() {
            return this.target;
        }


        /**
         * 根据属性生成对象
         *
         * @param superclass
         * @param propertyMap
         * @return
         */
        private Object generateBean(Class superclass, Map<String, Class> propertyMap) {
            BeanGenerator generator = new BeanGenerator();
            if (null != superclass) {
                generator.setSuperclass(superclass);
            }
            BeanGenerator.addProperties(generator, propertyMap);
            return generator.create();
        }
    }


}
