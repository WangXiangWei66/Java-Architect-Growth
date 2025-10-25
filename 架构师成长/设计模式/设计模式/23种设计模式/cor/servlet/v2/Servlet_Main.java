package cor.servlet.v2;


import java.util.ArrayList;
import java.util.List;
//新增了对相应对象的处理，让过滤器不仅能修改请求数据，还能同步修改响应数据
public class Servlet_Main {

    public static void main(String[] args) {
        Request request = new Request();
        request.str = "大家好:),<script>,欢迎访问 mashibing.com,大家都是996";
        //创建相应对象
        Response response = new Response();
        response.str = "response ";//响应初始值
        //创建过滤器链
        FilterChain chain = new FilterChain();
        chain.add(new HTMLFilter()).add(new SensitiveFilter());
        chain.doFilter(request, response);
        System.out.println(request.str);
        System.out.println(response.str);
    }
}


class Request {
    String str;
}

class Response {
    String str;
}

//true：允许后续过滤
//false：中断过滤
interface Filter {
    boolean doFilter(Request request, Response response);
}

class HTMLFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response) {
        request.str = request.str.replaceAll("<", "[").replaceAll(">", "]");
        //在响应内容后，添加过滤器标识
        response.str += "--HTMLFilter()";
        return true;//允许继续执行后续过滤器
    }
}

class SensitiveFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response) {
        request.str = request.str.replaceAll("996", "955");
        response.str += "--SensitiveFilter()";
        return true;
    }
}

//过滤器链，管理多个过滤器的执行顺序
class FilterChain implements Filter {

    List<Filter> filters = new ArrayList<>();

    public FilterChain add(Filter f) {
        filters.add(f);
        return this;
    }

    public boolean doFilter(Request request, Response response) {
        for (Filter f : filters) {
            f.doFilter(request, response);
        }
        return true;
    }
}