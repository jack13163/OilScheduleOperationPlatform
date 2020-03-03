package opt.rl4j.fly;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ShootGame extends JPanel {
    public static final int WIDTH = 400; // ????
    public static final int HEIGHT = 654; // ????
    /**
     * ?????????: START RUNNING PAUSE GAME_OVER
     */
    private int state;
    private static final int START = 0;
    private static final int RUNNING = 1;
    private static final int PAUSE = 2;
    private static final int GAME_OVER = 3;

    private int score = 0; // ?÷?
    private Timer timer; // ?????
    private int intervel = 1000 / 100; // ?????(????)

    public static BufferedImage background;
    public static BufferedImage start;
    public static BufferedImage airplane;
    public static BufferedImage bee;
    public static BufferedImage bullet;
    public static BufferedImage hero0;
    public static BufferedImage hero1;
    public static BufferedImage pause;
    public static BufferedImage gameover;

    private FlyingObject[] flyings = {}; // ?л?????
    private Bullet[] bullets = {}; // ???????
    private Hero hero = new Hero(); // ????

    static {
        try {
            background = ImageIO.read(new java.io.File("img/background.png"));
            start = ImageIO.read(new java.io.File("img/start.png"));
            airplane = ImageIO.read(new java.io.File("img/airplane.png"));
            bee = ImageIO.read(new java.io.File("img/bee.png"));
            bullet = ImageIO.read(new java.io.File("img/bullet.png"));
            hero0 = ImageIO.read(new java.io.File("img/hero0.png"));
            hero1 = ImageIO.read(new java.io.File("img/hero1.png"));
            pause = ImageIO.read(new java.io.File("img/pause.png"));
            gameover = ImageIO.read(new java.io.File("img/gameover.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??
     */
    @Override
    public void paint(Graphics g) {
        g.drawImage(background, 0, 0, null); // ???????
        paintHero(g); // ??????
        paintBullets(g); // ?????
        paintFlyingObjects(g); // ????????
        paintScore(g); // ??????
        paintState(g); // ???????
    }

    /**
     * ??????
     */
    public void paintHero(Graphics g) {
        g.drawImage(hero.getImage(), hero.getX(), hero.getY(), null);
    }

    /**
     * ?????
     */
    public void paintBullets(Graphics g) {
        for (int i = 0; i < bullets.length; i++) {
            Bullet b = bullets[i];
            g.drawImage(b.getImage(), b.getX() - b.getWidth() / 2, b.getY(),
                    null);
        }
    }

    /**
     * ????????
     */
    public void paintFlyingObjects(Graphics g) {
        for (int i = 0; i < flyings.length; i++) {
            FlyingObject f = flyings[i];
            g.drawImage(f.getImage(), f.getX(), f.getY(), null);
        }
    }

    /**
     * ??????
     */
    public void paintScore(Graphics g) {
        int x = 10; // x????
        int y = 25; // y????
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 22); // ????
        g.setColor(new Color(0xFF0000));
        g.setFont(font); // ????????
        g.drawString("SCORE:" + score, x, y); // ??????
        y = y + 20; // y??????20
        g.drawString("LIFE:" + hero.getLife(), x, y); // ????
    }

    /**
     * ???????
     */
    public void paintState(Graphics g) {
        switch (state) {
            case START: // ??????
                g.drawImage(start, 0, 0, null);
                break;
            case PAUSE: // ?????
                g.drawImage(pause, 0, 0, null);
                break;
            case GAME_OVER: // ????????
                g.drawImage(gameover, 0, 0, null);
                break;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fly");
        ShootGame game = new ShootGame(); // ??????
        frame.add(game); // ?????????JFrame??
        frame.setSize(WIDTH, HEIGHT); // ?????С
        frame.setAlwaysOnTop(true); // ??????????????
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ????????
        frame.setIconImage(new ImageIcon("images/icon.jpg").getImage()); // ???????????
        frame.setLocationRelativeTo(null); // ?????????λ??
        frame.setVisible(true); // ???????paint

        game.action(); // ???????
    }

    /**
     * ??????д???
     */
    public void action() {
        // ?????????
        MouseAdapter l = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) { // ??????
                if (state == RUNNING) { // ???????????????--?????λ??
                    int x = e.getX();
                    int y = e.getY();
                    hero.moveTo(x, y);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) { // ??????
                if (state == PAUSE) { // ???????????
                    state = RUNNING;
                }
            }

            @Override
            public void mouseExited(MouseEvent e) { // ??????
                if (state == RUNNING) { // ???δ??????????????????
                    state = PAUSE;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) { // ?????
                switch (state) {
                    case START:
                        state = RUNNING; // ????????????
                        break;
                    case GAME_OVER: // ????????????????
                        flyings = new FlyingObject[0]; // ????????
                        bullets = new Bullet[0]; // ??????
                        hero = new Hero(); // ???????????
                        score = 0; // ?????
                        state = START; // ???????????
                        break;
                }
            }
        };
        this.addMouseListener(l); // ?????????????
        this.addMouseMotionListener(l); // ?????????????

        timer = new Timer(); // ?????????
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (state == RUNNING) { // ??????
                    enterAction(); // ????????
                    stepAction(); // ?????
                    shootAction(); // ???????
                    bangAction(); // ??????????
                    outOfBoundsAction(); // ??????????Ｐ???
                    checkGameOverAction(); // ??????????
                }
                repaint(); // ??棬????paint()????
            }

        }, intervel, intervel);
    }

    int flyEnteredIndex = 0; // ????????????

    /**
     * ????????
     */
    public void enterAction() {
        flyEnteredIndex++;
        if (flyEnteredIndex % 40 == 0) { // 400?????????????????--10*40
            FlyingObject obj = nextOne(); // ????????????????
            flyings = Arrays.copyOf(flyings, flyings.length + 1);
            flyings[flyings.length - 1] = obj;
        }
    }

    /**
     * ?????
     */
    public void stepAction() {
        for (int i = 0; i < flyings.length; i++) { // ???????????
            FlyingObject f = flyings[i];
            f.step();
        }

        for (int i = 0; i < bullets.length; i++) { // ????????
            Bullet b = bullets[i];
            b.step();
        }
        hero.step(); // ?????????
    }

    /**
     * ???????????
     */
    public void flyingStepAction() {
        for (int i = 0; i < flyings.length; i++) {
            FlyingObject f = flyings[i];
            f.step();
        }
    }

    int shootIndex = 0; // ???????

    /**
     * ???
     */
    public void shootAction() {
        shootIndex++;
        if (shootIndex % 30 == 0) { // 300???????
            Bullet[] bs = hero.shoot(); // ????????
            bullets = Arrays.copyOf(bullets, bullets.length + bs.length); // ????
            System.arraycopy(bs, 0, bullets, bullets.length - bs.length,
                    bs.length); // ???????
        }
    }

    /**
     * ????????????????
     */
    public void bangAction() {
        for (int i = 0; i < bullets.length; i++) { // ???????????
            Bullet b = bullets[i];
            bang(b); // ????????????????????
        }
    }

    /**
     * ??????????Ｐ???
     */
    public void outOfBoundsAction() {
        int index = 0; // ????
        FlyingObject[] flyingLives = new FlyingObject[flyings.length]; // ??????????
        for (int i = 0; i < flyings.length; i++) {
            FlyingObject f = flyings[i];
            if (!f.outOfBounds()) {
                flyingLives[index++] = f; // ??????????
            }
        }
        flyings = Arrays.copyOf(flyingLives, index); // ?????????????????

        index = 0; // ?????????0
        Bullet[] bulletLives = new Bullet[bullets.length];
        for (int i = 0; i < bullets.length; i++) {
            Bullet b = bullets[i];
            if (!b.outOfBounds()) {
                bulletLives[index++] = b;
            }
        }
        bullets = Arrays.copyOf(bulletLives, index); // ???????????????
    }

    /**
     * 检测游戏是否结束的Action
     */
    public void checkGameOverAction() {
        if (isGameOver() == true) {
            state = GAME_OVER; // ?????
            BeepUtil.playSound("sound/a.wav");
        }
    }

    /**
     * 判断游戏是否结束
     */
    public boolean isGameOver() {

        for (int i = 0; i < flyings.length; i++) {
            int index = -1;
            FlyingObject obj = flyings[i];
            if (hero.hit(obj)) { // 是否碰到障碍物
                hero.subtractLife(); // 生命值减一
                hero.setDoubleFire(0); // 双炮变为单炮
                BeepUtil.playSound("sound/bombo.wav");
                index = i;
            }
            if (index != -1) {
                FlyingObject t = flyings[index];
                flyings[index] = flyings[flyings.length - 1];
                flyings[flyings.length - 1] = t; // ?????????????????????

                flyings = Arrays.copyOf(flyings, flyings.length - 1); // ?????????????
            }
        }

        return hero.getLife() <= 0;
    }

    /**
     * ????????????????????
     */
    public void bang(Bullet bullet) {
        int index = -1; // ???е??????????
        for (int i = 0; i < flyings.length; i++) {
            FlyingObject obj = flyings[i];
            if (obj.shootBy(bullet)) { // ?ж???????
                index = i; // ????????е???????????
                break;
            }
        }
        if (index != -1) { // ?л??е??????
            FlyingObject one = flyings[index]; // ????????е??????

            FlyingObject temp = flyings[index]; // ?????е??????????????????????
            flyings[index] = flyings[flyings.length - 1];
            flyings[flyings.length - 1] = temp;

            flyings = Arrays.copyOf(flyings, flyings.length - 1); // ???????????????(???????е?)

            // ???one??????(??????????????)
            if (one instanceof Enemy) { // ??????????????????
                Enemy e = (Enemy) one; // ??????????
                score += e.getScore(); // ???
            } else { // ????????????????
                Award a = (Award) one;
                int type = a.getType(); // ???????????
                switch (type) {
                    case Award.DOUBLE_FIRE:
                        hero.addDoubleFire(); // ???????????
                        break;
                    case Award.LIFE:
                        hero.addLife(); // ???ü???
                        break;
                }
            }
        }
    }

    /**
     * 产生飞行物
     *
     * @return
     */
    public static FlyingObject nextOne() {
        Random random = new Random();
        int type = random.nextInt(20); // [0,20)
        if (type < 4) {
            return new Bee();
        } else {
            return new Airplane();
        }
    }

}
