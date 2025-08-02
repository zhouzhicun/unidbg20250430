package zz.base;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.ModuleListener;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.backend.Unicorn2Factory;

import com.github.unidbg.debugger.FunctionCallListener;
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
    public boolean traceJNIOnloadFlag = false;                  //是否trace JNIOnload
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

        if(traceJNIOnloadFlag) {
            trace_JNI_Onload();
        }
        dm.callJNI_OnLoad(emulator);
        nativeAPI = vm.resolveClass(clsName);

    }

    public void trace_JNI_Onload() {
        String traceFile = rootPath() + "/trace/JNI_OnLoad_trace.log";
        PrintStream traceStream = createTraceStream(traceFile);
        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
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
            createDirectories(file.getParentFile());
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

    /**
     * 递归创建目录（包括所有不存在的父目录）
     * @param dir 要创建的目录
     */
    private static void createDirectories(File dir) {
        if (dir == null) {
            return; // 如果传入的目录为null，则返回（比如文件没有父目录）
        }
        if (dir.exists()) {
            return; // 如果目录已存在，直接返回
        }
        // 先递归创建父目录
        createDirectories(dir.getParentFile());
        // 然后创建当前目录
        if (dir.mkdir()) {
            System.out.println("目录创建成功: " + dir.getPath());
        } else {
            System.out.println("目录创建失败: " + dir.getPath());
        }
    }




    //========================================= 日志 ========================================================

    /**
     * 启用DEBUG日志
     */
    public void enableDebugLog(Class cls) {
        Logger.getLogger(cls).setLevel(Level.DEBUG);
    }

    public void enableDebug() {
        if (is64Bit) {
            //Logger.getLogger(DalvikVM64.class).setLevel(Level.DEBUG);
            Logger.getLogger(ARM64SyscallHandler.class).setLevel(Level.DEBUG);
        } else {
            //Logger.getLogger(DalvikVM.class).setLevel(Level.DEBUG);
            Logger.getLogger(ARM32SyscallHandler.class).setLevel(Level.DEBUG);
        }
        Logger.getLogger(AndroidSyscallHandler.class).setLevel(Level.DEBUG);
    }


    // 打印调用栈
    public void printCallStack() {
        System.out.println("call stack ==>");
        emulator.getUnwinder().unwind();
    }

    //========================================= patch指令 ==============================================


    /**
     patch指令:
     offset_addr: 偏移地址
     asmCode： 汇编指令
     例如：asmCode = "subs r0, r2, r3";  patch(0x1000, asmCode);
     */
    public void patch(long offset_addr, String asmCode) {

        KeystoneArchitecture arch = KeystoneArchitecture.Arm;
        if (is64Bit) {
            arch = KeystoneArchitecture.Arm64;
        }

        UnidbgPointer pointer = UnidbgPointer.pointer(this.emulator, module.base + offset_addr);
        Keystone keystone = new Keystone(arch, KeystoneMode.LittleEndian);
        byte[] codeBytes = keystone.assemble(asmCode).getMachineCode();
        pointer.write(codeBytes);
    }


    public void nop64(long start_offset_addr,  long count) {

        KeystoneArchitecture arch = arch = KeystoneArchitecture.Arm64;
        UnidbgPointer pointer = UnidbgPointer.pointer(this.emulator, module.base + start_offset_addr);
        Keystone keystone = new Keystone(arch, KeystoneMode.LittleEndian);
        String asm = "NOP";
        byte[] codeBytes = keystone.assemble(asm).getMachineCode();
        codeBytes = copyByteArrayNTimes(codeBytes, (int)count);
        pointer.write(codeBytes);
    }


    public static byte[] copyByteArrayNTimes(byte[] source, int n) {
        if (source == null || n <= 0) return new byte[0]; // 边界处理

        int sourceLength = source.length;
        byte[] result = new byte[sourceLength * n]; // 目标数组长度 = 原数组长度 × N

        for (int i = 0; i < n; i++) {
            int destPos = i * sourceLength; // 当前复制位置
            System.arraycopy(source, 0, result, destPos, sourceLength);
        }
        return result;
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
     String traceFile = rootPath() + "/trace/moji_func_trace.log";
     PrintStream traceStream = createTraceStream(traceFile);

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


    static String prefix = "";

    /**
     使用：
     String traceFile = rootPath() + "/trace/moji_func_trace.log";
     PrintStream traceStream = createTraceStream(traceFile);
     traceFunction(traceStream);
     */
    public void traceFunction(PrintStream traceStream) {

        emulator.attach().traceFunctionCall(module, new FunctionCallListener() {
            @Override
            public void onCall(Emulator<?> emulator, long callerAddress, long functionAddress) {
                prefix += "  ";
                traceStream.println("\n" + prefix + "|--" + " start caller=" + UnidbgPointer.pointer(emulator, callerAddress) + ", function=" + UnidbgPointer.pointer(emulator, functionAddress));
            }
            @Override
            public void postCall(Emulator<?> emulator, long callerAddress, long functionAddress, Number[] args) {
                prefix = prefix.substring(0, prefix.length() - 2);
                //traceStream.println("end caller=" + UnidbgPointer.pointer(emulator, callerAddress) + ", function=" + UnidbgPointer.pointer(emulator, functionAddress));
            }
        });
    }


    public void traceCount() {

        emulator.getBackend().hook_add_new(new CodeHook() {
            int count = 0;
            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                count += 1;
                System.out.println(count);
            }

            @Override
            public void onAttach(UnHook unHook) {

            }

            @Override
            public void detach() {
            }
        }, module.base, module.base+ module.size, null);
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
