/**
 * 建立时间：2008-9-10
 */
package cn.aofeng.jmx.mbean;

/**
 * 内省方法过滤器.
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
public class MethodFilter extends AbstractFilter {

    public MethodFilter() {
        filterNames.add("registerSelf");
        filterNames.add("unRegisterSelf");
    }

}
