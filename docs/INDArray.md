一、ND4J的在内存中的存储结构

    对于ND4J而言，所有的数据都存储在堆外内存，是一维的连续内存，INDArray 只是指向了这片连续的内存空间，把连续内存映射成张量，ND4J定义了两种排序规则：C order和F order，C order表示行优先，F order表示列优先。下图展示了ND4J的内存存储。

    上图可以看出，不过张量是几维，对应的物理存储都是一维的连续内存空间，NDArray在指向这片连续的地址，这正是ND4J强大的地方，对于各种矩阵操作，例如：矩阵转置、矩阵加标量等等操作，都可以轻而易举的实现，而不用花力气去dup一个巨型数组，高性能也表现在这种优雅的设计方式上。

二、ND4J的基本操作

    1、加法

    INDArray add(INDArray other)  ：元素对应相加，返回的张量是拷贝出来的

    INDArray addi(INDArray other) ：元素对应相加，与上面不同的是，返回值不是拷贝出来的新数组，而是用计算结果替换原内存数据

    INDArray add(Number n)：每个元素加上一个标量

    INDArray addi(Number n)：每个元素加上一个标量，并覆盖原数组

    2、减法

     INDArray sub(Number n)：每个元素减去一个标量

    INDArray subi(Number n)：每个元素减去标量，并覆盖原数组

    INDArray sub(INDArray other)：对应元素相减

     INDArray subi(INDArray other)：对应元素相减，并覆盖原数组

    3、乘法

    乘法分两种，对应元素相乘和矩阵乘法

    INDArray mul(INDArray other)：对应元素相乘

    INDArray muli(INDArray other)：对应元素相乘，并覆盖原数组

     INDArray mmul(INDArray other)：矩阵相乘

    INDArray mmuli(INDArray other)：矩阵相乘，并覆盖原数组

    4、除法

    INDArray div(INDArray other)：对应元素相除

    INDArray divi(INDArray other)：对应元素相除并覆盖原数组

    INDArray div(Number n)：每个元素除以一个标量

    INDArray divi(Number n)：每个元素除以一个标量，并覆盖原数组

    5、矩阵转置

     INDArray transpose()

    INDArray transposei()

    总结一下：后面以i结尾的方法，表示in place，也就是会覆盖原内存空间的数据，和”传引用“一个意思

    6、张量创建

    Nd4j类中定义了很多静态方法，用于创建N维张量，用法例如： Nd4j.zeros(nRows, nColumns)

    public static INDArray zeros(int rows, int columns) ：创建一个全部元素为0的张量

     public static INDArray ones(int rows, int columns) ：创建一个全部元素为1的张量

    public static INDArray hstack(INDArray... arrs)：沿着水平方向接起多个矩阵，矩阵必须有相同的行

    public static INDArray vstack(INDArray... arrs)：沿着垂直方向接起多个矩阵，矩阵必须有相同的列

     public static INDArray rand(int rows, int columns)：随机对应形状的张量

    public static INDArray rand(int[] shape)：随机对应形状的张量

    7、张量设置值

    INDArray putScalar(int[] i, double value)：对应位置设置标量

    INDArray putScalar(int row, int col, double value)：对应行列处设置标量

    INDArray put(INDArrayIndex[] indices, INDArray element):对应维度处设置INDArray

    8、控制张量形状

    INDArray reshape(int... newShape)：重新定义张量形状
    
    9、ND4J序列化和反序列INDArray
    
    import org.nd4j.linalg.api.ndarray.INDArray;
    import org.nd4j.linalg.factory.Nd4j;
    import org.nd4j.serde.binary.BinarySerde;
    
    import java.io.*;
    import java.nio.ByteBuffer;
    
    INDArray arrWrite = Nd4j.linspace(1,10,10);
    INDArray arrRead;
    
    //1. Binary format  二进制格式
    //   Close the streams manually or use try with resources.
    try (DataOutputStream sWrite = new DataOutputStream(new FileOutputStream(new File("tmp.bin")))) {
        Nd4j.write(arrWrite, sWrite);
        }
    
    try (DataInputStream sRead = new DataInputStream(new FileInputStream(new File("tmp.bin")))) {
        arrRead = Nd4j.read(sRead);
        }
    
    //2. Binary format using java.nio.ByteBuffer; 使用特定类库存储二进制格式
    ByteBuffer buffer = BinarySerde.toByteBuffer(arrWrite);
    arrRead = BinarySerde.toArray(buffer);
    
    //3. Text format 存储文本格式
    Nd4j.writeTxt(arrWrite, "tmp.txt");
    arrRead = Nd4j.readTxt("tmp.txt");
    
    // To read csv format: 存储成为CSV格式，但是该方法已经标记为过时
    // The writeNumpy method has been deprecated.
    arrRead =Nd4j.readNumpy("tmp.csv", ", ");