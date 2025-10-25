package cor.servlet.v3;


import java.util.ArrayList;
import java.util.List;

//双向过滤增强
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
        chain.doFilter(request, response, chain);
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
//chain:当前过滤器链条，用于传递到下一个过滤器
interface Filter {
    boolean doFilter(Request request, Response response, FilterChain chain);
}
//请求->下一个过滤器->响应
class HTMLFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response, FilterChain chain) {
        request.str = request.str.replaceAll("<", "[").replaceAll(">", "]") + "HTMLFilter()";
        chain.doFilter(request, response, chain);
        //在响应内容后，添加过滤器标识
        response.str += "--HTMLFilter()";
        return true;//允许继续执行后续过滤器
    }
}

class SensitiveFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response, FilterChain chain) {
        request.str = request.str.replaceAll("996", "955") + "SensitiveFilter()";
        chain.doFilter(request, response, chain);
        response.str += "--SensitiveFilter()";
        return true;
    }
}

//过滤器链，管理多个过滤器的执行顺序
class FilterChain implements Filter {
    int index = 0;//记录当前执行到的过滤器索引
    List<Filter> filters = new ArrayList<>();//存储过滤器列表

    public FilterChain add(Filter f) {
        filters.add(f);
        return this;
    }

    public boolean doFilter(Request request, Response response, FilterChain chain) {
        if (index == filters.size()) {
            return false;
        }
        Filter f = filters.get(index);
        index++;
        return f.doFilter(request, response, chain);
    }

}