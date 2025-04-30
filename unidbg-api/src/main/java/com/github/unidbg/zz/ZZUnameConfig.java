package com.github.unidbg.zz;

import java.util.HashMap;


public class ZZUnameConfig {

    public static class UnameKey {

        public static final String sysname = "sysName";
        public static final String machineArm32 = "machineArm32";
        public static final String machineArm64 = "machineArm64";
        public static final String nodename = "nodename";
        public static final String domainname = "domainname";
        public static final String release = "release";
        public static final String version = "version";

    }

    public static class UnameValue {

        //默认配置
        public static String sysname = "Linux";
        public static String machineArm32 = "armv7l";
        public static String machineArm64 = "aarch64";
        public static String nodename = "localhost";
        public static String domainname = "localdomain";
        public static String release = "4.14.243-gff8eae656fe6-ab8007944";
        public static String version = "#1 SMP PREEMPT Thu Dec 16 11:39:02 UTC 2021";
    }

    /**
     * 只更新release和 version值
     * @param release
     * @param version
     */
    public static void updateUname(String release, String version) {
        UnameValue.release = release;
        UnameValue.version = version;
    }

    /**
     * 根据传入的字典更新多个值
     * @param configMap
     */
    public static void updateUname(HashMap<String, String> configMap) {
        if(configMap == null || configMap.isEmpty()) {
            return;
        }

        for (String key : configMap.keySet()) {
            String value = configMap.get(key);
            switch (key) {
                case UnameKey.sysname: {
                    UnameValue.sysname = value;
                    break;
                }
                case UnameKey.machineArm32: {
                    UnameValue.machineArm32 = value;
                    break;
                }
                case UnameKey.machineArm64: {
                    UnameValue.machineArm64 = value;
                    break;
                }
                case UnameKey.nodename: {
                    UnameValue.nodename = value;
                    break;
                }
                case UnameKey.domainname: {
                    UnameValue.domainname = value;
                    break;
                }
                case UnameKey.release: {
                    UnameValue.release = value;
                    break;
                }
                case UnameKey.version: {
                    UnameValue.version = value;
                    break;
                }
            }
        }

    }
}
