package wang.com;

import wang.com.core.Factory;
import wang.com.demo.P;

public final class App {
    private App() {
    }

    public static void main(String[] args) throws Exception{
        Factory context = new Factory().build(App.class.getPackage().getName());
        P p = context.getBean(P.class);
        System.out.println(p.getSName());
    }
}
