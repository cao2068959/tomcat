package chy.frame.core;

import org.apache.catalina.*;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.Servlet;

public class ChyTomcat {

    //默认端口8080
    private int port = 8080;

    //指定web.xml的位置
    private String webxmlpath;

    private Tomcat tomcat;

    private StandardContext context;
    //静态资源文件的context 如果没有设置 assetpath 则和上面context 相同
    private StandardContext assetContext;
    //虚拟目录
    private String contextPath = "/";

    //项目挂载目录,改变路径将会影响静态资源的访问位置
    private String contextDir = null;

    private String projectPath = System.getProperty("user.dir");

    /**
     * 构造 2个参数都可以默认为null;
     * @param webxmlpath
     * @param assetpath
     */
    public ChyTomcat(String webxmlpath, String assetpath) {
        this(-1,webxmlpath,assetpath);
    }

    public ChyTomcat(int port, String webxmlpath, String contextDir) {
        if(port>0){
            this.port = port;
        }
        this.webxmlpath = pathHanlde(webxmlpath);
        if(contextDir!=null){
            this.contextDir =  pathHanlde(contextDir);
        }else{
            this.contextDir =  projectPath;
        }


        //初始化tomcat
        init();
    }


    /**
     * 路径处理,如果是classpath:xxx 开头就放在类路径下面
     * @return
     */
    private String pathHanlde(String path){
        if(path == null){
            return null;
        }

        boolean isclasspath = path.startsWith("classpath:");
        if(!isclasspath){
            return path;
        }
        String[] split = path.split(":");
        if(split.length != 2){
            return path;
        }
        String result = projectPath + "/" + split[1];
        return result;
    }

    private void init(){
        tomcat = new Tomcat();
        //Server server = tomcat.getServer();

        //看源码发现,他只能设置一个service,直接拿默认的
        Service service = tomcat.getService();

        //创建连接器,并且添加对应的连接器,同时连接器指定端口
        Connector connector = new Connector();
        connector.setPort(port);
        service.addConnector(connector);

        //创建一个引擎,放入service中
        Engine engine = new StandardEngine();
        service.setContainer(engine);
        engine.setDefaultHost("localhost");
        engine.setName("myTomcat");

        //添加host
        Host host = new StandardHost();
        engine.addChild(host);
        host.setName("localhost");
        host.setAppBase("webapps");

        System.out.println("当前项目路径 : "+contextDir);
        //在对应的host下面创建一个 context 并制定他的工作路径,会加载该目录下的所有文件,或者静态文件,并且会读取该目录下WEB-INF/web.xml文件
        context = (StandardContext) tomcat.addContext(host, contextPath, contextDir);
        //加载自定义的 web.xml文件
        loadWebxml(context);
        //创建一个context 用来指向static文件
        creatAssetContext(host);

    }


    /**
     * 加载xml文件
     */
    private void loadWebxml(StandardContext context){
        //初始化ContextConfig配置
        context.addLifecycleListener(new ContextConfig());
        if(webxmlpath!=null){
            //加载指定位置的web.xml
            context.getServletContext().setAttribute("org.apache.catalina.deploy.alt_dd",webxmlpath);
        }
    }



    private void creatAssetContext(Host host){
        //开启默认的servlet 用来处理静态请求
        Wrapper defaultServlet = new StandardWrapper();
        defaultServlet.setServlet(new DefaultServlet());
        defaultServlet.setName("default");
        context.addChild(defaultServlet);
        defaultServlet.addMapping("/");
    }


    /**
     * 添加一个servlet在容器中
     * @param servletObj
     * @param mapping
     */
    public void addServlet(Servlet servletObj, String mapping){
        Wrapper servlet = new StandardWrapper();
        servlet.setServlet(servletObj);
        servlet.setName(servletObj.getClass().getName());
        context.addChild(servlet);
        servlet.addMapping(mapping);
    }





    //开始tomcat服务
    public void start(){

        try {
            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }


    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

}
