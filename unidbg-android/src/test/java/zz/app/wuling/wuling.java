package zz.app.wuling;

import com.github.unidbg.linux.android.dvm.*;
import zz.base.BaseJni;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


public class wuling extends BaseJni {

    wuling() {

//        //================= 基本配置 ===========================
//        public boolean is64Bit;             //是否ARM64     //项目名
//        public String projectName;          //项目名
//        public String apkName;              //apk文件名
//        public String bundleName;           //app包名
//        public String soName;               //so的名字，掐头趣味，例如: libSinger.so，则传入Singer
//        public String clsName;              //接口类

        //1.App初始化
        this.is64Bit = true;
        this.projectName = "wuling";
        this.apkName = "wuling_V8.2.12.apk";
        this.bundleName = "com.cloudy.linglingbang";
        this.soName = "encrypt";
        this.clsName = "com.bangcle.comapiprotect.CheckCodeUtil";

        this.traceJNIOnloadFlag = false;

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

        zz.app.wuling.wuling test = new zz.app.wuling.wuling();
//        System.err.println("sd = " + test.call_checkcode());
        System.err.println("decrypt result = " + test.call_decrypt());
    }




    public String call_checkcode() {

        System.err.println("开始 call checkcode: ");

        List<Object> params = new ArrayList<>(10);
        params.add(vm.getJNIEnv());
        params.add(0);

        String var1 = "pageCode=79&areaCode=1&longitude=0.0&latitude=0.0";
        Integer var2 = 2;
        String var3 = "1754058938886";

        // arg3
       params.add(vm.addLocalObject(new StringObject(vm, var1)));
       params.add(var2);
       params.add(vm.addLocalObject(new StringObject(vm, var3)));


        Number number = module.callFunction(emulator, 0x25250L, params.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        return result;
    }


    public String call_decrypt() {

        trace();
//        addBreakpoint();
        patch();

        List<Object> params = new ArrayList<>(10);
        params.add(vm.getJNIEnv());
        params.add(0);

        //String var1 = "MYqL/QMm32+bUSkRZsbbo0ho6r4LCfUWAF/Y65P+qYj7TyThGkDTpYvwXwXafColOtqct+nb5z8l45ocSon6eNMx1gIbafraxweuHnufLoZ3F4UkpSl4t/vHZtUGydhYywU448ltZ/dpKfXYCAgC99fluL1KtRNcZdV9Q6C4uE53hJlaUeqEjtW0M5WQi+wcdV8SRB6Mg1G8W5HoKEL+UPqvn25UVJhi84r+MPoFG/grju3X+rQH/O6uL+cVWGZi+";
        String var1 = "MYqL/QMm32+bUSkRZsbbo0ho6r4LCfUWAF/Y65P+qYj7TyThGkDTpYvwXwXafColOtqct+nb5z8l45ocSon6eNMx1gIbafraxweuHnufLoZ3F4UkpSl4t/vHZtUGydhYywU448ltZ/dpKfXYCAgC99fluL1KtRNcZdV9Q6C4uE53hJlaUeqEjtW0M5WQi+wcdx5uP422/5bYdMawTpLE5/k07gmttHz006kQ9zm2H3JXcbY8rRQgZxhHPOpJ8O0eS";

        // arg3
        params.add(vm.addLocalObject(new StringObject(vm, var1)));
        Number number = module.callFunction(emulator, 0x2BEE0L, params.toArray());
        String result = vm.getObject(number.intValue()).getValue().toString();
        return result;

    }

    public void trace() {

        //traceCount();
        String traceFile = rootPath() + "/trace/decrypt_func_trace.log";
        PrintStream traceStream = createTraceStream(traceFile);
        //emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
        traceFunction(traceStream);
    }

    public void addBreakpoint() {
//        addBreakpoint(0x2C58C);
//        addBreakpoint(0x2C598);
    }

    public void patch() {
//        nop64(0x2C578, (0x2C75C - 0x2C578) / 4);
//        nop64(0x2C880, 1);

        //patch逻辑：直接跳过检测，注意：编写patch逻辑时，相关B指令跳转时，最后一个参数是两条指令的偏移值。
        //2C590  CBNZ  W0, loc_2C5CC
        patch(0x2C590, "CBNZ  W0, 0x1cc");     // 即 0x1cc 是 0x2C75C 相对于 0x2C590 的偏移。
    }

    //================================== 补环境 ========================================

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {

        switch (signature) {
            case "android/app/ActivityThread->currentActivityThread()Landroid/app/ActivityThread;": {
                return vm.resolveClass("android/app/ActivityThread").newObject(null);
            }
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {

        switch (signature) {
            case "android/app/ActivityThread->getSystemContext()Landroid/app/ContextImpl;": {
                return vm.resolveClass("android/app/ContextImpl").newObject(null);
            }
            case "android/app/ContextImpl->getPackageManager()Landroid/content/pm/PackageManager;": {
                return vm.resolveClass("android/content/pm/PackageManager").newObject(null);
            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "android/os/Build->MODEL:Ljava/lang/String;": {
                return new StringObject(vm, "Pixel 4");
            }
            case "android/os/Build->MANUFACTURER:Ljava/lang/String;": {
                return new StringObject(vm, "Google");
            }
            case "android/os/Build$VERSION->SDK:Ljava/lang/String;": {
                return new StringObject(vm, "31");
            }

        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    //===========================================================================


    //
//    @Override
//    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
//        switch (signature) {
//            case "com/moji/tool/AppDelegate->getAppContext()Landroid/content/Context;": {
//                return vm.resolveClass("com.view.mjweather.MJApplication").newObject(null);
//            }
//            case "android/os/ServiceManager->getService(Ljava/lang/String;)Landroid/os/IBinder;": {
//                return null;
//            }
//        }
//        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
//    }
//
//    @Override
//    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
//        switch (signature) {
//            case "android/app/ActivityThread->sCurrentActivityThread:Landroid/app/ActivityThread;": {
//                return vm.resolveClass("android/app/ActivityThread").newObject(null);
//            }
//        }
//        return super.getStaticObjectField(vm, dvmClass, signature);
//    }
//
//    @Override
//    public void setStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature, DvmObject<?> value) {
//        switch (signature) {
//            case "android/app/ActivityThread->sPackageManager:Landroid/content/pm/IPackageManager;": {
//                return;
//            }
//        }
//        super.setStaticObjectField(vm, dvmClass, signature, value);
//    }
//
//    @Override
//    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
//        switch (signature) {
//            case "com/view/mjweather/MJApplication->getPackageManager()Landroid/content/pm/PackageManager;": {
//                return vm.resolveClass("android.content.pm.PackageManager").newObject(null);
//            }
//            case "com/view/mjweather/MJApplication->getPackageName()Ljava/lang/String;": {
//                return new StringObject(vm, "com.moji.mjweather");
//            }
//        }
//        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
//    }





}
