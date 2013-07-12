/**
 * 建立时间：2008-9-10
 */
package cn.aofeng.jmx.mbean;

/**
 * 内省过滤器.
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
public interface IntrospectionFilter {

    /**
     * 检测当前名称是否需要过滤.
     * 
     * @param name 属性名或方法名
     * @return 如果属于需要过滤的名称或者传入的name为null,返回true,否则返回false.
     */
    boolean filter(String name);
}
