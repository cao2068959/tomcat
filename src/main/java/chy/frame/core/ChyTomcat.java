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

    //Ĭ�϶˿�8080
    private int port = 8080;

    //ָ��web.xml��λ��
    private String webxmlpath;

    private Tomcat tomcat;

    private StandardContext context;
    //��̬��Դ�ļ���context ���û������ assetpath �������context ��ͬ
    private StandardContext assetContext;
    //����Ŀ¼
    private String contextPath = "/";

    //��Ŀ����Ŀ¼,�ı�·������Ӱ�쾲̬��Դ�ķ���λ��
    private String contextDir = null;

    private String projectPath = System.getProperty("user.dir");

    /**
     * ���� 2������������Ĭ��Ϊnull;
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


        //��ʼ��tomcat
        init();
    }


    /**
     * ·������,�����classpath:xxx ��ͷ�ͷ�����·������
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

        //��Դ�뷢��,��ֻ������һ��service,ֱ����Ĭ�ϵ�
        Service service = tomcat.getService();

        //����������,������Ӷ�Ӧ��������,ͬʱ������ָ���˿�
        Connector connector = new Connector();
        connector.setPort(port);
        service.addConnector(connector);

        //����һ������,����service��
        Engine engine = new StandardEngine();
        service.setContainer(engine);
        engine.setDefaultHost("localhost");
        engine.setName("myTomcat");

        //���host
        Host host = new StandardHost();
        engine.addChild(host);
        host.setName("localhost");
        host.setAppBase("webapps");

        System.out.println("��ǰ��Ŀ·�� : "+contextDir);
        //�ڶ�Ӧ��host���洴��һ�� context ���ƶ����Ĺ���·��,����ظ�Ŀ¼�µ������ļ�,���߾�̬�ļ�,���һ��ȡ��Ŀ¼��WEB-INF/web.xml�ļ�
        context = (StandardContext) tomcat.addContext(host, contextPath, contextDir);
        //�����Զ���� web.xml�ļ�
        loadWebxml(context);
        //����һ��context ����ָ��static�ļ�
        creatAssetContext(host);

    }


    /**
     * ����xml�ļ�
     */
    private void loadWebxml(StandardContext context){
        //��ʼ��ContextConfig����
        context.addLifecycleListener(new ContextConfig());
        if(webxmlpath!=null){
            //����ָ��λ�õ�web.xml
            context.getServletContext().setAttribute("org.apache.catalina.deploy.alt_dd",webxmlpath);
        }
    }



    private void creatAssetContext(Host host){
        //����Ĭ�ϵ�servlet ��������̬����
        Wrapper defaultServlet = new StandardWrapper();
        defaultServlet.setServlet(new DefaultServlet());
        defaultServlet.setName("default");
        context.addChild(defaultServlet);
        defaultServlet.addMapping("/");
    }


    /**
     * ���һ��servlet��������
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





    //��ʼtomcat����
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
