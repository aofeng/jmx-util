package cn.aofeng.jmx.mbean;

import java.util.HashMap;
import java.util.Map;

/**
 * MBean对象。
 * 
 * @author 聂勇 <a href="mailto:aofengblog@163.com">aofengblog@163.com</a>
 */
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
