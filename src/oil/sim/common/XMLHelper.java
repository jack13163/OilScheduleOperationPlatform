package oil.sim.common;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class XMLHelper {
    /**
     * 解析XML为DOM对象
     *
     * @param path
     * @return
     * @throws DocumentException
     */
    public static Document parse(String path) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(path));
        return document;
    }

}
