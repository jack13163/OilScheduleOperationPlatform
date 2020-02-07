package opt.rl4j;

public class CMDHelper {
    public static void startHttpServer() {
        try {
            Process process = Runtime.getRuntime().exec("cmd /c python gym_http_server.py");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
