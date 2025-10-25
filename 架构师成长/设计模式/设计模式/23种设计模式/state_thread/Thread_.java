package state_thread;

public class Thread_ {

    ThreadState_ state;
    //委托当前的状态来执行下面的方法
    void move(Action input) {
        state.move(input);
    }

    void run() {
        state.run();
    }
}
