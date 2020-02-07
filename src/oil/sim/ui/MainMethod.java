package oil.sim.ui;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.File;
import java.io.FileInputStream;

public class MainMethod {
    public static MainFrame frame = null;

    public static void main(String[] args) {

        // 指定log4j2.xml文件的位置
        ConfigurationSource source;
        String relativePath = "log4j2.xml";
        File log4jFile = new File(relativePath);
        try {
            if (log4jFile.exists()) {
                source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.initialize(null, source);
            } else {
                System.out.println("loginit failed");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // 初始化GUI界面，控制权交给用户
        frame = new MainFrame("约束多目标优化");
    }
}
