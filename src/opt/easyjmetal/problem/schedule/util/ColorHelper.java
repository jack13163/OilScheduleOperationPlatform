package opt.easyjmetal.problem.schedule.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorHelper {
    public static String RGB2Hex(String rgb) {
        Matcher matcher = Pattern.compile("(?<=[\\(|\\[])[^\\)|\\]]+").matcher(rgb);
        String[] rgbArr = null;
        String hex = "";
        if (matcher.find()) {
            String tmp = matcher.group();
            rgbArr = tmp.split(",");
        }
        hex = String.format("#%02X%02X%02X", Integer.parseInt(rgbArr[0].trim()), Integer.parseInt(rgbArr[1].trim()), Integer.parseInt(rgbArr[2].trim()));
        return hex;
    }

    public static List<String> RGBArray2HexArray(String rgb) {
        Matcher matcher = Pattern.compile("(?<=[\\(|\\[])[^\\)|\\]]+").matcher(rgb);
        List<String> rgbArr = new LinkedList<>();
        while (matcher.find()) {
            String[] tmp = matcher.group().split(",");
            String hex = String.format("#%02X%02X%02X", Integer.parseInt(tmp[0].trim()), Integer.parseInt(tmp[1].trim()), Integer.parseInt(tmp[2].trim()));
            rgbArr.add(hex);
        }
        return rgbArr;
    }

    public static void main(String[] args) {
        String co = "rgb(208, 204, 255)";
        System.out.println(RGB2Hex(co));
        String co2 = "[208, 204, 245]";
        System.out.println(RGB2Hex(co2));
        String co3 = "colors = {[51,204,255]/255;\n" +
                "[255,255,0]/255;\n" +
                "[51,204,102]/255;\n" +
                "[51,255,204]/255;\n" +
                "[255,255,153]/255;\n" +
                "[219,186,119]/255;\n" +
                "\n" +
                "[204,255,255]/255;\n" +
                "[102,255,51]/255;\n" +
                "[255,204,0]/255;\n" +
                "[102,153,255]/255;\n" +
                "[153,204,51]/255;};";
        System.out.println(RGBArray2HexArray(co3));
    }
}
