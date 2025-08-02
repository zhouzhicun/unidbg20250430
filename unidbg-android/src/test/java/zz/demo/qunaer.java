package zz.demo;



import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class qunaer extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    qunaer(){
        // 创建模拟器实例
        emulator = AndroidEmulatorBuilder.for64Bit().setProcessName("com.Qunar").build();
        // 获取模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK，Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/apks/qne/qne10.2.14.apk"));
        // 设置JNI
        vm.setJni(this);
        // 打印日志
        vm.setVerbose(true);
        // 加载目标SO
        DalvikModule dm = vm.loadLibrary("goblin_6_1_1", true);
        // DalvikModule dm = vm.loadLibrary(new File("unidbg-android/apks/pdd/lib.so"), true);
        //获取本SO模块的句柄,后续需要用它
        module = dm.getModule();
        // 调用JNI OnLoad
        dm.callJNI_OnLoad(emulator);
    }


    public static void main(String[] args) {
        qunaer demo = new qunaer();
        demo.callByAddress();
    }

    public void callByAddress(){
        // args list
        List<Object> list = new ArrayList<>(10);
        // jnienv
        list.add(vm.getJNIEnv());
        // jclazz
        list.add(0);


        byte[] arr = {0x31};
        list.add(vm.addLocalObject(new ByteArray(vm,arr)));
        Number number = module.callFunction(emulator, 0x86cc, list.toArray());
        ByteArray resultArr = vm.getObject(number.intValue());

        Formatter formatter = new Formatter();
        for (byte b : resultArr.getValue()) {
            formatter.format("%02X", b);
        }
        String hexString = formatter.toString();
        System.out.println(hexString);
    };



    //=============================== 补环境 =====================================

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature){
            case "com/mqunar/core/basectx/application/QApplication->getContext()Landroid/content/Context;":{
                return vm.resolveClass("android/content/Context").newObject(signature);
            }
            case "java/security/MessageDigest->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;":{
                StringObject type = varArg.getObjectArg(0);
                System.out.println("====input:"+type);
                try {
                    return dvmClass.newObject(MessageDigest.getInstance(type.getValue()));
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature){
            case "java/security/MessageDigest->digest([B)[B":{
                MessageDigest messageDigest = (MessageDigest) dvmObject.getValue();
                ByteArray array = varArg.getObjectArg(0);
                assert array != null;
                return new ByteArray(vm, messageDigest.digest(array.getValue()));
            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }
}