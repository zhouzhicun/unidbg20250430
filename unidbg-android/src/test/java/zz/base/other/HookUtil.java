package zz.base.other;

import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.Arm64Svc;
import com.github.unidbg.arm.HookStatus;
import com.github.unidbg.arm.backend.*;
import com.github.unidbg.arm.context.RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.hook.HookContext;
import com.github.unidbg.hook.ReplaceCallback;
import com.github.unidbg.hook.hookzz.*;
import com.github.unidbg.hook.whale.IWhale;
import com.github.unidbg.hook.whale.Whale;
import com.github.unidbg.hook.xhook.IxHook;
import com.github.unidbg.linux.android.XHookImpl;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.utils.Inspector;
import com.sun.jna.Pointer;
import unicorn.ArmConst;



/**
 * 参考文档：
 * Unidbg Hook 大全： https://blog.seeflower.dev/archives/67/
 *
 * Unidbg 在Android 上支持的Hook，可以分为两大类：
 * Unidbg 内置的第三方Hook框架，包括xHook/Whale/HookZz(arm32)/Dobby(arm64)
 * Unicorn Hook 以及 Unidbg基于它封装的Console Debugger
 *
 */

/**
 方式1：使用unidbg断点
 */


/**
 * 方式2：使用xHook
 * xHook是爱奇艺开源的Android PLT hook框架，优点是挺稳定好用，缺点是不能Hook Sub_xxx子函数。
 */

/**
 方式3：使用HookZz
 HookZz现在叫Dobby，Unidbg中是HookZz和Dobby是两个独立的Hook库，因为作者认为HookZz在arm32上支持较好，Dobby在arm64上支持较好。
 HookZz是inline hook方案，因此可以Hook Sub_xxx，缺点是短函数可能出bug，受限于inline Hook原理。
 HookZz也可以实现类似于单行断点的Hook，但在Unidbg的Hook大环境下感觉用处不大，不建议使用, 如果需要单行断点，直接使用unidbg的断点就行。
 */

/**
 * 方式4：使用Whale
 * Whale 是一个跨平台的Hook框架，在Andorid Native Hook 上也是inline Hook方案，
 */

/**
 方式5：使用 Unicorn Hook
 如果想对某个函数进行集中的、高强度的、同时又灵活的调试，Unicorn CodeHook是一个好选择。
 比如我想查看目标函数第一条指令的r1，第二条指令的r2，第三条指令的r3，类似于这种需求。
 */

public class HookUtil {

    /**
     方式1：使用unidbg断点
     */
    public void HookByConsoleDebugger(Emulator<?> emulator, Module module, long offsetAddr) {

        emulator.attach().addBreakPoint(module.base + offsetAddr, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {

                //打印入参
                RegisterContext context = emulator.getContext();
                Pointer input = context.getPointerArg(0);
                int length = context.getIntArg(1);
                Pointer buffer = context.getPointerArg(2);
                Inspector.inspect(input.getByteArray(0, length), "base64 input");


                // OnLeave: 给LR下断，打印结果。
                emulator.attach().addBreakPoint(context.getLRPointer().peer, new BreakPointCallback() {
                    @Override
                    public boolean onHit(Emulator<?> emulator, long address) {

                        //打印返回值
                        String result = buffer.getString(0);
                        System.out.println("base64 result:"+result);
                        return true;
                    }
                });
                return true;
            }
        });
    }



    /**
     * 方式2：使用xHook
     * xHook是爱奇艺开源的Android PLT hook框架，优点是挺稳定好用，缺点是不能Hook Sub_xxx子函数。
     */
    public void HookByXhook(Emulator<?> emulator, Module module, String soName,  String funcName){

        IxHook xHook = XHookImpl.getInstance(emulator);
        xHook.register(soName, funcName, new ReplaceCallback() {
            @Override
            public HookStatus onCall(Emulator<?> emulator, HookContext context, long originFunction) {

                Pointer input = context.getPointerArg(0);
                int length = context.getIntArg(1);
                Pointer buffer = context.getPointerArg(2);
                Inspector.inspect(input.getByteArray(0, length), "base64 input");
                context.push(buffer);
                return HookStatus.RET(emulator, originFunction);
            }
            @Override
            public void postCall(Emulator<?> emulator, HookContext context) {

                Pointer buffer = context.pop();
                System.out.println("base64 result:"+buffer.getString(0));
            }
        }, true);
        // 使其生效
        xHook.refresh();
    }




    /**
     方式3：使用HookZz
     HookZz现在叫Dobby，Unidbg中是HookZz和Dobby是两个独立的Hook库，因为作者认为HookZz在arm32上支持较好，Dobby在arm64上支持较好。
     HookZz是inline hook方案，因此可以Hook Sub_xxx，缺点是短函数可能出bug，受限于inline Hook原理。
     HookZz也可以实现类似于单行断点的Hook，但在Unidbg的Hook大环境下感觉用处不大，不建议使用, 如果需要单行断点，直接使用unidbg的断点就行。
     */
    public void HookByHookZz(Emulator<?> emulator, Module module, String funcName){

        IHookZz hookZz = HookZz.getInstance(emulator); // 加载HookZz，支持inline hook
        hookZz.enable_arm_arm64_b_branch(); // 测试enable_arm_arm64_b_branch，可有可无
        hookZz.wrap(module.findSymbolByName(funcName), new WrapCallback<HookZzArm32RegisterContext>() {
            @Override
            public void preCall(Emulator<?> emulator, HookZzArm32RegisterContext context, HookEntryInfo info) {
                Pointer input = context.getPointerArg(0);
                int length = context.getIntArg(1);
                Pointer buffer = context.getPointerArg(2);
                Inspector.inspect(input.getByteArray(0, length), "base64 input");
                context.push(buffer);
            }
            @Override
            public void postCall(Emulator<?> emulator, HookZzArm32RegisterContext context, HookEntryInfo info) {
                Pointer buffer = context.pop();
                System.out.println("base64 result:"+buffer.getString(0));
            }
        });
        hookZz.disable_arm_arm64_b_branch();
    }

    /**
     * 方式4：使用Whale
     * Whale 是一个跨平台的Hook框架，在Andorid Native Hook 上也是inline Hook方案，
     */
    public void HookByWhale(Emulator<?> emulator, Module module){

        IWhale whale = Whale.getInstance(emulator);

        whale.inlineHookFunction(module.findSymbolByName("base64_encode"), new ReplaceCallback() {
            Pointer buffer;
            @Override
            public HookStatus onCall(Emulator<?> emulator, long originFunction) {
                RegisterContext context = emulator.getContext();
                Pointer input = context.getPointerArg(0);
                int length = context.getIntArg(1);
                buffer = context.getPointerArg(2);
                Inspector.inspect(input.getByteArray(0, length), "base64 input");
                return HookStatus.RET(emulator, originFunction);
            }

            @Override
            public void postCall(Emulator<?> emulator, HookContext context) {
                System.out.println("base64 result:"+buffer.getString(0));
            }
        }, true);
    }


    /**
     方式5：使用 Unicorn Hook指令执行
     如果想对某个函数进行集中的、高强度的、同时又灵活的调试，Unicorn CodeHook是一个好选择。
     比如我想查看目标函数第一条指令的r1，第二条指令的r2，第三条指令的r3，类似于这种需求。
     */
    public void HookCodeExec(Emulator<?> emulator, Module module){

        long start = module.base+0x97C;
        long end = module.base+0x97C+0x17A;

        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void hook(Backend backend, long address, int size, Object user) {

                RegisterContext registerContext = emulator.getContext();

                if(address == module.base + 0x97C){
                    int r0 = registerContext.getIntByReg(ArmConst.UC_ARM_REG_R0);
                    System.out.println("0x97C 处 r0:"+Integer.toHexString(r0));
                }
                if(address == module.base + 0x97C + 2){
                    int r2 = registerContext.getIntByReg(ArmConst.UC_ARM_REG_R2);
                    System.out.println("0x97C +2 处 r2:"+Integer.toHexString(r2));
                }
                if(address == module.base + 0x97C + 4){
                    int r4 = registerContext.getIntByReg(ArmConst.UC_ARM_REG_R4);
                    System.out.println("0x97C +4 处 r4:"+Integer.toHexString(r4));
                }
            }

            @Override
            public void onAttach(UnHook unHook) {}

            @Override
            public void detach() {}

        }, start, end, null);

    }


    /**
     方式6：使用 Unicorn Hook内存读写
     */
    public static void hookMemoryAccess(Emulator<?> emulator, long beginAddr, long endAddr) {


        //1.hook读操作
        emulator.getBackend().hook_add_new(new ReadHook() {
            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                byte[] bytes = backend.mem_read(address, size);
                System.out.println(String.format(">>> memory read at 0x%x, block size = 0x%x\n", address, size));
            }

            @Override
            public void onAttach(UnHook unHook) {}

            @Override
            public void detach() {}

        }, beginAddr, endAddr, null);


        //2.hook写操作
        emulator.getBackend().hook_add_new(new WriteHook() {
            @Override
            public void hook(Backend backend, long address, int size, long value, Object user) {
//                byte[] bytes = backend.mem_read(address, size);
//                System.out.println(String.format(">>> memory read at 0x%x, block size = 0x%x\n", address, size));

//                //注意:
//                // read/write hook 的发生时机是读写活动发生前，我们希望在读写活动发生之后做检索，
//                // 这样才能找到最早的数据源，所以这里我选择手动写入。
//                byte[] writeData = new byte[size];
//                for (int i = 0; i < size; i++) {
//                    writeData[i] = (byte)(value >> (8 * i));
//                }
//                backend.mem_write(address, writeData);
            }

            @Override
            public void onAttach(UnHook unHook) {}

            @Override
            public void detach() {}

        }, beginAddr, endAddr, null);
    }


    /**
     * 方式7： hook svc系统调用
     */
    public static void hookSVC(Emulator<?> emulator) {

        SvcMemory svcMemory = emulator.getSvcMemory();
        svcMemory.registerSvc(new Arm64Svc() {
            @Override
            public long handle(Emulator<?> emulator) {
                RegisterContext context = emulator.getContext();
                int svcNumber = context.getIntArg(7);
                System.out.println("Hit custom SVC number: " + svcNumber);
                return 0;
            }
        });
    }


    /**
     * 统计trace指令数
     */

    public void HookCodeCount(Emulator<?> emulator, Module module){

//        long start = module.base+0x97C;
//        long end = module.base+0x97C+0x17A;

//        long count = 0;
//        emulator.getBackend().hook_add_new(new CodeHook() {
//            @Override
//            public void hook(Backend backend, long address, int size, Object user) {
//                count += 1;
//            }
//
//            @Override
//            public void onAttach(UnHook unHook) {
//
//            }
//
//            @Override
//            public void detach() {
//
//            }
//        }, module.base, module.base + module.size, null);

    }





}
