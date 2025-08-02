package zz.app.example;

import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.ModuleListener;
import com.github.unidbg.linux.android.dvm.*;

import zz.base.BaseJni;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class moji extends BaseJni {

    moji() {

//        //================= 基本配置 ===========================
//        public boolean is64Bit;             //是否ARM64     //项目名
//        public String projectName;          //项目名
//        public String apkName;              //apk文件名
//        public String bundleName;           //app包名
//        public String soName;               //so的名字，掐头趣味，例如: libSinger.so，则传入Singer
//        public String clsName;              //接口类

        //1.App初始化
        this.is64Bit = true;
        this.projectName = "example";
        this.apkName = "moji.apk";
        this.bundleName = "com.moji.mjweather";
        this.soName = "encrypt";
        this.clsName = "com.moji.mjweather.library.Digest";

//        //2.额外配置
//        //添加模块加载监听器
//        this.moduleListener = new ModuleListener() {
//            @Override
//            public void onLoaded(Emulator<?> emulator, Module module) {
//                System.err.println("loaded lib: " + module.name);
//                if(module.name.contains("signer")){
//                    emulator.traceCode(module.base, module.base+module.size);
//                }
//            }
//        };

        build();

    }

    public static void main(String[] args) {

        moji test = new moji();
        System.err.println("sign = " + test.call_sign());
    }

    public String call_sign() {

        System.err.println("开始 call sign: ");

        //traceCount();

        String traceFile = rootPath() + "/trace/moji_func_trace.log";
        PrintStream traceStream = createTraceStream(traceFile);
        traceFunction(traceStream);


        List<Object> params = new ArrayList<>(10);
        params.add(vm.getJNIEnv());
        params.add(0);

        // arg3
        String input = "{\"common\":{\"platform\":\"Android\",\"identifier\":\"\",\"app_version\":\"1009087802\",\"os_version\":\"31\",\"device\":\"Pixel 4\",\"brand\":\"google\",\"pid\":\"5068\",\"language\":\"CN\",\"uid\":\"968403975009353785\",\"uaid\":\"968403975172931586\",\"width\":1080,\"height\":2236,\"package_name\":\"com.moji.mjweather\",\"amp\":\"1725432467598\",\"locationcity\":1,\"current_city\":33,\"token\":\"0b3d96327296374be7af09cf92d92295\",\"vip\":\"0\",\"weather_tab_style\":0,\"giuid\":\"gtc_97da812cf686a7a19d79e40fc4b97d8232\",\"smid\":\"DUwXaIhvLQDepEEOAgqF5yLJ-5V4vD8Dpk86\",\"security_request\":0,\"net\":\"wifi\"},\"params\":{\"mobile\":\"EdiOaYg8uVwFTpYfiKiFOg==\",\"is_sercret\":1}}";
        params.add(vm.addLocalObject(new StringObject(vm, input)));

        Number number = module.callFunction(emulator, 0x3D1A0L, params.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        return result;
    }


    //================================== 补环境 ========================================

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "com/moji/tool/AppDelegate->getAppContext()Landroid/content/Context;": {
                return vm.resolveClass("com.view.mjweather.MJApplication").newObject(null);
            }
            case "android/os/ServiceManager->getService(Ljava/lang/String;)Landroid/os/IBinder;": {
                return null;
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "android/app/ActivityThread->sCurrentActivityThread:Landroid/app/ActivityThread;": {
                return vm.resolveClass("android/app/ActivityThread").newObject(null);
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    @Override
    public void setStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature, DvmObject<?> value) {
        switch (signature) {
            case "android/app/ActivityThread->sPackageManager:Landroid/content/pm/IPackageManager;": {
                return;
            }
        }
        super.setStaticObjectField(vm, dvmClass, signature, value);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "com/view/mjweather/MJApplication->getPackageManager()Landroid/content/pm/PackageManager;": {
                return vm.resolveClass("android.content.pm.PackageManager").newObject(null);
            }
            case "com/view/mjweather/MJApplication->getPackageName()Ljava/lang/String;": {
                return new StringObject(vm, "com.moji.mjweather");
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }





}
