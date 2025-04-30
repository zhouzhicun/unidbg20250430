package zz.tools.aes;

import com.github.unidbg.Emulator;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.memory.MemoryMap;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.utils.Inspector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;


/**
 * 仓库地址：https://github.com/Pr0214/Unidbg_FindKey/
 */

public class AESKeyFinder {

    protected final Emulator<?> emulator;
    protected Backend backend;
    protected List<byte[]> keylist = new ArrayList<>();
    protected List<Long> breakTrace = new ArrayList<>();
    protected byte[] MemoryRegion;

    public AESKeyFinder(Emulator<?> emulator) {
        this.emulator = emulator;
        this.backend = emulator.getBackend();
    }


    // 检索当前时机的内存中是否存在Key
    public boolean searchKeyInMemory() {
        int exist = 0;

        // 1.检索非模块的内存区域
        for (MemoryMap map : emulator.getMemory().getMemoryMap()) {
            if(emulator.getMemory().findModuleByAddress(map.base) == null){
                exist += searchMemory(map.base, map.base + map.size);
            }
        }
        // 2.搜索当前栈
        UnidbgPointer stack = emulator.getContext().getStackPointer();
        long stackstart = stack.toUIntPeer();
        long stackend = emulator.getMemory().getStackBase();
        exist += searchMemory(stackstart, stackend);

        return exist > 0;
    }


    public boolean containsSubArray(List<byte[]> j, byte[] sub) {
        for (byte[] arr : j ) {
            if (Arrays.equals(arr, sub)) {
                return true;
            }
        }
        return false;
    }


    private boolean byteArrayAllZero(byte[] array) {
        int sum = 0;
        for (byte b : array) {
            sum |= b;
        }
        return (sum == 0);
    }



    private int searchMemory(long start, long end) {

        int exist = 0;
        MemoryRegion = backend.mem_read(start, end - start);

        // 1.AES128 轮秘钥判断
        // AES-128密钥扫描（176字节块）
        for (long i = start; i <= end - (11 * 16); i = i+4) {
            // 去除一些潜在的空内存块，判断逻辑：每次从当前索引位置读取4字，即AES128的轮秘钥的一行；共16字节，判断是否为全0.
            byte[] oneBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x10));
            if(byteArrayAllZero(oneBlock)){
                i = i + 0x10 - 4;
            }

            //AES128快速判断，传入0x20字节(每4字一行，共2行；即32字节)
            else if(AESKey.isAes128KeyFastJudge(Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x20)))){

                byte[] BigBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + (11 * 16)));

                //判断是否已包含，如果已包含，则跳过
                if(containsSubArray(keylist, BigBlock)){
                    i = i + (11 * 16) - 4;
                    continue;
                }
                int result = AESKey.IsAes128Key(BigBlock);
                if(result>0){
                    exist++;
                    System.out.println("AES 128 Key Address:0x"+Integer.toHexString((int) i));
                    keylist.add(BigBlock);
                    keylist = new ArrayList<>(new HashSet<>(keylist));
                    if(result == 1){
                        Inspector.inspect(oneBlock, "AES-128 Key(BigEdian)");
                    }else if(result == 3){
                        Inspector.inspect(AESKey.ConvertToLittleEdian(oneBlock), "AES-128 Key(LittleEdian)");
                    }
                }
            }
        }


        // 1.AES192 轮秘钥判断
        for (long i = start; i <= end - (13 * 16); i = i+4) {
            // 去除一些潜在的空内存块，判断逻辑：每次从当前索引位置读取6字，即AES192的轮秘钥的一行；共24字节，判断是否为全0.
            byte[] oneBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x18));
            if(byteArrayAllZero(oneBlock)){
                i = i + 0x18 - 4;
            }

            //AES192快速判断，传入0x30字节(每6字一行，共2行，即48字节)
            else if(AESKey.isAes192KeyFastJudge(Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x30)))){

                byte[] BigBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + (13 * 16)));

                //判断是否已包含，如果已包含，则跳过
                if(containsSubArray(keylist, BigBlock)){
                    i = i + (13 * 16) - 4;
                    continue;
                }
                int result = AESKey.IsAes192Key(BigBlock);
                if(result>0){
                    exist++;
                    System.out.println("AES 192 Key Address:0x"+Integer.toHexString((int) i));
                    keylist.add(BigBlock);
                    keylist = new ArrayList<>(new HashSet<>(keylist));
                    if(result == 1){
                        Inspector.inspect(oneBlock, "AES-192 Key(BigEdian)");
                    }else if(result == 3){
                        Inspector.inspect(AESKey.ConvertToLittleEdian(oneBlock), "AES-192 Key(LittleEdian)");
                    }
                }
            }
        }


        //
        for (long i = start; i <= end - (15 * 16); i = i+4) {
            // 去除一些潜在的空内存块， 判断逻辑：每次从当前索引位置读取8字，即AES256的轮秘钥的一行；共32字节，判断是否为全0.
            byte[] oneBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x20));
            if(byteArrayAllZero(oneBlock)){
                i = i + 0x20 - 4;
            }
            //AES256快速判断，传入0x40字节(每8字一行，共2行，即64字节)
            else if(AESKey.isAes256KeyFastJudge(Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + 0x40)))){
                byte[] BigBlock = Arrays.copyOfRange(MemoryRegion, (int)(i - start) , (int)(i - start + (15 * 16)));

                //判断是否已包含，如果已包含，则跳过
                if(containsSubArray(keylist, BigBlock)){
                    i = i + (15 * 16) - 4;
                    continue;
                }
                int result = AESKey.IsAes256Key(BigBlock);
                if(result>0){
                    exist++;
                    System.out.println("AES 256 Key Address:0x"+Integer.toHexString((int) i));
                    keylist.add(BigBlock);
                    keylist = new ArrayList<>(new HashSet<>(keylist));
                    if(result == 1){
                        Inspector.inspect(oneBlock, "AES-256 Key(BigEdian)");
                    }else if(result == 3){
                        Inspector.inspect(AESKey.ConvertToLittleEdian(oneBlock), "AES-256 Key(LittleEdian)");
                    }
                }
            }
        }
        return exist;
    }

    /**
     * 动态跟踪函数调用（通过IDA导出的函数列表）
     * 实现原理：
     * 1.遍历函数列表，给每个函数添加断点；
     * 2.命中断点时：
     *    2.1 首先计数+1；
     *    2.2 然后判断命中次数是否 > 8, 大于则移除断点；
     *    2.3 拿到函数的返回地址，首先判断该函数返回地址是否已添加到 breakTrace列表中， 如果未添加，则对返回地址下断点；
     *        在函数返回地址的断点中，同样计数，计数 > 8就移除断点；然后调用 searchKeyInMemory() 开启内存搜索AES key。
     */
    public void searchEveryFunction(final long start, final List<String> funcList){
        System.out.println("Searching AES Key Start...");
        for(String fun : funcList){
            final long addr = Long.parseLong(fun.split("!")[0], 16);
            emulator.attach().addBreakPoint(start + addr, new BreakPointCallback() {
                int count = 0;
                @Override
                public boolean onHit(Emulator<?> emulator, long address) {
                    count ++;
                    if(count > 8){
//                        System.out.println("工具函数，去除其函数断点");
                        backend.removeBreakPoint(start + addr);
                        return true;
                    }
                    final long returnAddress = emulator.getContext().getLRPointer().peer;
                    if(!breakTrace.contains(returnAddress)){
                        breakTrace.add(returnAddress);
                        emulator.attach().addBreakPoint(returnAddress, new BreakPointCallback() {
                            @Override
                            public boolean onHit(Emulator<?> emulator, long address) {
                                if(count > 8){
//                                    System.out.println("工具函数，去除其函数返回处断点");
                                    backend.removeBreakPoint(returnAddress);
                                    return true;
                                }else {
                                    if(searchKeyInMemory()){
                                        System.out.println("Generate At Function : 0x"+ Integer.toHexString((int) (address-start)));
                                        System.out.println(">-----------------------------------------------------------------------------<\n");
                                    }
                                    return true;
                                }
                            }
                        });
                    }

                    return true;
                }
            });

        }
    }

    public static List<String> readFuncFromIDA(String path){
        List<String> funcList = new ArrayList<>();
        try {
            // open file to read
            Scanner scanner = new Scanner(new File(path));

            // read until end of file (EOF)
            while (scanner.hasNextLine()) {
                String oneLine = scanner.nextLine().replaceAll("\t", "").replaceAll("0x", "");
                funcList.add(oneLine);
            }

            // close the scanner
            scanner.close();

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return funcList;
    }

}
