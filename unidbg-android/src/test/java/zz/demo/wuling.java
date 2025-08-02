package zz.demo;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 五菱汽车（梆梆企业版加固）
 */
public class wuling extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private final Memory memory;

    wuling() {
        // 创建模拟器实例,进程名建议依照实际进程名填写，可以规避针对进程名的校验
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.cloudy.linglingbang").build();
        // 获取模拟器的内存操作接口
        memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        // 创建Android虚拟机,传入APK，Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/apks/llb/llb.apk"));
        // 设置JNI
        vm.setJni(this);
        // 打印日志
        vm.setVerbose(true);
        // 加载目标SO
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/apks/llb/libencrypt.so"), true);
        //获取本SO模块的句柄,后续需要用它
        module = dm.getModule();
        // 调用JNI OnLoad
        dm.callJNI_OnLoad(emulator);
    }



    public static void main(String[] args) {
        wuling llb = new wuling();
        llb.callByAddress();
        llb.decrtpy("Mhub8kSp2n38SHF4COj57zjesFrzCIB2JiH76iCwZZffL3Y4+1/fq1uEDKKWe4yAwiacSVxXNSq1sWN5TwtfHaVgxpOREVGT2+qZEZFkvjP1GaxPCPP2jwuy4x3GvPgHl2NhG2kpsfcXHHQK9HJ5iBdtO44QdDO0vtgqU9MGGb+3q+HJwKlgfWJZj24t8HOSypJNigdCXbUEC6HGEhZhAhMX+Za1lffLlxUouhVh8rzKyESEF97li1h1vTbEf6TJyMbbdEpxh355FbxV9wZgorCa93rDfu+bsVLDbQaAF1TcacxnokoS/yv92hYaqzwzSX3UdH5oQutjW6A4gH1Zk/1Yb3k+IHofvc6Lfm+cxrLHLDtsus9SM/4+2oqsE7tsbgUny37/PQXtUJEOwebDtpz5oYxPgEIbLKIHvptVKwh4=");
    }

    public String callByAddress() {
        // args list
        List<Object> list = new ArrayList<>(5);
        // jnienv
        list.add(vm.getJNIEnv());
        // jclazz
        list.add(0);
        // str1
        list.add(vm.addLocalObject(new StringObject(vm, "mobile=13535535353&password=fjfjfjffk&client_id=2019041810222516127&client_secret=c5ad2a4290faa3df39683865c2e10310&state=eu4acofTmb&response_type=token")));
        // int
        list.add(2);
        // str2
        list.add(vm.addLocalObject(new StringObject(vm, "1709100421650")));
        Number number = module.callFunction(emulator, 0x13A19, list.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println("======encrypt:" + result);
        return result;
    }


    public void decrtpy(String str) {
        // args list
        List<Object> list = new ArrayList<>(5);
        // jnienv
        list.add(vm.getJNIEnv());
        // jclazz
        list.add(0);
        // str
        list.add(vm.addLocalObject(new StringObject(vm, str)));
        // int
        Number number = module.callFunction(emulator, 0x165E1, list.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println("======decrypt:" + result);
    }


    //=============================== 补环境 ==================================================


    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "android/app/ActivityThread->currentActivityThread()Landroid/app/ActivityThread;": {
                return vm.resolveClass("android/app/ActivityThread").newObject(null);
            }
            case "android/os/SystemProperties->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;": {
                String arg = varArg.getObjectArg(0).getValue().toString();
                System.out.println("SystemProperties get arg:" + arg);
                if (arg.equals("ro.serialno")) {
                    return new StringObject(vm, "9B131FFBA001Y5");
                }
            }
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/app/ActivityThread->getSystemContext()Landroid/app/ContextImpl;": {
//                System.out.println("22222");
                return vm.resolveClass("android/app/ContextImpl").newObject(null);
            }
            case "android/app/ContextImpl->getPackageManager()Landroid/content/pm/PackageManager;": {
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);
            }
            case "android/app/ContextImpl->getSystemService(Ljava/lang/String;)Ljava/lang/Object;": {
                String arg = varArg.getObjectArg(0).getValue().toString();
//                System.out.println("getSystemService arg:"+arg);
                return vm.resolveClass("android.net.wifi").newObject(signature);
            }
            case "android/net/wifi->getConnectionInfo()Landroid/net/wifi/WifiInfo;": {
                return vm.resolveClass("android/net/wifi/WifiInfo").newObject(null);
            }
            case "android/net/wifi/WifiInfo->getMacAddress()Ljava/lang/String;": {
                return new StringObject(vm, "02:00:00:00:00:00");
            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "android/os/Build->MODEL:Ljava/lang/String;": {
                return new StringObject(vm, "Pixel 4 XL");
            }
            case "android/os/Build->MANUFACTURER:Ljava/lang/String;": {
                return new StringObject(vm, "Google");
            }
            case "android/os/Build$VERSION->SDK:Ljava/lang/String;": {
                return new StringObject(vm, "29");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }


}