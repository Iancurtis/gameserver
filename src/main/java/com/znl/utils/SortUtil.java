package com.znl.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Administrator on 2015/11/18.
 */
public class SortUtil {

    // 按任意属性进行排序
    static class AnyProperComparator implements Comparator<Object> {

        private String properName;// 根据此关键字属性排序

        private boolean flag;// 为true的时候是正序，为false的时候是倒序

        public AnyProperComparator(String properName, boolean flag) {
            super();
            this.properName = properName;
            this.flag = flag;
        }

        public void setProperName(String properName) {
            this.properName = properName;
        }

        public String getProperName() {
            return properName;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @SuppressWarnings("unchecked")
        public int compare(Object r1, Object r2) {
            Class c = r1.getClass();
            double result = 0;
            try {
//				Method field =
//				Integer i =0;
//				String classType = field.getType().getSimpleName();
                Method method = c.getDeclaredMethod(properName);
                method.setAccessible(true);
                Object  classType= method.invoke(r1).getClass().getSimpleName();
                // 这里仅根据方法的返回值类型的名称来判定，比较方便
                if ("String".equals(classType)) {
                    method = c.getMethod(properName, new Class[] {});
                    if (flag) {
                        result = ((String) method.invoke(r1))
                                .compareTo((String) method.invoke(r2));
                    } else {
                        result = ((String) method.invoke(r2))
                                .compareTo((String) method.invoke(r1));
                    }
                } else if ("Long".equals(classType)
                        || "long".equals(classType)) {
                    method = c.getMethod(properName, new Class[] {});
                    if (flag) {
                        result = ((Long) method.invoke(r1))
                                - ((Long) method.invoke(r2));
                    } else {
                        result = ((Long) method.invoke(r2))
                                - ((Long) method.invoke(r1));
                    }
                } else if ("Integer".equals(classType)
                        || "int".equals(classType)) {
                    method = c.getMethod(properName, new Class[] {});
                    if (flag) {
                        result = ((Integer) method.invoke(r1))
                                - ((Integer) method.invoke(r2));
                    } else {
                        result = ((Integer) method.invoke(r2))
                                - ((Integer) method.invoke(r1));
                    }
                } else if ("Double".equals(classType)
                        || "double".equals(classType)) {
                    method = c.getMethod(properName, new Class[] {});
                    if (flag) {
                        result = ((Double) method.invoke(r1))
                                - ((Double) method.invoke(r2));
                    } else {
                        result = ((Double) method.invoke(r2))
                                - ((Double) method.invoke(r1));
                    }
                } else if ("Float".equals(classType)
                        || "float".equals(classType)) {
                    method = c.getMethod(properName, new Class[] {});
                    if (flag) {
                        result = ((Float) method.invoke(r1))
                                - ((Float) method.invoke(r2));
                    } else {
                        result = ((Float) method.invoke(r2))
                                - ((Float) method.invoke(r1));
                    }
                } else {
                    System.out.println("属性排序只支持数据类型和String类型，其它类型暂不支持。");
                    result = -100;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            // 确定返回值
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            }
            return 0;
        }

    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> anyProperSort(List<T> list, String properName,boolean flag) {
        AnyProperComparator comparator = new AnyProperComparator(properName,
                flag);
        Collections.sort(list, comparator);
        return list;
    }


}
