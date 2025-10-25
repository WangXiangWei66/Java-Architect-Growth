package cor.servlet.v1;

//过滤器机制，实现了对请求数据的链式处理
import java.util.ArrayList;
import java.util.List;

public class Servlet_Main {

    public static void main(String[] args) {
        Request request = new Request();
        request.str = "大家好:),<script>,欢迎访问 mashibing.com,大家都是996";
        //创建相应对象
        Response response = new Response();
        response.str = " ";
        //创建过滤器链
        FilterChain chain = new FilterChain();
        chain.add(new HTMLFilter()).add(new SensitiveFilter());
        chain.doFilter(request, response);
        System.out.println(request.str);
    }
}


class Request {
    String str;
}

class Response {
    String str;
}
//定义过滤方法规范
interface Filter {
    boolean doFilter(Request request, Response response);
}

class HTMLFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response) {
        request.str = request.str.replaceAll("<", "[").replaceAll(">", "]");
        return true;//允许继续执行后续过滤器
    }
}

class SensitiveFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response) {
        request.str = request.str.replaceAll("996", "955");
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