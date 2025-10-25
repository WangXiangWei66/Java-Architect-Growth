package observer_v9;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        Button b = new Button();
        b.addActionListener(new MyActionListener());
        b.addActionListener(new MyActionListener2());
        b.buttonPressed();
    }
}
//负责维护监听器并触发事件
class Button {
    //存储所有注册的监听器
    private List<ActionListener> actionListeners = new ArrayList<ActionListener>();

    public void buttonPressed() {
        ActionEvent e = new ActionEvent(System.currentTimeMillis(), this);
        for (int i = 0; i < actionListeners.size(); i++) {
            ActionListener l = actionListeners.get(i);
            l.actionPerformed(e);
        }
    }
    //用于注册新的观察者
    public void addActionListener(ActionListener l) {
        actionListeners.add(l);
    }
}
//抽象观察者，动作监听器接口，定义事件处理方法
interface ActionListener {
    public void actionPerformed(ActionEvent e);
}

class MyActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        System.out.println("button pressed!");
    }
}

class MyActionListener2 implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        System.out.println("button pressed 2!");
    }
}
//事件对象：封装事件相关信息
class ActionEvent {
    long when;
    Object source;

    public ActionEvent(long when, Object source) {
        super();
        this.when = when;
        this.source = source;
    }

    public long getWhen() {
        return when;
    }

    public Object getSource() {
        return source;
    }
}