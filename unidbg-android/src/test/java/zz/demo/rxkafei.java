package zz.demo;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.MemoryBlock;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.virtualmodule.android.AndroidModule;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * 瑞幸咖啡
 */
public class rxkafei extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    rxkafei(){
        // 创建模拟器实例,进程名建议依照实际进程名填写，可以规避针对进程名的校验
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.lucky.luckyclient").build();
        // 获取模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK，Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/apks/rxkf/rxkf5.0.01.apk"));
        // 设置JNI
        vm.setJni(this);
        // 打印日志
        vm.setVerbose(true);
        new AndroidModule(emulator, vm).register(memory);
        // 加载目标SO
        DalvikModule dm = vm.loadLibrary("cryptoDD", true);
        //获取本SO模块的句柄,后续需要用它
        module = dm.getModule();
        // 调用JNI OnLoad
        dm.callJNI_OnLoad(emulator);
    }

    public static void main(String[] args) {
        rxkafei rxkafei = new rxkafei();
        rxkafei.HookByConsoleDebugger();
        rxkafei.callaes();
//        for (int i = 0; i < 200; i++) {
//            rxkf.callDfa();
//            rxkf.call_wbaes();
//        }
//        rxkf.callmd5();
    }

    public void callaes(){
        // args list
        List<Object> list = new ArrayList<>(10);
        // jnienv
        list.add(vm.getJNIEnv());
        // jclazz
        list.add(0);
        // str1
        String str = "yangruhua";
        byte[] byteArray = str.getBytes();
        list.add(vm.addLocalObject(new ByteArray(vm,byteArray)));
        // 最后的int
        list.add(0);

        Number number = module.callFunction(emulator, 0x1b1cd, list.toArray());
        ByteArray resultArr = vm.getObject(number.intValue());
        String base64String = Base64.getEncoder().encodeToString(resultArr.getValue());
        System.out.println("aesresult:"+base64String);
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int unsignedInt = b & 0xff;
            String hex = Integer.toHexString(unsignedInt);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static byte[] hexToBytes(String hexString) {
        // 将十六进制字符串转换为字节数组
        return DatatypeConverter.parseHexBinary(hexString);
    }


    public void call_wbaes(){
        MemoryBlock inputBlock = emulator.getMemory().malloc(16,true);
        UnidbgPointer inputPtr = inputBlock.getPointer();
        MemoryBlock ouputBlock = emulator.getMemory().malloc(16,true);
        UnidbgPointer ouputPtr = ouputBlock.getPointer();
        byte[] byteArray = hexToBytes("68656C6C6F0B0B0B0B0B0B0B0B0B0B0B");
        assert byteArray != null;
        inputPtr.write(0,byteArray,0,byteArray.length);
        module.callFunction(emulator,0x17bd5, inputPtr,16,ouputPtr,0);
        String res = bytesToHex(ouputPtr.getByteArray(0,0x10));
        System.out.println(res);
        inputBlock.free();
        ouputBlock.free();
    }

    public static int randint(int min,int max){
        Random rand = new Random();
        return rand.nextInt((max-min)+1)+min;
    }

    public void callDfa(){
        Debugger debugger = emulator.attach();
        debugger.addBreakPoint(module.base+0x14f98,new BreakPointCallback() {
            UnidbgPointer pointer;
            RegisterContext context = emulator.getContext();

            int num = 1;
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                pointer = context.getPointerArg(0);
                if(num%9==0){
                    pointer.setByte(randint(0,15),(byte) randint(0,0xff));
                }
                num+=1;
                return true;
            }

        });

    }

    public void callmd5(){
        // args list
        List<Object> list = new ArrayList<>(10);
        // jnienv
        list.add(vm.getJNIEnv());
        // jclazz
        list.add(0);
        // str1
        String str = "yangruhua";
        byte[] byteArray = str.getBytes();
        list.add(vm.addLocalObject(new ByteArray(vm,byteArray)));
        // 最后的int
        list.add(1);

        Number number = module.callFunction(emulator, 0x1a981, list.toArray());

        ByteArray resultArr = vm.getObject(number.intValue());
        String md5result = new String(resultArr.getValue(), StandardCharsets.UTF_8);
        System.out.println("md5result:"+md5result);
    };

    public void HookByConsoleDebugger() {
        Debugger debugger = emulator.attach();// 0x15C8C不走
//        debugger.addBreakPoint(module.base+0x15320,new BreakPointCallback() {
//            RegisterContext context = emulator.getContext();
//            @Override
//            public boolean onHit(Emulator<?> emulator, long address) {
//                emulator.attach().addBreakPoint(context.getLRPointer().peer, new BreakPointCallback() {
//                @Override
//                public boolean onHit(Emulator<?> emulator, long address) {
//                    return true;
//                }
//            });
//                return false;  // m0x402d2010
//            }
//
//        });

//        debugger.addBreakPoint(module.base+0x14f98,new BreakPointCallback() {  //0x15AD6不走
//            RegisterContext context = emulator.getContext();
//            int num = 1;
//            @Override
//            public boolean onHit(Emulator<?> emulator, long address) {
//                System.out.println("num:"+num);
//                emulator.attach().addBreakPoint(context.getLRPointer().peer, new BreakPointCallback() {
//                @Override
//                public boolean onHit(Emulator<?> emulator, long address) {
//                    return true;
//                }
//            });
//            num+=1;
//            return true;
//            }
//
//        });
//        debugger.addBreakPoint(module.base + 0x13e3c);
        debugger.addBreakPoint(module.base + 0x15320,new BreakPointCallback() {
            RegisterContext context = emulator.getContext();
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                emulator.attach().addBreakPoint(context.getLRPointer().peer, new BreakPointCallback() {
                @Override
                public boolean onHit(Emulator<?> emulator, long address) {
                    return false;
                }
            });
                return false;  //m0xbffff6f4  m0x402d2000
            }

        });

    }


}



/*
*68 6f 0b 0b    68 6f 0b 0b
*65 0b 0b 0b    0b 0b 0b 65
*6c 0b 0b 0b    0b 0b 6c 0b
*6c 0b 0b 0b    0b 6c 0b 0b
*
*
*
* */