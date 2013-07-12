/**
 * 建立时间：2008-9-11
 */
package cn.aofeng.jmx.mbean;

/**
 * 内省属性过滤器.
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
public class AttributeFilter extends AbstractFilter {

    public AttributeFilter() {
        filterNames.add("Name");
        filterNames.add("ObjectName");
        filterNames.add("Type");
    }
}
