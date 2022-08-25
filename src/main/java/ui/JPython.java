package ui;

import java.io.IOException;

public class JPython {
    public static void Graphic(double time1, double time2, double time3, String str) throws IOException {
        //通过原生方式调用，解决python文件引入第三方库的问题
        //第一个参数默认是python3,第二个参数python脚本路径，第三和第四个参数是python要接收的参数
        String[] argg = new String[] { "python3", "src/ui/test2.py", String.valueOf(time1), String.valueOf(time2), String.valueOf(time3), str};

        Process pr = Runtime.getRuntime().exec(argg);
    }
}
