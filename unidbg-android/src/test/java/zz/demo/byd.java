package zz.demo;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.SystemService;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.MapsFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import unicorn.Arm64Const;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 比亚迪（梆梆企业版加固）
 */
public class byd extends AbstractJni implements IOResolver {

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;

    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        System.out.println("get path:"+pathname);
        if ("/proc/self/maps".equals(pathname) || ("/proc/" + emulator.getPid() + "/maps").equals(pathname)) {
            return FileResult.success(new SimpleFileIO(oflags,new File("unidbg-android/src/test/resources/byd/maps"),pathname));
        }
        return null;
    }
    byd(){
        // 创建模拟器实例
        emulator = AndroidEmulatorBuilder.for64Bit().setProcessName("com.byd.aeri.caranywhere").build();
        // 获取模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));
        emulator.getSyscallHandler().addIOResolver(this);
        // 创建Android虚拟机,传入APK，Unidbg可以替我们做部分签名校验的工作
        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/resources/byd/byd.apk"));
        // 设置JNI
        vm.setJni(this);
        // 打印日志
        vm.setVerbose(true);
        //这个是虚拟模块
        new AndroidModule(emulator,vm).register(memory);
        // 加载目标SO
//        DalvikModule dm = vm.loadLibrary("encrypt", true);
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/byd/libencrypt_fixed.so"), true);
        //获取本SO模块的句柄,后续需要用它
        module = dm.getModule();
        // 调用JNI OnLoad
        dm.callJNI_OnLoad(emulator);
    }


    public static void main(String[] args) {
        byd demo = new byd();
        String encryptdata = demo.checkcode();
        System.out.println("encryptdata:" + encryptdata);
    }

    public String checkcode(){
        //arg list
        ArrayList<Object> params = new ArrayList<>(10);
        //jnienv
        params.add(vm.getJNIEnv());
        //jclazz
        params.add(0);
        //str1  参数1
        StringObject str1 = new StringObject(vm, "F{\"encryData\":\"81DCED420565FC031EE422D8E515A6C9AFAE09A9201D71528D09D4FAC05FBF9A8B62BD8633A1DD06420D7DFA2FC549CBF57AEB7347C0386FB5B9A119ED47D96961E68C72D6562722164FD64329DDBF70\",\"identifier\":\"club100\",\"reqTimestamp\":\"1715590883092\",\"imeiMD5\":\"A7C923F15DCB9EDF87324572F1EFA4C6\",\"sign\":\"AEd76A489234FC8b3C871Cd13C84F12bF72cA87\",\"serviceDir\":\"v2_Goods.getSystemConfigImage\",\"serverFlag\":\"e_shop\",\"identifierType\":2,\"appChannel\":\"1\",\"objective\":\"\"}");
        params.add(vm.addLocalObject(str1));
        //int 参数2
        params.add(0);
        //str2 参数3
        StringObject str2 = new StringObject(vm, "1715591022250");
        params.add(vm.addLocalObject(str2));

        Number number = module.callFunction(emulator, 0x253ac, params.toArray());
        StringObject res = vm.getObject(number.intValue());
        return res.toString();
    }



    //===================================== 补环境 ============================================

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature){
            case "android/app/ActivityThread->currentActivityThread()Landroid/app/ActivityThread;":{
                return dvmClass.newObject(null);
            }
            case "android/os/SystemProperties->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;":{
                String arg0 = varArg.getObjectArg(0).getValue().toString();
                String arg1 = varArg.getObjectArg(1).getValue().toString();
                System.out.println(arg0+"===="+arg1);
                if(arg0.equals("ro.serialno")){
                    return new StringObject(vm, "unknown");
                }
                return new StringObject(vm,"");
            }

        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature){
            case "android/app/ActivityThread->getSystemContext()Landroid/app/ContextImpl;":{
                return vm.resolveClass("android/app/ContextImpl").newObject(null);
            }
            case "android/app/ContextImpl->getPackageManager()Landroid/content/pm/PackageManager;":{
                DvmClass clazz = vm.resolveClass("android/content/pm/PackageManager");
                return clazz.newObject(signature);
            }
            case "android/app/ContextImpl->getSystemService(Ljava/lang/String;)Ljava/lang/Object;":{
                StringObject serviceName = varArg.getObjectArg(0);
                assert serviceName != null;
                System.out.println(serviceName.toString());
                return new SystemService(vm, serviceName.getValue());
            }
            case "android/net/wifi/WifiManager->getConnectionInfo()Landroid/net/wifi/WifiInfo;":{
                return vm.resolveClass("android/net/wifi/WifiInfo").newObject(null);
            }
            case "android/net/wifi/WifiInfo->getMacAddress()Ljava/lang/String;":{
                return new StringObject(vm,"da:c4:13:ef:95:aa");
            }

        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }
}
