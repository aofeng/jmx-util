/**
 * 建立时间：2008-9-11
 */
package cn.aofeng.jmx.mbean;

import java.util.ArrayList;
import java.util.List;

/**
 * 内省抽象过滤器.
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
public abstract class AbstractFilter implements IntrospectionFilter {

    protected List<String> filterNames = new ArrayList<String>();
    
    public boolean filter(String name) {
        if (null == name) {
            return true;
        }
        
        for (String filterName : filterNames) {
            if (filterName.equals(name)) {
                return true;
            }
        }
        
        return false;
    }

}
