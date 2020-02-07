package opt.rl4j.fly;

/**
 * ����
 */
public interface Award {
    int DOUBLE_FIRE = 0;  //˫������
    int LIFE = 1;   //1����

    /**
     * ��ý�������(�����0��1)
     */
    int getType();
}
