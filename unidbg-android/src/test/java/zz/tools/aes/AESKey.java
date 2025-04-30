package zz.tools.aes;

import java.util.Arrays;


/**
 * AESKey实现类主要包括3部分逻辑：
 * 1.SBox和RCon定义；
 * 2.AES128, AES192, AES256 秘钥编排实现；
 * 3.AES128-192-256 轮秘钥快速判断和完整判断实现。
 */
public class AESKey {

    //标准AES加密-S盒
    private static final byte[] Sbox = {
            (byte)0x63,(byte)0x7c,(byte)0x77,(byte)0x7b,(byte)0xf2,(byte)0x6b,(byte)0x6f,(byte)0xc5,
            (byte)0x30,(byte)0x01,(byte)0x67,(byte)0x2b,(byte)0xfe,(byte)0xd7,(byte)0xab,(byte)0x76,
            (byte)0xca,(byte)0x82,(byte)0xc9,(byte)0x7d,(byte)0xfa,(byte)0x59,(byte)0x47,(byte)0xf0,
            (byte)0xad,(byte)0xd4,(byte)0xa2,(byte)0xaf,(byte)0x9c,(byte)0xa4,(byte)0x72,(byte)0xc0,
            (byte)0xb7,(byte)0xfd,(byte)0x93,(byte)0x26,(byte)0x36,(byte)0x3f,(byte)0xf7,(byte)0xcc,
            (byte)0x34,(byte)0xa5,(byte)0xe5,(byte)0xf1,(byte)0x71,(byte)0xd8,(byte)0x31,(byte)0x15,
            (byte)0x04,(byte)0xc7,(byte)0x23,(byte)0xc3,(byte)0x18,(byte)0x96,(byte)0x05,(byte)0x9a,
            (byte)0x07,(byte)0x12,(byte)0x80,(byte)0xe2,(byte)0xeb,(byte)0x27,(byte)0xb2,(byte)0x75,
            (byte)0x09,(byte)0x83,(byte)0x2c,(byte)0x1a,(byte)0x1b,(byte)0x6e,(byte)0x5a,(byte)0xa0,
            (byte)0x52,(byte)0x3b,(byte)0xd6,(byte)0xb3,(byte)0x29,(byte)0xe3,(byte)0x2f,(byte)0x84,
            (byte)0x53,(byte)0xd1,(byte)0x00,(byte)0xed,(byte)0x20,(byte)0xfc,(byte)0xb1,(byte)0x5b,
            (byte)0x6a,(byte)0xcb,(byte)0xbe,(byte)0x39,(byte)0x4a,(byte)0x4c,(byte)0x58,(byte)0xcf,
            (byte)0xd0,(byte)0xef,(byte)0xaa,(byte)0xfb,(byte)0x43,(byte)0x4d,(byte)0x33,(byte)0x85,
            (byte)0x45,(byte)0xf9,(byte)0x02,(byte)0x7f,(byte)0x50,(byte)0x3c,(byte)0x9f,(byte)0xa8,
            (byte)0x51,(byte)0xa3,(byte)0x40,(byte)0x8f,(byte)0x92,(byte)0x9d,(byte)0x38,(byte)0xf5,
            (byte)0xbc,(byte)0xb6,(byte)0xda,(byte)0x21,(byte)0x10,(byte)0xff,(byte)0xf3,(byte)0xd2,
            (byte)0xcd,(byte)0x0c,(byte)0x13,(byte)0xec,(byte)0x5f,(byte)0x97,(byte)0x44,(byte)0x17,
            (byte)0xc4,(byte)0xa7,(byte)0x7e,(byte)0x3d,(byte)0x64,(byte)0x5d,(byte)0x19,(byte)0x73,
            (byte)0x60,(byte)0x81,(byte)0x4f,(byte)0xdc,(byte)0x22,(byte)0x2a,(byte)0x90,(byte)0x88,
            (byte)0x46,(byte)0xee,(byte)0xb8,(byte)0x14,(byte)0xde,(byte)0x5e,(byte)0x0b,(byte)0xdb,
            (byte)0xe0,(byte)0x32,(byte)0x3a,(byte)0x0a,(byte)0x49,(byte)0x06,(byte)0x24,(byte)0x5c,
            (byte)0xc2,(byte)0xd3,(byte)0xac,(byte)0x62,(byte)0x91,(byte)0x95,(byte)0xe4,(byte)0x79,
            (byte)0xe7,(byte)0xc8,(byte)0x37,(byte)0x6d,(byte)0x8d,(byte)0xd5,(byte)0x4e,(byte)0xa9,
            (byte)0x6c,(byte)0x56,(byte)0xf4,(byte)0xea,(byte)0x65,(byte)0x7a,(byte)0xae,(byte)0x08,
            (byte)0xba,(byte)0x78,(byte)0x25,(byte)0x2e,(byte)0x1c,(byte)0xa6,(byte)0xb4,(byte)0xc6,
            (byte)0xe8,(byte)0xdd,(byte)0x74,(byte)0x1f,(byte)0x4b,(byte)0xbd,(byte)0x8b,(byte)0x8a,
            (byte)0x70,(byte)0x3e,(byte)0xb5,(byte)0x66,(byte)0x48,(byte)0x03,(byte)0xf6,(byte)0x0e,
            (byte)0x61,(byte)0x35,(byte)0x57,(byte)0xb9,(byte)0x86,(byte)0xc1,(byte)0x1d,(byte)0x9e,
            (byte)0xe1,(byte)0xf8,(byte)0x98,(byte)0x11,(byte)0x69,(byte)0xd9,(byte)0x8e,(byte)0x94,
            (byte)0x9b,(byte)0x1e,(byte)0x87,(byte)0xe9,(byte)0xce,(byte)0x55,(byte)0x28,(byte)0xdf,
            (byte)0x8c,(byte)0xa1,(byte)0x89,(byte)0x0d,(byte)0xbf,(byte)0xe6,(byte)0x42,(byte)0x68,
            (byte)0x41,(byte)0x99,(byte)0x2d,(byte)0x0f,(byte)0xb0,(byte)0x54,(byte)0xbb,(byte)0x16
    };

    //标准AES加密-Rcon
    private static final byte[] Rcon = {(byte)0x8d, (byte)0x01, (byte)0x02, (byte)0x04, (byte)0x08,
            (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x80, (byte)0x1b, (byte)0x36};



//=================================== 1.AESKey秘钥编排，包括AES128,192,256 =======================================================

    /**
     AES-128密钥扩展实现逻辑
     1.初始密钥直接拷贝前4个字（16字节）
     2.每4轮执行一次RotWord（循环左移）+ SubWord（S盒替换）+ Rcon异或操作
     3.其他轮次直接与前4轮异或生成新密钥
     */
    public static byte[] ExpandKey128BigEdian(byte[] Key){
        byte[] RoundKey = new byte[176];  //4 * 4 * 11; 一行4字，每字4字节；11轮（11行）；共176字节
        int i, j, k;
        byte[] tempa = new byte[4];

        // 第一轮key就是原始key本身
        for (i = 0; i < 4; ++i)
        {
            RoundKey[(i * 4) + 0] = Key[(i * 4) + 0];
            RoundKey[(i * 4) + 1] = Key[(i * 4) + 1];
            RoundKey[(i * 4) + 2] = Key[(i * 4) + 2];
            RoundKey[(i * 4) + 3] = Key[(i * 4) + 3];
        }

        //逐字生成
        for (i = 4; i < 44; ++i) {
            {
                //1.保存上一轮4字到tempa数组
                k = (i - 1) * 4;
                tempa[0] = RoundKey[k + 0];
                tempa[1] = RoundKey[k + 1];
                tempa[2] = RoundKey[k + 2];
                tempa[3] = RoundKey[k + 3];
            }
            if(i % 4 == 0){

                // 1.循环左移
                {
                    byte u8tmp = tempa[0];
                    tempa[0] = tempa[1];
                    tempa[1] = tempa[2];
                    tempa[2] = tempa[3];
                    tempa[3] = u8tmp;
                }

                // SubWord() is a function that takes a four-byte input word and
                // applies the S-box to each of the four bytes to produce an output word.

                // 2.S盒替换
                // Function Subword()
                {
                    tempa[0] = getSBoxValue(tempa[0]);
                    tempa[1] = getSBoxValue(tempa[1]);
                    tempa[2] = getSBoxValue(tempa[2]);
                    tempa[3] = getSBoxValue(tempa[3]);
                }

                // 3.Rcon异或
                tempa[0] = (byte) (tempa[0] ^ Rcon[i/4]);

            }


            // 最后一步
            j = i * 4;  k=(i - 4) * 4;
            RoundKey[j + 0] = (byte) (RoundKey[k + 0] ^ tempa[0]);
            RoundKey[j + 1] = (byte) (RoundKey[k + 1] ^ tempa[1]);
            RoundKey[j + 2] = (byte) (RoundKey[k + 2] ^ tempa[2]);
            RoundKey[j + 3] = (byte) (RoundKey[k + 3] ^ tempa[3]);
        }

        return RoundKey;
    };




    public static byte[] ExpandKey192BigEdian(byte[] Key) {
        byte[] RoundKey = new byte[208];  //4 * 4 * 13; 一行4字，每字4字节；13轮（13行）；共208字节
        int i, j, k;
        byte[] tempa = new byte[4];

        // 初始密钥拷贝（前6个字）
        for (i = 0; i < 6; ++i) {
            RoundKey[(i * 4) + 0] = Key[(i * 4) + 0];
            RoundKey[(i * 4) + 1] = Key[(i * 4) + 1];
            RoundKey[(i * 4) + 2] = Key[(i * 4) + 2];
            RoundKey[(i * 4) + 3] = Key[(i * 4) + 3];
        }

        // 主循环生成后续46个字（i从6到51）
        for (i = 6; i < 52; ++i) {
            // 取前一个字
            k = (i - 1) * 4;
            tempa[0] = RoundKey[k + 0];
            tempa[1] = RoundKey[k + 1];
            tempa[2] = RoundKey[k + 2];
            tempa[3] = RoundKey[k + 3];

            // 每6个字的起始位置处理
            if (i % 6 == 0) {
                // 循环左移
                byte u8tmp = tempa[0];
                tempa[0] = tempa[1];
                tempa[1] = tempa[2];
                tempa[2] = tempa[3];
                tempa[3] = u8tmp;

                // S盒替换
                tempa[0] = getSBoxValue(tempa[0]);
                tempa[1] = getSBoxValue(tempa[1]);
                tempa[2] = getSBoxValue(tempa[2]);
                tempa[3] = getSBoxValue(tempa[3]);

                // 异或轮常数（Rcon索引为i/6）
                tempa[0] = (byte) (tempa[0] ^ Rcon[i / 6]);
            }

            // 每6字中间位置处理（i mod 6 == 3）
            else if (i % 6 == 3) {
                // 仅S盒替换
                tempa[0] = getSBoxValue(tempa[0]);
                tempa[1] = getSBoxValue(tempa[1]);
                tempa[2] = getSBoxValue(tempa[2]);
                tempa[3] = getSBoxValue(tempa[3]);
            }

            // 异或前第6个字生成新字
            j = i * 4;
            k = (i - 6) * 4;
            RoundKey[j + 0] = (byte) (RoundKey[k + 0] ^ tempa[0]);
            RoundKey[j + 1] = (byte) (RoundKey[k + 1] ^ tempa[1]);
            RoundKey[j + 2] = (byte) (RoundKey[k + 2] ^ tempa[2]);
            RoundKey[j + 3] = (byte) (RoundKey[k + 3] ^ tempa[3]);
        }

        return RoundKey;
    }


    /**
     AES256 秘钥编排
     密钥编排将32个字节经过运算推演出15个轮密钥，每一组16个字节，称之为K0,K1，....K14。
     在AES-256中，密钥扩展后得16*15共240字节，使用时逐十六个字节划分成K0,K1，....K14使用；
     */
    public static byte[] ExpandKey256BigEdian(byte[] Key){
        byte[] RoundKey = new byte[240];  //4 * 4 * 15; 一行4字，每字4字节；15轮（15行）；共240字节
        int i, j, k;
        byte[] tempa = new byte[4];
        // The first round key is the key itself.
        for (i = 0; i < 8; ++i)
        {
            RoundKey[(i * 4) + 0] = Key[(i * 4) + 0];
            RoundKey[(i * 4) + 1] = Key[(i * 4) + 1];
            RoundKey[(i * 4) + 2] = Key[(i * 4) + 2];
            RoundKey[(i * 4) + 3] = Key[(i * 4) + 3];
        }
        for (i = 8; i < 60; ++i) {
            {
                k = (i - 1) * 4;
                tempa[0] = RoundKey[k + 0];
                tempa[1] = RoundKey[k + 1];
                tempa[2] = RoundKey[k + 2];
                tempa[3] = RoundKey[k + 3];
            }
            if(i % 8 == 0){
                {
                    // 循环左移
                    byte u8tmp = tempa[0];
                    tempa[0] = tempa[1];
                    tempa[1] = tempa[2];
                    tempa[2] = tempa[3];
                    tempa[3] = u8tmp;
                }

                // SubWord() is a function that takes a four-byte input word and
                // applies the S-box to each of the four bytes to produce an output word.

                // Function Subword()
                {
                    tempa[0] = getSBoxValue(tempa[0]);
                    tempa[1] = getSBoxValue(tempa[1]);
                    tempa[2] = getSBoxValue(tempa[2]);
                    tempa[3] = getSBoxValue(tempa[3]);
                }

                tempa[0] = (byte) (tempa[0] ^ Rcon[i/8]);

            }

            // 每8字中间位置处理（i mod 8 == 4）
            if(i % 8 == 4){
                // 仅S盒替换
                tempa[0] = getSBoxValue(tempa[0]);
                tempa[1] = getSBoxValue(tempa[1]);
                tempa[2] = getSBoxValue(tempa[2]);
                tempa[3] = getSBoxValue(tempa[3]);
            }
            // 最后一步
            // 异或前第8个字生成新字
            j = i * 4; k=(i - 8) * 4;
            RoundKey[j + 0] = (byte) (RoundKey[k + 0] ^ tempa[0]);
            RoundKey[j + 1] = (byte) (RoundKey[k + 1] ^ tempa[1]);
            RoundKey[j + 2] = (byte) (RoundKey[k + 2] ^ tempa[2]);
            RoundKey[j + 3] = (byte) (RoundKey[k + 3] ^ tempa[3]);
        }

        return RoundKey;
    };


    //=================================== AESKey快速判断, 包括AES128,192,256 =======================================================


    /**
     * 输入参数：32字节
     * 快速判断AES128秘钥
     * 判断原理：
     * 在AES128加密算法中, 秘钥编排生成11轮秘钥，每轮秘钥4字， 判断第二轮的第2,3,4字。
     */
    public static boolean isAes128KeyFastJudge(byte[] InputArray){

        for(int i = 20; i < 32; i++){
            if(InputArray[i] != (InputArray[i - 4] ^ InputArray[i - 16])){
                return false;
            }
        }
        return true;
    }

    /**
     * 输入参数： 48字节。
     AES192秘钥长度：24字节
     按照每6字一行，检查第二行的6字；因为第一行的6字（6 * 4 = 24字节）是原始秘钥。
     其中第二行的第1个字和第4个字有参与S盒替换，所以不检查， 而是检查第2行的 第2,3字 和 5,6字； 因此拆成两个循环。
     第一个循环：检查第2行的2,3字；对应字节索引是28至35（即28 <= i < 36），覆盖8个字节。这对应于密钥扩展后的第一个关键段，假设此处满足前4字节与前24字节异或的规律。
     第二个循环：检查第2行的5,6字；对应字节索引的40至48（即40 <= i < 48），覆盖另一个8字节段。该范围跳过4字节（36至39），模拟原函数中两个循环间的间隔。
     */
    public static boolean isAes192KeyFastJudge(byte[] InputArray) {

        for (int i = 28; i < 36; i++) {
            if (InputArray[i] != (InputArray[i - 4] ^ InputArray[i - 24])) {
                return false;
            }
        }
        for (int i = 40; i < 48; i++) {
            if (InputArray[i] != (InputArray[i - 4] ^ InputArray[i - 24])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 输入参数：64字节
     * AES256秘钥长度：32字节
     * 前面32字节是原始秘钥，检查第二排秘钥
     * 第二排的第1个字和 第5个字需要跳过，原理同 AES192, 所以拆成两个循环。
     */
    public static boolean isAes256KeyFastJudge(byte[] InputArray){

        for(int i = 36; i < 48; i++){
            if(InputArray[i] != (InputArray[i - 4] ^ InputArray[i - 32])){
                return false;
            }
        }
        for(int i = 52; i < 64; i++){
            if(InputArray[i] != (InputArray[i - 4] ^ InputArray[i - 32])){
                return false;
            }
        }
        return true;
    }



//=================================== AESKey判断, 包括AES128,192,256 =======================================================

    //转小端序
    public static byte[] ConvertToLittleEdian(byte[] InputArray){
        byte[] LittleEdianKey = new byte[InputArray.length];
        for(int i = 0; i < (InputArray.length / 4) ; i ++){
            LittleEdianKey[(i * 4) + 0] = InputArray[(i + 1) * 4 - 1];
            LittleEdianKey[(i * 4) + 1] = InputArray[(i + 1) * 4 - 2];
            LittleEdianKey[(i * 4) + 2] = InputArray[(i + 1) * 4 - 3];
            LittleEdianKey[(i * 4) + 3] = InputArray[(i + 1) * 4 - 4];
        }
        return LittleEdianKey;
    }


    // 判断传入的176个元素的字节数组是否是标准/非标准的AES-128 Key
    // 返回值：0:不是Key， 1: 大端序的Key，2: 是大端序的魔改Key，3: 是小端序的标准Key，4: 是小端序的魔改Key
    public static int IsAes128Key(byte[] InputArray){
        byte[] KeyExpandedBig = ExpandKey128BigEdian(InputArray);
        if(arrayEquals(KeyExpandedBig, InputArray) > 0){   //arrayEquals返回0,1；0：不相等，1：相等；
            return 1;
        }else if(arrayEquals(ConvertToLittleEdian(ExpandKey128BigEdian(ConvertToLittleEdian(InputArray))), InputArray)>0){
            return 3;
        }else {
            return 0;
        }

    };

    // 判断传入的208个元素的字节数组是否是标准/非标准的AES-256 Key
    // 返回值：0：不是Key， 1：大端序的Key，2：大端序的魔改Key，3：小端序的标准Key，4：小端序的魔改Key
    public static int IsAes192Key(byte[] InputArray){
        byte[] KeyExpandedBig = ExpandKey192BigEdian(InputArray);
        if(arrayEquals(KeyExpandedBig, InputArray) > 0){   //arrayEquals返回0,1；0：不相等，1：相等；
            return 1;
        }else if(arrayEquals(ConvertToLittleEdian(ExpandKey192BigEdian(ConvertToLittleEdian(InputArray))), InputArray)>0){
            return 3;
        }else {
            return 0;
        }

    };

    // 判断传入的240个元素的字节数组是否是标准/非标准的AES-256 Key
    // 返回值：0：不是Key， 1：大端序的Key，2：大端序的魔改Key，3：小端序的标准Key，4：小端序的魔改Key
    public static int IsAes256Key(byte[] InputArray){
        byte[] KeyExpandedBig = ExpandKey256BigEdian(InputArray);
        if(arrayEquals(KeyExpandedBig, InputArray) > 0){
            return 1;
        }else if(arrayEquals(ConvertToLittleEdian(ExpandKey256BigEdian(ConvertToLittleEdian(InputArray))), InputArray)>0){
            return 3;
        }else {
            return 0;
        }

    };

    //==================================== helper ===========================================


    public static byte getSBoxValue(byte num){
        return Sbox[byte2Int(num)];
    }

    private static int byte2Int(byte b) {
        return (b & 0xff);
    }

    public static int arrayEquals(byte[] arr1, byte[] arr2){
        if (Arrays.equals(arr1, arr2)) {
            return 1;
        }else {
            return 0;
        }
    };
}
