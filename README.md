# jmx-util

# 功能

* 封装普通的Java对象，使之成为符合JMX规范的MBean。
* 根据指定的接口，暴露方法和属性。
* 可自定义过滤器，过滤指定的方法和属性。

# 类关系图

![类关系图](./doc/guide/images/jmx-util-class.svg)

* **IntrospectionMBean**。实现了`javax.management.DynamicMBean`，负责将普通Java对象封装成MBean，它会调用`IntrospectionFilter`的实现类，过滤属性和方法。
* **IntrospectionFilter**。过滤器接口定义，默认实现类有`AbstractFilter`、`AttributeFilter`、`MethodFilter`。可根据自已的需要继承AbstracFilter类或IntrospectionFilter接口实现属性和方法过滤器。
* **Description**。描述注解，可应用于类和方法。

# 使用说明

1、引入依赖。

```xml
<dependency>
    <groupId>io.github.aofeng</groupId>
    <artifactId>jmx-util</artifactId>
    <version>1.1.0</version>
</dependency>
```

2、编写一个POJO类。[源代码](https://github.com/aofeng/jmx-util/blob/master/example/cn/aofeng/jmx/mbean/People.java)

```java
@Description("用户")
public class People {

    private String userName;
    
    private int age;
    
    private char sex;
    
    private Map<String, Object> ex = new HashMap<String, Object>();

    @Description("是否成年")
    public boolean isAdult() {
        return (age >= 18);
    }
    
    @Description("获取姓名")
    public String getUserName() {
        return userName;
    }

    @Description("设置姓名")
    public void setUserName(String name) {
        this.userName = name;
    }

    @Description("获取年龄")
    public int getAge() {
        return age;
    }

    @Description("设置年龄")
    public void setAge(int age) {
        this.age = age;
    }

    @Description("获取性别")
    public char getSex() {
        return sex;
    }

    @Description("设置性别")
    public void setSex(char sex) {
        this.sex = sex;
    }

    @Description("获取其他的信息")
    public Map<String, Object> getEx() {
        return ex;
    }

    @Description("设置其他的信息")
    public void setEx(Map<String, Object> ex) {
        this.ex = ex;
    }
}
```

3、编写注册MBean对象的代码。[完整的源代码](https://github.com/aofeng/jmx-util/blob/master/example/cn/aofeng/jmx/mbean/HelloJmx.java)

```Java
public class HelloJmx {

    public static void main(String[] args) throws Exception {
        People zhs = new People();
        zhs.setUserName("张三");
        zhs.setAge(16);
        zhs.setSex('M');

        People ls = new People();
        ls.setUserName("李四");
        ls.setAge(18);
        ls.setSex('M');

        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        // 张三使用默认的方法和属性过滤器
        ObjectName zhsObjectName = new ObjectName("cn.aofeng.jmx", "name", "张三");
        Object zhsMBean = new IntrospectionMBean(zhs, People.class);
        mbeanServer.registerMBean(zhsMBean, zhsObjectName);

        // 李四使用自定义的方法和属性过滤器
        IntrospectionFilter attributeFilter = new CustomAttributeFilter();
        IntrospectionFilter methodFilter  = new CustomMethodFilter();
        ObjectName lsObjectName = new ObjectName("cn.aofeng.jmx", "name", "李四");
        Object lsMBean = new IntrospectionMBean(ls, People.class, attributeFilter, methodFilter);
        mbeanServer.registerMBean(lsMBean, lsObjectName);

        Thread.sleep(1000 * 1000);
    }

    public static class CustomAttributeFilter extends AttributeFilter {

        public CustomAttributeFilter() {
            super();
            super.filterNames.add("Class");
        }
    }

    public static class CustomMethodFilter extends MethodFilter {

        public CustomMethodFilter() {
            super();
            super.filterNames.add("equals");
            super.filterNames.add("hashCode");
            super.filterNames.add("toString");
            super.filterNames.add("wait");
            super.filterNames.add("notify");
            super.filterNames.add("notifyAll");
        }
    }
}
```

4、运行HelloJmx。

5、打开JConsole，选择HelloJmx的进程。

6、点击`连接`按钮，选择`MBean`选项卡。

![JMX-MBean信息显示界](./doc/guide/images/jconsole.webp)

# 参考资料

* [Java Management Extensions (JMX) Technology](http://www.oracle.com/technetwork/java/javase/tech/javamanagement-140525.html)
* [JMX管理Tomcat/Resin](http://aofengblog.blog.163.com/blog/static/6317021200871711013857/)
