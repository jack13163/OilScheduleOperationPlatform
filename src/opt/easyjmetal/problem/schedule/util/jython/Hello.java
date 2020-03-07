package opt.easyjmetal.problem.schedule.util.jython;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Hello {
    public static void main(String[] args) {
        String[] arguments = new String[] {"python", "data/hello.py"};
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
            //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
            int re = process.waitFor();
            System.out.println(re);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}