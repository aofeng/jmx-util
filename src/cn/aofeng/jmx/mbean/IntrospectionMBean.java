/**
 * 建立时间：2008-9-8
 */
package cn.aofeng.jmx.mbean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;

/**
 * 对实现了*MBean接口的普通对象实例进行内省。通过反射获取对象的构造方法、方法、属性、通知等信息，
 * 将其封装成一个{@link DynamicMBean}.
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
@SuppressWarnings("rawtypes")
public class IntrospectionMBean implements DynamicMBean {

    private Class _descriptionAnno = Description.class;
    
    private Class _mbeanInterface;
    
    private Object _mbeanImpl;
    
    private MBeanInfo _mbeanInfo;
    
    /**
     * 属性过滤器.
     */
    private IntrospectionFilter _attributeFilter;
    
    /**
     * 方法过滤器.
     */
    private IntrospectionFilter _methodFilter;
    
    /**
     * 方法参数信息缓存.
     */
    private Map<Method, MBeanParameterInfo[]> _methodParameterCache 
            = new HashMap<Method, MBeanParameterInfo[]>();
            
    /**
     * 构造方法参数信息缓存.
     */
    private Map<Constructor, MBeanParameterInfo[]> _constructorParameterCache 
            = new HashMap<Constructor, MBeanParameterInfo[]>();

    /**
     * getter方法缓存.
     */
    private Map<String, Method> _getMethodCache = new HashMap<String, Method>();
        
    /**
     * setter方法缓存.
     */
    private Map<String, Method> _setMethodCache = new HashMap<String, Method>();
    
    /**
     * 构造一个mbean的内省对象.
     * 
     * @param mbeanImpl 普通mbean的实例
     * @param mbeanInterface mbean的实现接口(*MBean)
     * 
     * @exception NotCompliantMBeanException 内省时发生异常
     * 
     * @see #IntrospectionMBean(Object, Class, IntrospectionFilter, IntrospectionFilter)
     */
    public IntrospectionMBean(Object mbeanImpl, Class mbeanInterface)
            throws NotCompliantMBeanException {
        this(mbeanImpl, mbeanInterface, null, null);
    }
    
    /**
     * 构造一个mbean的内省对象.
     * 
     * @param mbeanImpl 普通mbean的实例
     * @param mbeanInterface mbean的实现接口(*MBean)
     * @param attributeFilter 属性过滤器
     * @param methodFilter 方法过滤器
     * @throws NotCompliantMBeanException 内省时发生异常
     * 
     * @see #introspect(Object, Class)
     */
    public IntrospectionMBean(Object mbeanImpl, Class mbeanInterface,
            IntrospectionFilter attributeFilter,
            IntrospectionFilter methodFilter) throws NotCompliantMBeanException {
        this._mbeanImpl = mbeanImpl;
        this._mbeanInterface = mbeanInterface;
        this._attributeFilter = attributeFilter;
        this._methodFilter = methodFilter;
        
        this._mbeanInfo = introspect(mbeanImpl, mbeanInterface);
    }
    
    
    /**
     * 内省mbean生成{@link MBeanInfo}信息.
     * 
     * @param mbeanImpl mbean实现类实例
     * @param mbeanInterface mbean接口
     * @return 内省mbean生成的{@link MBeanInfo}信息
     * @throws NotCompliantMBeanException 内省mbean发生错误时抛出此异常
     */
    MBeanInfo introspect(Object mbeanImpl, Class mbeanInterface)
            throws NotCompliantMBeanException {
        String className = mbeanInterface.getName();
        
        // 获取属性信息和方法信息
        List<MBeanAttributeInfo> attributeInfosTemp = new ArrayList<MBeanAttributeInfo>();
        List<MBeanOperationInfo> opertionInfosTemp = new ArrayList<MBeanOperationInfo>();
        Method[] methods =  mbeanInterface.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            String description = getDescription(method);
            Class[] args = method.getParameterTypes();
            
            String name = null;
            boolean isStartWithIs = methodName.startsWith("is");
            boolean isStartWithGet = methodName.startsWith("get");
            if ((isStartWithIs || isStartWithGet) && args.length == 0) {
                if (isStartWithIs) {
                    name = methodName.substring(2);
                } else if (isStartWithGet) {
                    name = methodName.substring(3);
                }
                
                if (null != _attributeFilter && _attributeFilter.filter(name)) {
                    continue;
                }
                
                MBeanAttributeInfo attributeInfo;
                try {
                    attributeInfo = new MBeanAttributeInfo(name,
                            description, method, findSetMethod(name));
                } catch (IntrospectionException e) {
                    throw new NotCompliantMBeanException(e.getMessage());
                }
                attributeInfosTemp.add(attributeInfo);
            } else { // 方法
                if (null != _methodFilter && _methodFilter.filter(methodName)) {
                    continue;
                }
                
                MBeanOperationInfo operationInfo = new MBeanOperationInfo(methodName, 
                        description, getSignature(method), method.getReturnType().getName(), 
                        MBeanOperationInfo.UNKNOWN);
                opertionInfosTemp.add(operationInfo);
            }
        }
        MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[attributeInfosTemp.size()];
        attributeInfosTemp.toArray(attributeInfos);
        MBeanOperationInfo[] opertionInfos = new MBeanOperationInfo[opertionInfosTemp.size()];
        opertionInfosTemp.toArray(opertionInfos);
        
        
        // 获取构造方法信息
        List<MBeanConstructorInfo> constructorInfosTemp = new ArrayList<MBeanConstructorInfo>();
        Constructor[] constructors = mbeanInterface.getConstructors();
        for (Constructor constructor : constructors) {
            String name = constructor.getName();
            
            MBeanConstructorInfo constructorInfo = new MBeanConstructorInfo(
                    name, "", getSignature(constructor));
            constructorInfosTemp.add(constructorInfo);
        }
        MBeanConstructorInfo[] constructorInfos = new MBeanConstructorInfo[constructorInfosTemp.size()];
        constructorInfosTemp.toArray(constructorInfos);
        
        
        // 获取通知信息 TODO 没有实现获取mbean通知的代码
        MBeanNotificationInfo[] notificationInfos = new MBeanNotificationInfo[0];
        
        MBeanInfo result = new MBeanInfo(className,
                getDescription(mbeanInterface), attributeInfos,
                constructorInfos, opertionInfos, notificationInfos);
        
        return result;
    }
    
    /**
     * 获取mbean 类的描述信息.
     * 
     * @param cl mbean Class
     * @return 描述信息
     */
    @SuppressWarnings("unchecked")
    private String getDescription(Class cl) {
        Description desc = (Description) cl.getAnnotation(_descriptionAnno);
        String description = (null == desc ? "" : desc.value());
        
        return description;
    }
    
    /**
     * 获取mbean 方法的描述信息.
     * 
     * @param method mbean 方法
     * @return 描述信息
     */
    @SuppressWarnings("unchecked")
    private String getDescription(Method method) {
        Description desc = (Description) method.getAnnotation(_descriptionAnno);
        String description = (null == desc ? "" : desc.value());
        
        return description;
    }
    
    /**
     * 获取mbean方法的参数信息.
     * 
     * @param method mbean的方法
     * @return mbean的参数信息。如果方法没有参数，返回长度为0的数组.
     */
    private MBeanParameterInfo[] getSignature(Method method) {
        if (_methodParameterCache.containsKey(method)) {
            return _methodParameterCache.get(method);
        }
        
        Class[] parameterTypes =  method.getParameterTypes();
        List<MBeanParameterInfo> temp = new ArrayList<MBeanParameterInfo>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            MBeanParameterInfo parameterInfo = new MBeanParameterInfo(
                    getParameterName(i), parameterType.getName(), "");

            temp.add(parameterInfo);
        }
        
        MBeanParameterInfo[] result = new MBeanParameterInfo[temp.size()];
        temp.toArray(result);
        
        // 加入缓存
        _methodParameterCache.put(method, result);
        
        return result;
    }
    
    private String getParameterName(int i) {
        return "param" + i;
    }
    
    /**
     * 获取mbean构造方法的参数信息.
     * 
     * @param constructor mbean的构造方法
     * @return mbean构造方法的参数信息
     */
    private MBeanParameterInfo[] getSignature(Constructor constructor) {
        if (_constructorParameterCache.containsKey(constructor)) {
            return _constructorParameterCache.get(constructor);
        }
        
        Class[] parameterTypes =  constructor.getParameterTypes();
        List<MBeanParameterInfo> temp = new ArrayList<MBeanParameterInfo>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            MBeanParameterInfo parameterInfo = new MBeanParameterInfo(
                    getParameterName(i), parameterType.getName(), "");

            temp.add(parameterInfo);
        }
        
        MBeanParameterInfo[] result = new MBeanParameterInfo[temp.size()];
        temp.toArray(result);
        
        // 加入缓存
        _constructorParameterCache.put(constructor, result);
        
        return result;
    }
    
    /*
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        if (null == attribute || "".equals(attribute.trim())) {
            return null;
        }
        
        MBeanAttributeInfo[] attInfos = _mbeanInfo.getAttributes();
        Object result = findGetMethodInvoke(attInfos, attribute);
        
        return result;
    }
    

    /*
     * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
     */
    public AttributeList getAttributes(String[] attributes) {
        if (null == attributes || attributes.length == 0) {
            return null;
        }
        
        MBeanAttributeInfo[] attInfos = _mbeanInfo.getAttributes();
        AttributeList result = new AttributeList();
        for (String attribute : attributes) {
            result.add(
                    findCreateAttribute(attInfos, attribute));
        }
        
        return result;
    }
    
    /**
     * 根据属性名称建立{@link Attribute}.
     * 
     * @param attInfos mbean已有的属性信息
     * @param attribute 需要获取值的属性名称
     * @return 如果该属性不存在get方法或is方法，属性值赋值null.
     */
    private Attribute findCreateAttribute(MBeanAttributeInfo[] attInfos, String attribute) {
        Object value = null;
        try {
            value = findGetMethodInvoke(attInfos, attribute);
        } catch (Exception e) {
            
        }
        
        return new Attribute(attribute, value);
    }
    
    /**
     * 根据属性名称查找它的getter方法，找到getter方法后调用getter方法获取属性值.
     * 
     * @param attInfos mbean已有的属性信息
     * @param attribute 需要获取值的属性名称
     * @return 属性值.
     * 
     * @exception AttributeNotFoundException 当找不到属性的getter方法时，将抛出此异常
     * @exception MBeanException 发生未知错误时，将抛出此异常
     * @exception ReflectionException 当调用属性的getter方法发生错误时，将抛出此异常
     */
    private Object findGetMethodInvoke(MBeanAttributeInfo[] attInfos, String attribute) 
            throws AttributeNotFoundException, MBeanException, ReflectionException{
        
        Object value = null;
        
        for (MBeanAttributeInfo beanAttributeInfo : attInfos) {
            if (attribute.equals(beanAttributeInfo.getName())) {
                try {
                    Method method = findGetMethod(attribute);
                    if (null == method) {
                        throw new AttributeNotFoundException("Attribute " + attribute + "has no getter method");
                    }
                    value = method.invoke(_mbeanImpl, (Object[]) null);
                } catch (IllegalAccessException e) {
                    throw new ReflectionException(e);
                } catch (InvocationTargetException e) {
                    throw new ReflectionException(e);
                } catch (Exception e) {
                    throw new MBeanException(e);
                }
                
                break;
            }
        }
        
        if (null == value) {
            throw new AttributeNotFoundException("Attribute " + attribute + "not found");
        }
        
        return value;
    }
    
    /**
     * 根据属性名称查找它的getter方法.
     * 
     * @param attribute 属性名称
     * @return 如果属性名称为null，返回null；如果找不到属性的getter方法，返回null.
     */
    private Method findGetMethod(String attribute) {
        if (_getMethodCache.containsKey(attribute)) {
            return _getMethodCache.get(attribute);
        }
        
        Method[] methods = this._mbeanInterface.getMethods();
        for (Method method : methods) {
            String firstUpperAttribute = firstCharToUpper(attribute);
            String isMetodName = "is" + firstUpperAttribute;
            String getMethodName = "get" + firstUpperAttribute;
            
            boolean isReturnNotVoid = !method.getReturnType().equals(void.class);
            int parametersLen = method.getParameterTypes().length;
            if ((method.getName().equals(isMetodName) || method.getName().equals(getMethodName)) 
                    && isReturnNotVoid && parametersLen == 0) {
                // 加入缓存
                _getMethodCache.put(attribute, method);
                
                return method;
            }
        }
        
        return null;
    }
    
    /**
     * 将字符串的第一个字母变成大写.
     */
    private String firstCharToUpper(String str) {
        StringBuilder builder = new StringBuilder(str);
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        
        return builder.toString();
    }

    /*
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    public MBeanInfo getMBeanInfo() {
        return _mbeanInfo;
    }

    /*
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        if (null == actionName) {
            return null;
        }
        
        Method[] methods = this._mbeanInterface.getMethods();
        for (Method method : methods) {
            if (actionName.equals(method.getName())) {
                Object result = null;
                try {
                    result = method.invoke(_mbeanImpl, params);
                } catch (IllegalAccessException e) {
                    throw new ReflectionException(e);
                } catch (InvocationTargetException e) {
                    throw new ReflectionException(e);
                } catch(Exception e) {
                    throw new MBeanException(e);
                }
                
                return result;
            }
        }
        
        return null;
    }

    /*
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        Method setMethod = findSetMethod(attribute.getName());

        if (null != setMethod) {
            try {
                setMethod.invoke(_mbeanImpl, new Object[]{attribute.getValue()});
            } catch (Exception e) {
                throw new ReflectionException(e, "Fail to invoke setter method");
            }
        } else {
            throw new AttributeNotFoundException("Attribute " + attribute.getName() + " not found");
        }
    }
    
    /**
     * 根据属性名称查找其setter方法.
     * 
     * @param attribute 属性名称
     * @return 如果属性名称为null，返回null；如果找不到属性的setter方法，返回null.
     */
    private Method findSetMethod(String attribute) {
        if (_setMethodCache.containsKey(attribute)) {
            return _setMethodCache.get(attribute);
        }
        
        Method[] methods = this._mbeanInterface.getMethods();
        for (Method method : methods) {
            String firstUpperAttribute = firstCharToUpper(attribute);
            String setMetodName = "set" + firstUpperAttribute;
            
            if (setMetodName.equals(method.getName()) 
                    && method.getParameterTypes().length == 1) {
                // 加入缓存
                _setMethodCache.put(attribute, method);
                
                return method;
            }
        }
        
        return null;
    }

    /*
     * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
     */
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList result = new AttributeList();
        
        for (Object attributeObj : attributes) {
            Attribute attribute = (Attribute) attributeObj;
            
            Method setMethod = findSetMethod(attribute.getName());
            if (null != setMethod) {
                try {
                    setMethod.invoke(_mbeanImpl, new Object[]{attribute.getValue()});
                    result.add(attribute);
                } catch (Exception e) {
                    
                }
            }
        }
        
        return result;
    }

    public Object getImplement() {
        return _mbeanImpl;
    }

}
