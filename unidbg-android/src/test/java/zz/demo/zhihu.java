package zz.demo;


import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneEncoded;
import keystone.KeystoneMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 知乎
 */
public class zhihu extends AbstractJni implements IOResolver {
    private final AndroidEmulator emulator;
    private final DvmClass CryptoTool;
    private final VM vm;
    private final Module module;

    public zhihu() throws IOException {
        emulator = AndroidEmulatorBuilder
                .for32Bit()
                .addBackendFactory(new Unicorn2Factory(true))
                .setProcessName("com.zhihu.android")
                .build();
        Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));
        vm = emulator.createDalvikVM(new File("unidbg-android/apks/zh2/zh9.29.0.apk"));
        vm.setJni(this);
        vm.setVerbose(true);
        emulator.getSyscallHandler().addIOResolver(this);
        DalvikModule dm2 = vm.loadLibrary("bangcle_crypto_tool", true);
        module = dm2.getModule();
        CryptoTool = vm.resolveClass("com.bangcle.CryptoTool");
        dm2.callJNI_OnLoad(emulator);
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


    public void x_zse_96_encrypt() throws IOException {
        List<Object> list = new ArrayList<>(5);
        list.add(vm.getJNIEnv());
        list.add(0);
        byte[] byy1 = {-118,-125,-125,41,41,34,-113,-115,42,35,34,42,-125,-128,40,-125,-125,33,-126,41,40,-118,-117,35,-125,-128,-117,35,42,-115,35,-113};
        byte[] byy2 = {-103,48,58,58,50,52,58,57,-110,-110,58,59,58,-103,-110,-110};
        ByteArray arr1 = new ByteArray(vm,byy1);
        list.add(vm.addLocalObject(arr1));
        list.add(vm.addLocalObject(new StringObject(vm, "541a3a5896fbefd351917c8251328a236a7efbf27d0fad8283ef59ef07aa386dbb2b1fcbba167135d575877ba0205a02f0aac2d31957bc7f028ed5888d4bbe69ed6768efc15ab703dc0f406b301845a0a64cf3c427c82870053bd7ba6721649c3a9aca8c3c31710a6be5ce71e4686842732d9314d6898cc3fdca075db46d1ccf3a7f9b20615f4a303c5235bd02c5cdc791eb123b9d9f7e72e954de3bcbf7d314064a1eced78d13679d040dd4080640d18c37bbde")));
        ByteArray arr2 = new ByteArray(vm,byy2);
        list.add(vm.addLocalObject(arr2));
        Number number = module.callFunction(emulator, 0xa708, list.toArray());
        ByteArray resultArr = vm.getObject(number.intValue());
        System.out.println("result:"+ bytesToHex(resultArr.getValue()));
    }


    public static void main(String[] args) throws IOException {
        zhihu zh = new zhihu();
        zh.patchInstruction();
        zh.x_zse_96_encrypt();
    }


    private void patchInstruction() {
        try (Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.ArmThumb)) {
            KeystoneEncoded assemble = keystone.assemble("nop;nop");
            byte[] machineCode = assemble.getMachineCode();
            emulator.getMemory().pointer(module.base + 0x8EBC).write(0,machineCode,0,machineCode.length);

            KeystoneEncoded encoded = keystone.assemble("mov r3, 1");
            byte[] patchCode = encoded.getMachineCode();
            emulator.getMemory().pointer(module.base + 0x4e70).write(0, patchCode, 0, patchCode.length);
            KeystoneEncoded encoded1 = keystone.assemble("mov r3, 1");
            byte[] patchCode1 = encoded1.getMachineCode();
            emulator.getMemory().pointer(module.base + 0x4e84).write(0, patchCode1, 0, patchCode1.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //=============================== 补环境 ============================================

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature) {
            case "com/secneo/apkwrapper/H->PKGNAME:Ljava/lang/String;": {
                String packageName = vm.getPackageName();
                if (packageName != null) {
                    return new StringObject(vm, packageName);
                }
                break;
            }
            case "com/secneo/apkwrapper/H->ISMPASS:Ljava/lang/String;": {
                return new StringObject(vm, "###MPASS###");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }
    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/app/ActivityThread->getSystemContext()Landroid/app/ContextImpl;": {
                return vm.resolveClass("android/app/ContextImpl").newObject(signature);
            }
            case "android/app/ContextImpl->getPackageManager()Landroid/content/pm/PackageManager;": {
                return vm.resolveClass("android/content/pm/PackageManager").newObject(signature);
            }
            case "java/io/File->getPath()Ljava/lang/String;": {
                System.out.println("PATH:" + dvmObject.getValue());
                if (dvmObject.getValue().equals("android/os/Environment->getExternalStorageDirectory()Ljava/io/File;")) {
                    return new StringObject(vm, "/mnt/sdcard");
                }
            }
            case "java/lang/String->intern()Ljava/lang/String;": {
                String string = dvmObject.getValue().toString();
                return new StringObject(vm, string.intern());
            }
            case "java/lang/Class->getDeclaredFields()[Ljava/lang/reflect/Field;": {
                DvmClass c = (DvmClass) dvmObject;
                System.out.println(c.getClassName());
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }
    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature) {
            case "android/content/pm/PackageInfo->applicationInfo:Landroid/content/pm/ApplicationInfo;": {
                return vm.resolveClass("android/content/pm/ApplicationInfo").newObject(signature);
            }
            case "android/content/pm/ApplicationInfo->sourceDir:Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/com.zhihu.android-XnTWbKh_JrM9gUKdEn_Wug==/base.apk");
            }
            case "android/content/pm/ApplicationInfo->dataDir:Ljava/lang/String;": {
                return new StringObject(vm, "/data/data/com.zhihu.android-XnTWbKh_JrM9gUKdEn_Wug==");
            }
            case "android/content/pm/ApplicationInfo->nativeLibraryDir:Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/com.zhihu.android-XnTWbKh_JrM9gUKdEn_Wug==/lib/arm");
            }
        }
        return super.getObjectField(vm, dvmObject, signature);
    }
    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "android/os/Environment->getExternalStorageDirectory()Ljava/io/File;": {
                return vm.resolveClass("java/io/File").newObject(signature);
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }
    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        if (pathname.equals("/proc/self/cmdline")) {
            return FileResult.success(new ByteArrayFileIO(oflags, pathname, "com.zhihu.android".getBytes()));
        }
        if (pathname.equals("/data/app/com.zhihu.android-XnTWbKh_JrM9gUKdEn_Wug==/base.apk")) {
            return FileResult.success(new SimpleFileIO(oflags, new File("D:\\code\\me\\app\\unidbg-master\\unidbg-android\\src\\test\\resources\\zhihu\\zhihu.apk"), pathname));
        }
        return null;
    }
}
