package cn.aofeng.jmx.mbean;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * 使用jmx-util的示例。
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
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
