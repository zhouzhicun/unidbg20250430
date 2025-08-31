//package zz.demo;
//import com.github.unidbg.AndroidEmulator;
//import com.github.unidbg.Emulator;
//import com.github.unidbg.Module;
//import com.github.unidbg.arm.backend.Unicorn2Factory;
//import com.github.unidbg.file.FileResult;
//import com.github.unidbg.file.IOResolver;
//import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
//import com.github.unidbg.linux.android.AndroidResolver;
//import com.github.unidbg.linux.android.dvm.*;
//import com.github.unidbg.linux.android.dvm.array.ByteArray;
//import com.github.unidbg.linux.file.ByteArrayFileIO;
//import com.github.unidbg.memory.Memory;
//import okhttp3.Headers;
//import okhttp3.HttpUrl;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okio.Buffer;
//import okio.BufferedSink;
//import org.apache.commons.codec.binary.Base64;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.Objects;
//
//
//public class xhs extends AbstractJni implements IOResolver {
//    private final AndroidEmulator emulator;
//    private final VM vm;
//    public DvmClass XhsHttpInterceptor;
//    public DvmObject<?> xhshttpinterceptorObject;
//    public long cPtr;
//    private final Module module;
//    private Request request;
//    private String url;
//
//
//    xhs() {
//
//        emulator = AndroidEmulatorBuilder.
//                for64Bit().
//                addBackendFactory(new Unicorn2Factory(true)).
//                build();
//        emulator.getSyscallHandler().setEnableThreadDispatcher(true);
//        final Memory memory = emulator.getMemory();
//        memory.setLibraryResolver(new AndroidResolver(23));
//        vm = emulator.createDalvikVM(new File("unidbg-android/src/test/java/com/xhs/files/xhs8.32.0.apk"));
//        vm.setJni(this);
//        vm.setVerbose(true);
//        emulator.getSyscallHandler().addIOResolver(this);
//        DalvikModule dm = vm.loadLibrary("xyass", true);
//        dm.callJNI_OnLoad(emulator);
//
//        module = dm.getModule();
//        System.out.println("module.size"+module.size);
//
//        XhsHttpInterceptor = vm.resolveClass("com/xingin/shield/http/XhsHttpInterceptor");
//        url = "https://edith.xiaohongshu.com/api/sns/v6/homefeed?oid=homefeed_recommend&cursor_score=1713832563.9680&geo=eyJsYXRpdHVkZSI6MC4wMDAwMDAsImxvbmdpdHVkZSI6MC4wMDAwMDB9%0A&trace_id=2ed01cae-5ff5-3a88-8182-66c653799f7f&note_index=32&refresh_type=3&client_volume=0.00&unread_begin_note_id=661bd2fe000000001a00e333&unread_end_note_id=6617bcbd000000000401b33c&unread_note_count=2&preview_ad=&preview_type=&loaded_ad=%7B%22ads_id_list%22%3A%5B%5D%2C%22loaded_ad_pos_list%22%3A%5B%5D%7D&home_ads_id=&user_action=0&personalization=1&is_break_down=0&orientation=portrait_split&last_card_position=32&last_live_position=0&last_live_id=";
//        request = new Request.Builder()
//                .url(url)
//                .addHeader("xy-common-params", "fid=1713832256106ec1fd9193cfe1adb79e559d7b3f01e3&device_fingerprint=2024042308305780b6251b1ce1e184698762381dce9b140145ecfc387fdf53&device_fingerprint1=2024042308305780b6251b1ce1e184698762381dce9b140145ecfc387fdf53&cpu_name=mt6895&gid=7d9f66d92bb655e05c82b5cd03e85b5d88cf9739473593357728d9ab&device_model=phone&launch_id=1713832563&tz=Asia%2FShanghai&channel=XiaomiPreload2022&versionName=8.32.0&deviceId=39923e3c-6c6e-3891-945a-fd03c4796eb3&platform=android&sid=session.1713832266146287782113&identifier_flag=4&t=1713832639&project_id=ECFAAF&build=8320689&x_trace_page_current=explore_feed&lang=zh-Hans&app_id=ECFAAF01&uis=light&teenager=0")
//                .addHeader("xy-direction","93")
//                .build();
//    }
//
//
//    @Override
//    public FileResult<?> resolve(Emulator emulator, String pathname, int oflags) {
//        System.out.println("file open:"+pathname);
//        if(pathname.contains("status")){
//            return FileResult.success(new ByteArrayFileIO(oflags, pathname, "TracerPid:      0".getBytes(StandardCharsets.UTF_8)));
//        }
//        return null;
//    }
//
//
//    public void callinitializeNative(){
//
//        XhsHttpInterceptor.callStaticJniMethod(emulator, "initializeNative()V");
//    };
//
//    public void callinitialize(){
//        xhshttpinterceptorObject = XhsHttpInterceptor.newObject("xhs");
//        cPtr = xhshttpinterceptorObject.callJniMethodLong(emulator, "initialize(Ljava/lang/String;)J", "main");
//    };
//
//    public void callintercept(){
//        DvmObject<?> chain = vm.resolveClass("okhttp3/Interceptor$Chain").newObject(null);
//        xhshttpinterceptorObject.callJniMethodObject(emulator, "intercept(Lokhttp3/Interceptor$Chain;J)Lokhttp3/Response;", chain, cPtr);
//        String shield = request.headers().get("shield");
//        System.out.println("shield:"+shield);
//        if(Objects.equals(shield, "XYAAAAAQAAAAEAAABTAAAAUzUWEe0xG1IbD9/c+qCLOlKGmTtFa+lG434NfuFQTKRBzI2yneNkH53+rOYKz8Mi1cx+2KY3QQwYRWWLN7/z1S011Oc8PZAcZStVDuw6DCul1soQ")){
//            System.out.println("结果正确");
//        }else {
//            System.out.println("结果异常");
//        }
//    }
//
//
//    public static void main(String[] args) {
//        xhs demo = new xhs();
//        demo.callinitializeNative();
//        demo.callinitialize();
//        demo.callintercept();
//    }
//
//
//    //============================== 补环境  ========================================
//
//    @Override
//    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
//        switch (signature){
//            case "com/xingin/shield/http/ContextHolder->sLogger:Lcom/xingin/shield/http/ShieldLogger;":{
//                return vm.resolveClass("com/xingin/shield/http/ShieldLogger").newObject(null);
//            }
//            case "com/xingin/shield/http/ContextHolder->sDeviceId:Ljava/lang/String;":{
//                return new StringObject(vm, "39923e3c-6c6e-3891-945a-fd03c4796eb3");
//            }
//        }
//        return super.getStaticObjectField(vm, dvmClass, signature);
//    }
//
//
//    @Override
//    public void callVoidMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
//        switch (signature){
//            case "com/xingin/shield/http/ShieldLogger->nativeInitializeStart()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->nativeInitializeEnd()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->initializeStart()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->initializedEnd()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->buildSourceStart()V":{
//                return;
//            }
//            case "okhttp3/RequestBody->writeTo(Lokio/BufferedSink;)V":{
//                BufferedSink bufferedSink = (BufferedSink) vaList.getObjectArg(0).getValue();
//                RequestBody requestBody = (RequestBody) dvmObject.getValue();
//                if(requestBody != null){
//                    try {
//                        requestBody.writeTo(bufferedSink);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->buildSourceEnd()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->calculateStart()V":{
//                return;
//            }
//            case "com/xingin/shield/http/ShieldLogger->calculateEnd()V":{
//                return;
//            }
//        }
//        super.callVoidMethodV(vm, dvmObject, signature, vaList);
//    }
//
//    @Override
//    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
//        switch (signature){
//            case "java/nio/charset/Charset->defaultCharset()Ljava/nio/charset/Charset;":{
//                return dvmClass.newObject(Charset.defaultCharset());
//            }
//            case "com/xingin/shield/http/Base64Helper->decode(Ljava/lang/String;)[B":{
//                String input = vaList.getObjectArg(0).getValue().toString();
//                return new ByteArray(vm, Base64.decodeBase64(input));
//            }
//        }
//        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
//    }
//
//    @Override
//    public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
//        switch (signature){
//            // 获取版本信息
//            case "android/content/pm/PackageInfo->versionCode:I":{
//                int versionCode = (int) vm.getVersionCode();
//                System.out.println("versionCode:"+versionCode);
//                return versionCode; //和这个有关
//            }
//        }
//        return super.getIntField(vm, dvmObject, signature);
//    }
//
//    @Override
//    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
//        switch (signature){
//            // 获取 sAppId
//            case "com/xingin/shield/http/ContextHolder->sAppId:I":{
//                return -319115519;
//            }
//        }
//        return super.getStaticIntField(vm, dvmClass, signature);
//    }
//
//    @Override
//    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
//        switch (signature){
//            case "android/content/Context->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;":{
//                return vm.resolveClass("android/content/SharedPreferences").newObject(vaList.getObjectArg(0).getValue().toString());
//            }
//            case "android/content/SharedPreferences->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;":{
//                String tableName = dvmObject.getValue().toString();
//                if(tableName.equals("s")){
//                    String key = vaList.getObjectArg(0).getValue().toString();
//                    System.out.println("get key:"+key);
//                    switch (key){
//                        case "main":{
//                            return new StringObject(vm, "");
//                        }
//                        case "main_hmac":{
//                            return new StringObject(vm, "7hdVv+nZfs5dMhWvQB+p5xeCy/RHp4RpSjXvKvEcvuVBx2YNpW4se3oGydywYMgCW60PmOwZRJHUch42GJWv1IC+KWF65KJ/tGzS2eSkIAqsWEna1k4ASav5AQAzgw0F");
//                        }
//                    }
//                }
//            }
//            case "okhttp3/Interceptor$Chain->request()Lokhttp3/Request;":{
//                return vm.resolveClass("okhttp3/Request").newObject(request);
//            }
//            case "okhttp3/Request->url()Lokhttp3/HttpUrl;":{
//                return vm.resolveClass("okhttp3/HttpUrl").newObject(request.url());
//            }
//            case "okhttp3/HttpUrl->encodedPath()Ljava/lang/String;":{
//                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
//                return new StringObject(vm, httpUrl.encodedPath());
//            }
//            case "okhttp3/HttpUrl->encodedQuery()Ljava/lang/String;":{
//                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
//                return new StringObject(vm, httpUrl.encodedQuery());
//            }
//            case "okhttp3/Request->body()Lokhttp3/RequestBody;":{
//                return vm.resolveClass("okhttp3/RequestBody").newObject(request.body());
//            }
//            case "okhttp3/Request->headers()Lokhttp3/Headers;":{
//                return vm.resolveClass("okhttp3/Headers").newObject(request.headers());
//            }
//            case "okio/Buffer->writeString(Ljava/lang/String;Ljava/nio/charset/Charset;)Lokio/Buffer;": {
//                Buffer buffer = (Buffer) dvmObject.getValue();
//                buffer.writeString(vaList.getObjectArg(0).getValue().toString(), (Charset) vaList.getObjectArg(1).getValue());
//                return dvmObject;
//            }
//            case "okhttp3/Headers->name(I)Ljava/lang/String;":{
//                Headers headers = (Headers) dvmObject.getValue();
//                return new StringObject(vm, headers.name(vaList.getIntArg(0)));
//            }
//            case "okhttp3/Headers->value(I)Ljava/lang/String;":{
//                Headers headers = (Headers) dvmObject.getValue();
//                return new StringObject(vm, headers.value(vaList.getIntArg(0)));
//            }
//            case "okio/Buffer->clone()Lokio/Buffer;":{
//                Buffer buffer = (Buffer) dvmObject.getValue();
//                return vm.resolveClass("okio/Buffer").newObject(buffer.clone());
//            }
//            case "okhttp3/Request->newBuilder()Lokhttp3/Request$Builder;": {
//                Request request = (Request) dvmObject.getValue();
//                return vm.resolveClass("okhttp3/Request$Builder").newObject(request.newBuilder());
//            }
//            case "okhttp3/Request$Builder->header(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;": {
//                Request.Builder builder = (Request.Builder) dvmObject.getValue();
//                builder.header(vaList.getObjectArg(0).getValue().toString(), vaList.getObjectArg(1).getValue().toString());
//                return dvmObject;
//            }
//            case "okhttp3/Request$Builder->build()Lokhttp3/Request;": {
//                Request.Builder builder = (Request.Builder) dvmObject.getValue();
//                request = builder.build();
//                return vm.resolveClass("okhttp3/Request").newObject(request);
//            }
//            case "okhttp3/Interceptor$Chain->proceed(Lokhttp3/Request;)Lokhttp3/Response;": {
//                return vm.resolveClass("okhttp3/Response").newObject(null);
//            }
//        }
//        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
//    }
//
//    @Override
//    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
//        switch (signature){
//            case "okio/Buffer-><init>()V":{
//                return dvmClass.newObject(new Buffer());
//            }
//        }
//        return super.newObjectV(vm, dvmClass, signature, vaList);
//    }
//
//    @Override
//    public int callIntMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
//        switch (signature){
//            case "okhttp3/Headers->size()I":{
//                Headers headers = (Headers) dvmObject.getValue();
//                return headers.size();
//            }
//            case "okio/Buffer->read([B)I":{
//                Buffer buffer = (Buffer) dvmObject.getValue();
//                return buffer.read((byte[]) vaList.getObjectArg(0).getValue());
//            }
//            case "okhttp3/Response->code()I":
//                return 200;
//        }
//        return super.callIntMethodV(vm, dvmObject, signature, vaList);
//    }
//
//}