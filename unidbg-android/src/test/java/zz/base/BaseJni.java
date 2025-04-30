package zz.base;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.ModuleListener;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.ARM32SyscallHandler;
import com.github.unidbg.linux.ARM64SyscallHandler;
import com.github.unidbg.linux.AndroidSyscallHandler;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.github.unidbg.virtualmodule.android.JniGraphics;
import com.github.unidbg.virtualmodule.android.MediaNdkModule;
import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneMode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


import java.io.*;
import java.util.List;

public class BaseJni extends AbstractJni {


    //================= 基本配置 ===========================
    public boolean is64Bit;             //是否ARM64     //项目名
    public String projectName;          //项目名
    public String apkName;              //apk文件名
    public String bundleName;           //app包名
    public String soName;               //so的名字，掐头趣味，例如: libSinger.so，则传入Singer
    public String clsName;              //接口类

    //================= 扩展配置 ===========================
    public boolean loadVirtualModuleFlag = false;               //是否需要加载虚拟Module
    public List<String> dependlibraryList = null;               //依赖库列表
    public List<IOResolver<AndroidFileIO>> ioResolvers = null;  //ioResolvers列表
    public ModuleListener moduleListener = null;                //module加载的监听器


    //=======================================================
    public AndroidEmulator emulator;
    public VM vm;
    public Module module;
    public DvmObject<?> nativeAPI;


    /******************************* build ****************************************/

    public void build() {
        createEmulator();
        commonBuild();
    }

    /**
     * 创建模拟器
     */
    public void createEmulator() {

        AndroidEmulatorBuilder resultBuilder = null;
        if (is64Bit) {
            resultBuilder = AndroidEmulatorBuilder.for64Bit();
        } else {
            resultBuilder = AndroidEmulatorBuilder.for32Bit();
        }

        //2.创建emulator
        String virtualfs = String.format("%s/rootfs", rootPath());
        emulator = resultBuilder.addBackendFactory(new Unicorn2Factory(true))
                .setProcessName(bundleName)
                .setRootDir(new File(virtualfs))
                .build();
    }


    /**
     * 通用build
     */
    public void commonBuild() {

        //1.支持多线程
        emulator.getSyscallHandler().setEnableThreadDispatcher(true);
        emulator.getBackend().registerEmuCountHook(100000);

        //2.初始化Memory
        Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));

        //3.創建VM
        String apkPath = String.format("%s/%s", rootPath(), apkName);
        vm = emulator.createDalvikVM(new File(apkPath));
        vm.setJni(this);
        vm.setVerbose(true);


        extendBuild();

        //6.获取DvmClass
        DalvikModule dm = vm.loadLibrary(soName, true);
        module = dm.getModule();
        dm.callJNI_OnLoad(emulator);
        nativeAPI = vm.resolveClass(clsName);

    }


    public void extendBuild() {

        Memory memory = emulator.getMemory();

        //1.添加ioresolver
        if (ioResolvers != null) {
            for (IOResolver<AndroidFileIO> resolver : ioResolvers) {
                emulator.getSyscallHandler().addIOResolver(resolver);
            }
        }

        //2.添加虚拟module
        if (loadVirtualModuleFlag) {
            new AndroidModule(emulator, vm).register(memory);
            new JniGraphics(emulator, vm).register(memory);
            new MediaNdkModule(emulator, vm).register(memory);
        }

        //3.添加依赖库
        if(dependlibraryList != null) {
            for (String libName : dependlibraryList) {
                vm.loadLibrary(libName, true);
            }
        }

        //4.添加modulelistener
        if (moduleListener != null) {
            memory.addModuleListener(moduleListener);
        }
    }



    //========================================= helper ========================================================


    public String rootPath() {
        return String.format("%s/%s", "unidbg-android/src/test/java/zz/app", projectName);
    }

    public static void createFile(String filePath) {
        File file = new File(filePath);
        try {
            //如果文件已存在，则先删除。
            if (file.exists()) {
                file.delete();
            }
            //创建新文件
            file.createNewFile();

        } catch (IOException e) {
            System.out.println(filePath + " 文件创建失败。");
            e.printStackTrace();
        }
    }




    //========================================= 日志 ========================================================

    /**
     * 启用DEBUG日志
     */
    public void enableDebugLog(Class cls) {
        Logger.getLogger(cls).setLevel(Level.DEBUG);
    }

    public void enableNormalDebugLog() {
        if (is64Bit) {
            Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
        } else {
            Logger.getLogger(ARM32SyscallHandler.class).setLevel(Level.DEBUG);
        }
        Logger.getLogger(AndroidSyscallHandler.class).setLevel(Level.DEBUG);
    }

    //========================================= patch指令 ==============================================


    /**
     patch指令
     例如：asmCode = "subs r0, r2, r3";
     */
    public void patch(long addr, String asmCode) {

        KeystoneArchitecture arch = KeystoneArchitecture.Arm;
        if (is64Bit) {
            arch = KeystoneArchitecture.Arm64;
        }

        UnidbgPointer pointer = UnidbgPointer.pointer(this.emulator, addr);
        Keystone keystone = new Keystone(arch, KeystoneMode.LittleEndian);
        byte[] codeBytes = keystone.assemble(asmCode).getMachineCode();
        pointer.write(codeBytes);
    }

    //========================================= 断点 ========================================================

    /**
     * 添加断点
     */
    public void addBreakpoint(long address) {

        if (!is64Bit) {
            address += 1;
        }
        emulator.attach().addBreakPoint(module.base + address);

    }


    //========================================= trace ========================================================

    /**
     使用：
     String traceFile = appInfo.outputfs + "/xhs_shield_tracewrite.log";
     PrintStream traceStream = Utils.createTracePrintStream(traceFile);

     emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
     emulator.traceRead().setRedirect(traceStream);
     emulator.traceWrite().setRedirect(traceStream);
     */
    public PrintStream createTraceStream(String traceFile) {
        createFile(traceFile);
        try {
            FileOutputStream fileStream = new FileOutputStream(traceFile);
            return new PrintStream(fileStream);
        } catch (FileNotFoundException e) {
            System.err.println("file not found: " + traceFile);
        }
        return null;
    }






    //=============================================== 获取DvmClass, DvmObject ======================================================

    /**
     * 获取DvmClass, 传入DvmObject, 得到它的class.
     */
    public DvmClass getDvmCls(DvmObject<?> dvmObject) {
        return dvmObject.getObjectType();
    }

    /**
     *
     *  获取DvmClass, 传入类的继承数组，返回DvmClass
     *  例如：获取ApplicationContext类: getDvmObject(["android/content/Context", "android/content/ContextWrapper", "android/app/Application"])
     */
    public DvmClass getDvmCls(String[] clsArr) {
        DvmClass cls = null;
        for (String clsName : clsArr) {
            if(cls == null) {
                cls = vm.resolveClass(clsName);
            } else {
                cls = vm.resolveClass(clsName, cls);
            }
        }
        return cls;
    }

    /**
     * 获取DvmObject
     * 传入类的继承数组，以及初始值， 返回DvmClass
     */
    public DvmObject<?> getDvmObject(String[] clsArr, Object value) {
        DvmClass cls = getDvmCls(clsArr);
        if(cls == null) {
            return null;
        }
        return cls.newObject(value);
    }

    /**
     * 获取ApplicationContext
     */
    public DvmObject<?> getAppContext() {
        String[] clsArr = new String[]{"android/content/Context", "android/content/ContextWrapper", "android/app/Application"};
        return getDvmObject(clsArr,null);
    }
}
