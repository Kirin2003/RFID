package ui;

import java.io.*;

public class JavaPythonTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String fileurl="D:/route/高程点-350.csv";
        String point="101.89405013,30.02698289,3301;102.14444407,29.56780298,3301;43.1,20.1,6301,1";

        //通过原生方式调用，解决python文件引入第三方库的问题
        //第一个参数默认是python,第二个参数python脚本路径，第三和第四个参数是python要接收的参数
        String[] argg = new String[] { "python3", "/home/hxq/Desktop/2021-2022winter/RFID/CIP/src/ECIP/optimize.py"};

        Process pr = Runtime.getRuntime().exec(argg);

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream(),"utf-8"));
        BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream(), "utf-8"));//获取错误信息的字符输入流对象
        String line;
        String errorMessage;
        String OutputResult = "";
        String errorResult = "";
        //接收返回结果
        while ((line = in.readLine()) != null) {
            System.out.println("进入循环");
            OutputResult += line;
            pr.waitFor();
        }
        while((errorMessage = error.readLine()) != null) {
            errorResult += errorMessage;
        }
        System.out.println(OutputResult);
        System.out.println(errorResult);
        in.close();
        pr.waitFor();
    }

}
