package zz.app.adjust;

import com.github.unidbg.Emulator;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.SystemService;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.zz.ZZFixConfig;
import zz.base.BaseJni;

import java.io.File;
import java.io.PrintStream;
import java.security.*;
import java.util.*;


import zz.base.other.StringUtil;

import javax.crypto.Mac;
import java.util.ArrayList;
import java.util.List;


public class Adjust extends BaseJni implements IOResolver<AndroidFileIO> {

    Adjust() {

//        //================= 基本配置 ===========================
//        public boolean is64Bit;             //是否ARM64     //项目名
//        public String projectName;          //项目名
//        public String apkName;              //apk文件名
//        public String bundleName;           //app包名
//        public String soName;               //so的名字，掐头趣味，例如: libSinger.so，则传入Singer
//        public String clsName;              //接口类

        //1.App初始化
        this.is64Bit = true;
        this.projectName = "adjust";
        this.apkName = "cpmm2dev-adjust.apk";
        this.bundleName = "com.game.cpdev.mm2.cpmm2dev";
        this.soName = "signer";
        this.clsName = "com.adjust.sdk.sig.NativeLibHelper";




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

        emulator.getSyscallHandler().addIOResolver(this);

        ZZFixConfig.enableFixAll(false);

    }

    public static void main(String[] args) {

        zz.app.adjust.Adjust test = new zz.app.adjust.Adjust();
        test.call_onResume();
        test.call_sign();

    }


    public void call_onResume() {

        //traceCount();
        String traceFile = rootPath() + "/trace/adjust_onResume_func_trace.log";
        PrintStream traceStream = createTraceStream(traceFile);
        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
        emulator.traceRead().setRedirect(traceStream);
        emulator.traceWrite().setRedirect(traceStream);
        traceFunction(traceStream);

        List<Object> params = new ArrayList<>(10);
        params.add(vm.getJNIEnv());
        params.add(0);

        module.callFunction(emulator, 0x9DC20L, params.toArray());

    }

    public void patch() {
        patch(0x18284, "mov x0, 0");
        //patch(0xC8A40, "NOP");
        addBreakpoint(0xC8A40);
    }


    public void call_sign() {


        System.err.println("开始 call sign: ");

        patch();

        try {
            //Security.addProvider(new com.sun.crypto.provider.SunJCE()); // 需要确保该类在classpath中
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        } catch (Exception e) {
            System.err.println("add Provider error,  e = " + e);

        }


        //traceCount();

        String traceFile = rootPath() + "/trace/adjust_nSign_func_trace.log";
        PrintStream traceStream = createTraceStream(traceFile);
//        emulator.traceCode(module.base, module.base + module.size).setRedirect(traceStream);
        traceFunction(traceStream);


        List<Object> params = new ArrayList<>(10);
        params.add(vm.getJNIEnv());
        params.add(0);

        DvmObject context = vm.resolveClass("android.content.Context").newObject(null);
        params.add(vm.addLocalObject(context));

        HashMap<String, Object> map = new HashMap<>();
        map.put("gps_adid_attempt", "1");
        map.put("api_level", 29);
        map.put("app_version", "2.2.0");
        map.put("app_token", "6bc0rvg981vk");
        map.put("created_at", "2025-09-01T10:16:10.413Z+0800");
        map.put("wait_total", "0.0");
        map.put("device_type", "phone");
        map.put("gps_adid", "b7feee99-cf1b-4d49-b7ad-10ca1ad1abe1");
        map.put("foreground", 1);
        map.put("google_app_set_id", "031c57cc-a2a2-01bb-e697-d236943475e9");
        map.put("device_name", "device_name");
        map.put("needs_response_details", 1);
        map.put("retry_count", 0);
        map.put("last_error", 0);
        map.put("gps_adid_src", "service");
        map.put("wait_time", "0.0");
        map.put("os_version", "10");
        map.put("first_error", 0);
        map.put("android_uuid", "070da02e-b2de-4f19-9943-9a3034768730");
        map.put("sent_at", "2025-09-01T10:16:10.478Z+0800");
        map.put("offline_mode_enabled", 0);
        map.put("environment", "production");
        map.put("attribution_deeplink", 1);
        map.put("package_name", "com.game.cpdev.mm2.cpmm2dev");
        map.put("os_name", "android");
        map.put("ui_mode", 1);
        map.put("activity_kind", "third_party_sharing");
        map.put("client_sdk", "android5.0.0");

        HashMap<String, Object> subMap = new HashMap<>();
        HashMap<String, Object> subMap2 = new HashMap<>();
        subMap2.put("ad_user_data", "1");
        subMap2.put("ad_personalization", "1");
        subMap.put("google_dma", subMap2);
        map.put("granular_third_party_sharing_options", subMap);

        map.put("enqueue_size", 0);
        map.put("tracking_enabled", 1);

        DvmObject paramsMap =  vm.resolveClass("java.util.Map").newObject(map);
        params.add(vm.addLocalObject(paramsMap));

        byte[] barr = {70,105,105,-121,-103,-11,-111,107,61,36,-98,-108,106,-76,-50,-31,-113,39,102,-32,21,-77,-76,6,37,-124,25,-68,115,63,-64,-73};
        params.add(vm.addLocalObject(new ByteArray(vm, barr)));

        params.add(29);

        Number number = module.callFunction(emulator, 0x9AFC0L, params.toArray());

        DvmObject resultObj = vm.getObject(number.intValue());
        if (resultObj != null) {
            byte[] result = (byte[])resultObj.getValue();
            if(result != null) {
                System.err.println("sign = " + StringUtil.bytesToHex(result));
            } else {
                System.err.println("sign = null 1111111111");
            }
        } else {
            System.err.println("sign = null 2222222222");
        }






//        String json = "{gps_adid_attempt=1, api_level=29, app_version=2.2.0, app_token=6bc0rvg981vk, created_at=2025-09-01T10:16:10.413Z+0800, wait_total=0.0, device_type=phone, gps_adid=b7feee99-cf1b-4d49-b7ad-10ca1ad1abe1, " +
//                "foreground=1, google_app_set_id=031c57cc-a2a2-01bb-e697-d236943475e9, device_name=Pixel 3a XL, needs_response_details=1, retry_count=0, " +
//                "last_error=0, gps_adid_src=service, wait_time=0.0, os_version=10, first_error=0, android_uuid=070da02e-b2de-4f19-9943-9a3034768730, " +
//                "sent_at=2025-09-01T10:16:10.478Z+0800, offline_mode_enabled=0, environment=production, attribution_deeplink=1, package_name=com.game.cpdev.mm2.cpmm2dev, os_name=android, ui_mode=1, " +
//                "activity_kind=third_party_sharing, client_sdk=android5.0.0, granular_third_party_sharing_options={\"google_dma\":{\"ad_user_data\":\"1\",\"ad_personalization\":\"1\"}}, enqueue_size=0, tracking_enabled=1}";

        // arg3
//        String input = "{\"common\":{\"platform\":\"Android\",\"identifier\":\"\",\"app_version\":\"1009087802\",\"os_version\":\"31\",\"device\":\"Pixel 4\",\"brand\":\"google\",\"pid\":\"5068\",\"language\":\"CN\",\"uid\":\"968403975009353785\",\"uaid\":\"968403975172931586\",\"width\":1080,\"height\":2236,\"package_name\":\"com.moji.mjweather\",\"amp\":\"1725432467598\",\"locationcity\":1,\"current_city\":33,\"token\":\"0b3d96327296374be7af09cf92d92295\",\"vip\":\"0\",\"weather_tab_style\":0,\"giuid\":\"gtc_97da812cf686a7a19d79e40fc4b97d8232\",\"smid\":\"DUwXaIhvLQDepEEOAgqF5yLJ-5V4vD8Dpk86\",\"security_request\":0,\"net\":\"wifi\"},\"params\":{\"mobile\":\"EdiOaYg8uVwFTpYfiKiFOg==\",\"is_sercret\":1}}";
//        params.add(vm.addLocalObject(new StringObject(vm, input)));
//
//        Number number = module.callFunction(emulator, 0x3D1A0L, params.toArray());
//        String result = vm.getObject(number.intValue()).getValue().toString();
//        return result;

    }


    //================================== 补环境 ======================================

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {

        switch (signature) {
            case "android/content/pm/PackageInfo->signingInfo:Landroid/content/pm/SigningInfo;": {
                return vm.resolveClass("android/content/pm/SigningInfo").newObject(null);
            }
            case "android/content/pm/ApplicationInfo->publicSourceDir:Ljava/lang/String;": {
                return new StringObject(vm, "/data/app/~~CjCZEY20cERJuxSWAPD_dQ==/com.game.cpdev.mm2.cpmm2dev-J8CyVaZ7Bk3r_v8HoHfecg==/base.apk");
            }

        }

        return super.getObjectField(vm, dvmObject, signature);
    }



//    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
//        if ("android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;".equals(signature) &&
//                dvmObject instanceof PackageInfo) {
//            PackageInfo packageInfo = (PackageInfo) dvmObject;
//            if (packageInfo.getPackageName().equals(vm.getPackageName())) {
//                CertificateMeta[] metas = vm.getSignatures();
//                if (metas != null) {
//                    Signature[] signatures = new Signature[metas.length];
//                    for (int i = 0; i < metas.length; i++) {
//                        signatures[i] = new Signature(vm, metas[i]);
//                    }
//                    return new ArrayObject(signatures);
//                }
//            }
//        }



    @Override
    public boolean callBooleanMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/content/pm/SigningInfo->hasMultipleSigners()Z": {
                return false;
            }
            case "java/util/Map->containsKey(Ljava/lang/Object;)Z": {
                Map map = (Map)dvmObject.getValue();
                String key = (String) varArg.getObjectArg(0).getValue();

                return map.containsKey(key);

                //return true;

            }
        }
        return super.callBooleanMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "android/content/pm/SigningInfo->getApkContentsSigners()[Landroid/content/pm/Signature;": {
                // 获取 Signature 类
                DvmClass SignatureClass = vm.resolveClass("android/content/pm/Signature");
                // 伪造签名数据（示例使用 Base64 编码的证书）
                String signature_base64 = "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKkedxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjAsb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/CxURaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJEqO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/Tgt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/yzKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCEyj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1SKMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifKZ0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3WfMBEmh/9iFBDAaTCK";
                byte[] fakeCertData = Base64.getDecoder().decode(signature_base64); //Base64.decode(signature_base64, Base64.DEFAULT);
                DvmObject<?> fakeSignature = SignatureClass.newObject(fakeCertData);

                // 构造签名数组（支持多签名）
                DvmObject<?>[] signatures = new DvmObject[]{fakeSignature};
                return new ArrayObject(signatures);
            }

            case "android/content/pm/Signature->toByteArray()[B": {
                String signature_base64 = "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKkedxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjAsb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/CxURaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJEqO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/Tgt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/yzKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCEyj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1SKMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifKZ0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3WfMBEmh/9iFBDAaTCK";
                byte[] fakeCertData = Base64.getDecoder().decode(signature_base64); //Base64.decode(signature_base64, Base64.DEFAULT);
                return new ByteArray(vm, fakeCertData);
            }

            case "java/security/MessageDigest->digest()[B":{
                MessageDigest messageDigest = (MessageDigest) dvmObject.getValue();
                //ByteArray array = varArg.getObjectArg(0);
                //assert array != null;
                return new ByteArray(vm, messageDigest.digest());
            }

            case "android/content/Context->getSystemService(Ljava/lang/String;)Ljava/lang/Object;": {
                StringObject serviceName = varArg.getObjectArg(0);
                assert serviceName != null;
                System.out.println(serviceName.toString());
                return new SystemService(vm, serviceName.getValue());
            }

            case "android/hardware/SensorManager->getSensorList(I)Ljava/util/List;": {
                int type = varArg.getIntArg(0);

                System.out.println("getSensorList, type = " + type);   // type = -1, 表示 type_ALL
                DvmObject<?> listObject = vm.resolveClass("java.util.List").newObject(getSensorList());
                return listObject;

            }

            case "java/util/List->get(I)Ljava/lang/Object;": {
                int index = varArg.getIntArg(0);
                List<Object> list = (List<Object>)dvmObject.getValue();
                DvmObject sensorObj = (DvmObject)list.get(index);

                return sensorObj;
            }
            case "android/hardware/Sensor->getName()Ljava/lang/String;": {
                zz.app.adjust.Sensor temp = (Sensor)dvmObject.getValue();
                String name = temp.getName();
                return new StringObject(vm, name);
            }
            case "android/hardware/Sensor->getVendor()Ljava/lang/String;": {
                zz.app.adjust.Sensor temp = (Sensor)dvmObject.getValue();
                String vendor = temp.getVendor();
                return new StringObject(vm, vendor);
            }
            case "android/view/WindowManager->getDefaultDisplay()Landroid/view/Display;": {
                return vm.resolveClass("android/view/Display").newObject(null);
            }
            case "java/util/Map->get(Ljava/lang/Object;)Ljava/lang/Object;": {
                Map tempMap = (Map)dvmObject.getValue();
                Object key = (Object) varArg.getObjectArg(0);
                Object val = tempMap.get(key);
                DvmObject<?> obj = ProxyDvmObject.createObject(vm, val);  //new DvmObject(val);
                return obj;
            }
            case "java/security/KeyStore->getKey(Ljava/lang/String;[C)Ljava/security/Key;": {

                String args = varArg.formatArgs();
                System.err.println("args = " + args);
                KeyStore store = (KeyStore) dvmObject.getValue();

                String  str = (String) varArg.getObjectArg(0).getValue();    // str = "key2"
                DvmObject<?> tempBytes = varArg.getObjectArg(1);
                char[] resultBytes = null;
                if (tempBytes != null) {
                    resultBytes = (char[]) varArg.getObjectArg(1).getValue();  // bytes = null
                }

                try {
                    Key key = store.getKey(str, resultBytes);
                    DvmObject<?> keyObj = vm.resolveClass("java/security/Key;").newObject(key);
                    return keyObj;
                } catch (Exception e) {
                    System.err.println("store.getKey()  error = " + e);
                    return vm.resolveClass("java/security/Key;").newObject(null);
                }

                //Object val = tempMap.get(key);
            }

            case "java/util/Map->toString()Ljava/lang/String;": {
                Map tempMap = (Map)dvmObject.getValue();
                String str = tempMap.toString();
                System.err.println("map.toString() = " + str);
                return new StringObject(vm, str);
            }

            case "java/lang/String->getBytes()[B": {
                String str = (String) dvmObject.getValue();
                System.err.println("string.getBytes(), str = " + str);
                byte[] bytes = str.getBytes();
                return new ByteArray(vm, bytes);
            }
            case "javax/crypto/Mac->doFinal()[B": {

//                Mac tempMac = (Mac)dvmObject.getValue();
//                byte[] bytes = tempMac.doFinal();
//                System.err.println("Mac.init(), result = " + StringUtil.bytesToHex(bytes));

                //重点： 造一个假的 mac_dofinal结果
                byte[] resultBytes = StringUtil.hexToBytes("de05191840bc291cc431d19abccb7eba1bbfd92ee5f6f2f05a51036008592e8b");
                return new ByteArray(vm, resultBytes);
            }

            case "android/content/pm/PackageManager->getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;": {
                String str = (String) varArg.getObjectArg(0).getValue();
                int type = (int)varArg.getIntArg(1);
                System.out.println("getApplicationInfo(str), str = " + str + "; type = " + type);
                return vm.resolveClass("android/content/pm/ApplicationInfo").newObject(null);
            }
            case "java/util/Map->remove(Ljava/lang/Object;)Ljava/lang/Object;": {
                Map map = (Map)dvmObject.getValue();
                String key = (String) varArg.getObjectArg(0).getValue();
                Object obj = map.remove(key);
                return ProxyDvmObject.createObject(vm, obj);
            }


        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "android/util/DisplayMetrics-><init>()V": {
                return vm.resolveClass("android/util/DisplayMetrics").newObject(null);
            }
        }
        return super.newObject(vm, dvmClass, signature, varArg);
    }



    List<DvmObject<?>> getSensorList() {

        List<Sensor> sensors = new ArrayList<>();
        sensors.add(new Sensor("LSM6DSR Accelerometer", "STMicro", 142856, 1L, 156.9064, 0.0047856453, 0.17, 5000L));
        sensors.add(new Sensor("LIS2MDL Magnetometer", "STMicro", 262, 2L, 4915.2, 0.01, 0.2, 10000L));
        sensors.add(new Sensor("LSM6DSR Gyroscope", "STMicro", 142856, 4L, 34.905033, 0.0012216945, 0.55, 5000L));
        sensors.add(new Sensor("TMD3702V Ambient Light Sensor", "AMS", 1, 5L, 1.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("BMP380 Pressure Sensor", "Bosch", 8709, 6L, 1249.9998, 0.0017, 0.7, 40000L));
        sensors.add(new Sensor("TMD3702V Proximity Sensor (wake-up)", "AMS", 1, 8L, 5.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("LIS2MDL Magnetometer-Uncalibrated", "STMicro", 262, 14L, 4915.2, 0.01, 0.2, 10000L));
        sensors.add(new Sensor("LSM6DSR Gyroscope-Uncalibrated", "STMicro", 142856, 16L, 34.905033, 0.0012216945, 0.55, 5000L));
        sensors.add(new Sensor("LSM6DSR Accelerometer-Uncalibrated", "STMicro", 142856, 35L, 156.9064, 0.0047856453, 0.17, 5000L));
        sensors.add(new Sensor("MAX11261 Edge-Detect Sensor", "Maxim", 1, 33172001L, 1.0, 0.01, 0.001, 10000L));
        sensors.add(new Sensor("LSM6DSR Temperature", "STMicro", 142856, 33172002L, 85.0, 0.0039, 0.24, 200000L));
        sensors.add(new Sensor("BMP380 Temperature", "Bosch", 8709, 33172003L, 1.0, 0.01, 0.3, 200000L));
        sensors.add(new Sensor("LIS2MDL Temperature", "STMicro", 262, 33172004L, 1.0, 0.01, 0.001, 10000L));
        sensors.add(new Sensor("camera v-sync 0", "Google", 1, 33172005L, 1.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("camera v-sync 1", "Google", 1, 33172005L, 1.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("camera v-sync 2", "Google", 1, 33172005L, 1.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("TMD3702V Color Sensor", "AMS", 1, 33172008L, 1.0, 0.01, 0.001, 100000L));
        sensors.add(new Sensor("VD6282 Rear Light", "STMicro", 1, 33172009L, 1000000.0, 0.001, 0.001, 1000000L));
        sensors.add(new Sensor("Combo Light", "Google", 2, 33172010L, 1.0, 0.01, 0.001, 0L));
        sensors.add(new Sensor("Binned Brightness", "Google", 1, 65541L, 255.0, 1.0, 0.2, 1000000L));
        sensors.add(new Sensor("Device Pickup Sensor", "Google", 1, 25L, 1.0, 1.0, 0.25, -1L));
        sensors.add(new Sensor("Proximity Gated Single Tap Gesture", "Google", 1, 65547L, 1.0, 1.0, 0.001, -1L));
        sensors.add(new Sensor("Double Twist", "Google", 1, 65537L, 1.0, 1.0, 1.0, 0L));
        sensors.add(new Sensor("Front Camera Light", "Google", 1, 65546L, 1000000.0, 0.001, 0.2, 1000000L));
        sensors.add(new Sensor("Game Rotation Vector Sensor", "Google", 1, 15L, 1.0, 1.0E-5, 1.0, 5000L));
        sensors.add(new Sensor("Geomagnetic Rotation Vector Sensor", "Google", 1, 20L, 1.0, 1.0E-5, 1.0, 5000L));
        sensors.add(new Sensor("Gravity Sensor", "Google", 1, 9L, 9.81, 1.0E-5, 1.0, 5000L));
        sensors.add(new Sensor("Linear Acceleration Sensor", "Google", 1, 10L, 156.96, 1.0E-5, 1.0, 20000L));
        sensors.add(new Sensor("Orientation Sensor", "Google", 1, 3L, 360.0, 1.0E-5, 1.0, 5000L));
        sensors.add(new Sensor("Rotation Vector Sensor", "Google", 1, 11L, 1.0, 1.0E-5, 1.0, 5000L));
        sensors.add(new Sensor("Significant Motion", "Google", 1, 17L, 1.0, 1.0, 0.25, -1L));
        sensors.add(new Sensor("Step Counter", "Google", 1, 19L, 1.8446744E19, 1.0, 0.1, 0L));
        sensors.add(new Sensor("Step Detector", "Google", 1, 18L, 1.0, 1.0, 0.1, 0L));
        sensors.add(new Sensor("Tilt Sensor", "Google", 1, 22L, 1.0, 1.0, 0.25, 0L));
        sensors.add(new Sensor("Device Orientation", "Google", 1, 27L, 3.0, 1.0, 1.0, 0L));
        sensors.add(new Sensor("Device Orientation Debug", "Google", 1, 65548L, 156.96, 1.0E-5, 1.0, 0L));
        sensors.add(new Sensor("Rotation preindication", "Google", 1, 65553L, 3.0, 1.0, 1.0, 0L));

        DvmClass SensorClass = vm.resolveClass("android/hardware/Sensor");

        List<DvmObject<?>> resultSensors = new ArrayList<>();
        for (int i = 0; i < sensors.size(); i++) {
            DvmObject<?> sensorObj = SensorClass.newObject(sensors.get(i));
            resultSensors.add(sensorObj);
        }
        return resultSensors;
    }




    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature) {
            case "java/security/MessageDigest->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;": {
                StringObject type = varArg.getObjectArg(0);
                System.out.println("====input:"+type);
                try {
                    return dvmClass.newObject(MessageDigest.getInstance(type.getValue()));
                } catch (NoSuchAlgorithmException e) {
                    throw new IllegalStateException(e);
                }
            }
            case "java/security/KeyStore->getInstance(Ljava/lang/String;)Ljava/security/KeyStore;": {
                String alg = (String)varArg.getObjectArg(0).getValue();
                System.err.println("keyStore.getInstance(), alg = " + alg);
                try {
                    //KeyStore store = (KeyStore)dvmClass.getValue();
                    if (alg.equals("AndroidKeyStore")) {
                        alg = "BKS";
                    }
                    KeyStore store = KeyStore.getInstance(alg);
                    return vm.resolveClass("java/security/KeyStore").newObject(store);
                } catch (Exception e) {
                    System.err.println("KeyStore->getInstance error,  e = " + e);
                    //throw new RuntimeException(e);
                }
                return vm.resolveClass("java/security/KeyStore").newObject(null);

            }
            case "javax/crypto/Mac->getInstance(Ljava/lang/String;)Ljavax/crypto/Mac;": {
                String alg = (String)varArg.getObjectArg(0).getValue();
                System.err.println("crypto.Mac.getInstance(), alg = " + alg);
                try {
                    Mac macAlg = Mac.getInstance(alg);
                    return vm.resolveClass("javax/crypto/Mac").newObject(macAlg);
                } catch (Exception e) {
                    System.out.println("Mac.getInstance(), e = " + e);
                }
                return vm.resolveClass("javax/crypto/Mac").newObject(null);
            }
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public void callVoidMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {

        switch (signature) {
            case "java/security/MessageDigest->update([B)V": {
                MessageDigest messageDigest = (MessageDigest) dvmObject.getValue();
                ByteArray array = varArg.getObjectArg(0);
                messageDigest.update(array.getValue());
                return;
            }
            case "android/view/Display->getMetrics(Landroid/util/DisplayMetrics;)V": {
                return;
            }
            case "java/security/KeyStore->load(Ljava/security/KeyStore$LoadStoreParameter;)V": {
                KeyStore.LoadStoreParameter parameter = null;
                if(varArg.getObjectArg(0) != null) {
                    parameter =  (KeyStore.LoadStoreParameter)varArg.getObjectArg(0).getValue();
                    System.err.println("loadStoreParameter = " + parameter.toString());
                } else {
                    System.err.println("loadStoreParameter is null ");
                }


                KeyStore store = (KeyStore) dvmObject.getValue();
                try{
                    store.load(parameter);
                } catch (Exception e) {
                    System.err.println(" store.load() failed, -----> e = " + e);
                    throw new RuntimeException(e);
                }

                return;
            }
            case "javax/crypto/Mac->init(Ljava/security/Key;)V": {
                Key tempKey = (Key)varArg.getObjectArg(0).getValue();
                Mac tempMac = (Mac)dvmObject.getValue();
                System.err.println("Mac.init(), tempMac = " + tempMac + "; key = " + tempKey);

                try {
                    tempMac.init(tempKey);
                } catch (InvalidKeyException e) {
                    System.err.println("mac.init(); e = " +  e);
                }

                return;
            }
            case "javax/crypto/Mac->update([B)V": {
                byte[] tempBytes = (byte[])varArg.getObjectArg(0).getValue();
                Mac tempMac = (Mac)dvmObject.getValue();

//                try {
//                    String str = StringUtil.bytesToHex(tempBytes);
//                    System.err.println("Mac.update(), tempMac = " + tempMac + "; tempBytes.hex = " + str);
//                    tempMac.update(tempBytes);
//                } catch (IllegalStateException e) {
//                    System.err.println("mac.update(); e = " +  e);
//                }

                return;
            }

        }
        super.callVoidMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature) {
            case "android/util/DisplayMetrics->widthPixels:I": {
                return 1080;
            }
            case "android/util/DisplayMetrics->heightPixels:I": {
                return 2280;
            }
        }
        return super.getIntField(vm, dvmObject, signature);
    }

    @Override
    public int callIntMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature) {
            case "java/util/List->size()I": {
                List list = (List) dvmObject.getValue();
                return list.size();
            }
        }
        return super.callIntMethod(vm, dvmObject, signature, varArg);
    }


    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {

        System.err.println("resolve pathname = " + pathname);
        if (pathname.equals("/proc/self/cmdline")) {
            return FileResult.success(new ByteArrayFileIO(oflags, pathname, "com.game.cpdev.mm2.cpmm2dev".getBytes()));
        }
        if(pathname.equals(("/proc/self/fd"))) {

            printCallStack();
            //String simulatedFdList = "0\n1\n2\n3\n"; // 模拟有0,1,2,3四个文件描述符

            /**
             *
             * resolve pathname = /proc/self/fd
             * [0x012150000][0x012172850][     libc.so][0x22850] open + 0x80
             * [0x012150000][0x01216eec4][     libc.so][0x1eec4] opendir + 0x14
             * [0x012000000][0x01209fa5c][libsigner.so][0x9fa5c]
             * [0x012000000][0x012018284][libsigner.so][0x18284]
             * [0x012000000][0x01209ae7c][libsigner.so][0x9ae7c]
             * */

            // 将模拟的目录内容返回为一个“文件”
            return FileResult.success(new SimpleFileIO(oflags, new File(" C:\\Users\\aoemo\\Desktop\\unidbg20250430\\unidbg-android\\src\\test\\java\\zz\\app\\adjust\\fd"), pathname));
        }
        if (pathname.equals("/data/app/~~CjCZEY20cERJuxSWAPD_dQ==/com.game.cpdev.mm2.cpmm2dev-J8CyVaZ7Bk3r_v8HoHfecg==/base.apk")) {
            return FileResult.success(new SimpleFileIO(oflags, new File("C:\\Users\\zzc\\Desktop\\unidbg20250430\\unidbg-android\\src\\test\\java\\zz\\app\\adjust\\cpmm2dev-adjust.apk"), pathname));
        }
        return null;
    }


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
