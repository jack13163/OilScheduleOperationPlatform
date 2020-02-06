package org.rl4j.fly;

import java.awt.image.BufferedImage;

/**
 * Ӣ�ۻ�:�Ƿ�����
 */
public class Hero extends FlyingObject {

    private BufferedImage[] images = {};  //Ӣ�ۻ�ͼƬ
    private int index = 0;                //Ӣ�ۻ�ͼƬ�л�����

    private int doubleFire;   //˫������
    private int life;   //��

    /**
     * ��ʼ������
     */
    public Hero() {
        life = 3;   //��ʼ3����
        doubleFire = 0;   //��ʼ����Ϊ0
        images = new BufferedImage[]{ShootGame.hero0, ShootGame.hero1}; //Ӣ�ۻ�ͼƬ����
        image = ShootGame.hero0;   //��ʼΪhero0ͼƬ
        width = image.getWidth();
        height = image.getHeight();
        x = 150;
        y = 400;
    }

    /**
     * ��ȡ˫������
     */
    public int isDoubleFire() {
        return doubleFire;
    }

    /**
     * ����˫������
     */
    public void setDoubleFire(int doubleFire) {
        this.doubleFire = doubleFire;
    }

    /**
     * ���ӻ���
     */
    public void addDoubleFire() {
        doubleFire = 40;
    }

    /**
     * ����
     */
    public void addLife() {  //����
        life++;
    }

    /**
     * ����
     */
    public void subtractLife() {   //����
        life--;
    }

    /**
     * ��ȡ��
     */
    public int getLife() {
        return life;
    }

    /**
     * ��ǰ�����ƶ���һ�£���Ծ��룬x,y���λ��
     */
    public void moveTo(int x, int y) {
        this.x = x - width / 2;
        this.y = y - height / 2;
    }

    /**
     * Խ�紦��
     */
    @Override
    public boolean outOfBounds() {
        return false;
    }

    /**
     * �����ӵ�
     */
    public Bullet[] shoot() {
        int xStep = width / 4;      //4��
        int yStep = 20;  //��
        if (doubleFire > 0) {  //˫������
            Bullet[] bullets = new Bullet[2];
            bullets[0] = new Bullet(x + xStep, y - yStep);  //y-yStep(�ӵ���ɻ���λ��)
            bullets[1] = new Bullet(x + 3 * xStep, y - yStep);
            return bullets;
        } else {      //��������
            Bullet[] bullets = new Bullet[1];
            bullets[0] = new Bullet(x + 2 * xStep, y - yStep);
            return bullets;
        }
    }

    /**
     * �ƶ�
     */
    @Override
    public void step() {
        if (images.length > 0) {
            image = images[index++ / 10 % images.length];  //�л�ͼƬhero0��hero1
        }
    }

    /**
     * ��ײ�㷨
     */
    public boolean hit(FlyingObject other) {

        int x1 = other.x - this.width / 2;                 //x������С����
        int x2 = other.x + this.width / 2 + other.width;   //x����������
        int y1 = other.y - this.height / 2;                //y������С����
        int y2 = other.y + this.height / 2 + other.height; //y����������

        int herox = this.x + this.width / 2;               //Ӣ�ۻ�x�������ĵ����
        int heroy = this.y + this.height / 2;              //Ӣ�ۻ�y�������ĵ����

        return herox > x1 && herox < x2 && heroy > y1 && heroy < y2;   //���䷶Χ��Ϊײ����
    }

}










