package opt.easyjmetal.problem.schedule;

import opt.easyjmetal.problem.schedule.models.DSObject;
import opt.easyjmetal.problem.schedule.models.FPObject;
import opt.easyjmetal.problem.schedule.models.PipeObject;
import opt.easyjmetal.problem.schedule.models.TankObject;
import opt.easyjmetal.problem.schedule.util.XMLHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.*;

public class Config implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Logger logger = LogManager.getLogger(Config.class.getName());

    public double RT;
    public double VolMin;
    public int NumOfDivide;
    public int Precision;
    public int HighOilDS;
    public static double HotingSpeed;
    public static int HotTank;
    public static boolean Stop;
    public double MaintenanceTime;
    public static boolean ShowDetail = false;
    public static boolean ShowHardCostChart = false;
    public static boolean ShowEachStep = false;
    public Map<String, Double> referenceCost;// 参考成本

    // 停运次数
    public static int stopTimes = 10;

    private List<TankObject> tanks;
    private List<DSObject> dss;
    private List<PipeObject> pipes;

    private static Config _instance;

    public List<TankObject> getTanks() {
        return tanks;
    }

    public List<DSObject> getDSs() {
        return dss;
    }

    public void setDSs(List<DSObject> dss) {
        this.dss = dss;
    }

    public List<PipeObject> getPipes() {
        return pipes;
    }

    private Config(List<DSObject> dss, List<TankObject> tanks, List<PipeObject> pipes) {
        this.tanks = tanks;
        this.dss = dss;
        this.pipes = pipes;
    }

    /**
     * 加载配置
     *
     * @return
     */
    public Config loadConfig() {
        getConfigFromXml();
        return _instance;
    }

    /**
     * 从xml文件中读取配置信息
     *
     * @return
     * @throws DocumentException
     */
    public void getConfigFromXml() {

        try {
            Element root = XMLHelper.parse("config.xml").getRootElement();

            List<Element> elements = root.elements();

            for (Element element : elements) {
                if (element.getName() == "rt") {
                    RT = Double.parseDouble(element.getText());
                } else if (element.getName() == "min-volume") {
                    VolMin = Double.parseDouble(element.getText());
                } else if (element.getName() == "num-divide") {
                    NumOfDivide = Integer.parseInt(element.getText());
                } else if (element.getName() == "precision") {
                    Precision = Integer.parseInt(element.getText());
                } else if (element.getName() == "hoting-speed") {
                    HotingSpeed = Double.parseDouble(element.getText());
                } else if (element.getName() == "high-oil-ds") {
                    HighOilDS = Integer.parseInt(element.getText());
                } else if (element.getName() == "hot-tank") {
                    HotTank = Integer.parseInt(element.getText());
                } else if (element.getName() == "auto-stop") {
                    Stop = Integer.parseInt(element.getText()) == 0 ? false : true;
                } else if (element.getName() == "maintenance-time") {
                    MaintenanceTime = Double.parseDouble(element.getText());
                } else if (element.getName() == "reference-cost") {
                    referenceCost = new HashMap<String, Double>();
                    String[] costsStr = element.getText().split(",");
                    for (int i = 0; i < costsStr.length; i++) {
                        String coString = costsStr[i];
                        int indexOfEqual = coString.indexOf("=");
                        String costName = coString.substring(0, indexOfEqual);
                        String tmp = coString.substring(indexOfEqual + 1, coString.length());
                        double costValue = Double.parseDouble(tmp);
                        referenceCost.put(costName, costValue);
                    }
                } else if (element.getName() == "tanks") {
                    for (Element tank : element.elements()) {
                        int tank_no = Integer.parseInt(tank.attributeValue("id"));
                        TankObject tankObject = tanks.get(tank_no - 1);

                        for (Element ele : tank.elements()) {
                            if (ele.getName() == "volume") {
                                tankObject.setVolume(Double.parseDouble(ele.getText()));
                            } else if (ele.getName() == "capacity") {
                                tankObject.setCapacity(Double.parseDouble(ele.getText()));
                            } else if (ele.getName() == "oil-type") {
                                tankObject.setOiltype(Integer.parseInt(ele.getText()));
                            } else if (ele.getName() == "assign") {
                                tankObject.setAssign(Integer.parseInt(ele.getText()));
                            } else if (ele.getName() == "maintenance") {
                                tankObject.setMaintenance(Integer.parseInt(ele.getText()) > 0 ? true : false);
                            }
                        }
                    }
                } else if (element.getName() == "pipes") {
                    for (Element pipe : element.elements()) {
                        int pipe_no = Integer.parseInt(pipe.attributeValue("id"));
                        PipeObject pipeObject = pipes.get(pipe_no - 1);

                        for (Element ele : pipe.elements()) {
                            if (ele.getName() == "volume") {
                                pipeObject.setVol(Double.parseDouble(ele.getText()));
                            } else if (ele.getName() == "charging-speed") {

                                double[] chargingSpeed = new double[ele.elements().size()];
                                double[] cost = new double[ele.elements().size()];
                                Iterator<Element> speed = ele.elementIterator();
                                for (int i = 0; i < ele.elements().size(); i++) {
                                    // 获取下一元素
                                    Element e = speed.next();
                                    chargingSpeed[i] = Double.parseDouble(e.getText());
                                    cost[i] = Double.parseDouble(e.attributeValue("cost"));
                                }
                                pipeObject.setChargingSpeed(chargingSpeed);
                                pipeObject.setCost(cost);
                            }
                        }
                    }
                } else if (element.getName() == "dss") {
                    for (Element ds : element.elements()) {
                        int ds_no = Integer.parseInt(ds.attributeValue("id"));
                        DSObject dsObject = dss.get(ds_no - 1);

                        for (Element ele : ds.elements()) {
                            if (ele.getName() == "feeding-speed") {
                                dsObject.setSpeed(Double.parseDouble(ele.getText()));
                            } else if (ele.getName() == "fps") {

                                // 进料包（可能有多个，按照顺序排列）
                                List<FPObject> fps = new LinkedList<>();
                                for (int i = 0; i < ele.elements().size(); i++) {

                                    Element fpElement = ele.elements().get(i);
                                    FPObject fpObject = new FPObject();
                                    fpObject.setVolume(Double.parseDouble(fpElement.getText()));
                                    fpObject.setOiltype(Integer.parseInt(fpElement.attributeValue("type")));
                                    fpObject.setSite(Integer.parseInt(fpElement.attributeValue("site")));

                                    fps.add(fpObject);
                                }

                                dsObject.setFps(fps);
                            }
                        }
                    }
                }
            }
        } catch (DocumentException e) {
            logger.fatal(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * 单例模式
     */
    public static Config getInstance() {
        if (_instance == null) {

            List<TankObject> tanks = new ArrayList<>();
            List<DSObject> dss = new LinkedList<DSObject>();
            List<PipeObject> pipes = new LinkedList<>();

            for (int i = 0; i < 11; i++) {
                tanks.add(new TankObject());
            }

            for (int i = 0; i < 4; i++) {
                dss.add(new DSObject());
            }

            for (int i = 0; i < 2; i++) {
                pipes.add(new PipeObject());
            }

            _instance = new Config(dss, tanks, pipes);
        }
        return _instance;
    }
}
