
## 类的成员变量：
- Emulator<?> emulator：模拟器实例，用来访问内存。
- TreeSet<Long> addressUsed：记录已访问的内存地址，有序的集合。
- Map<Long, Byte> memoryData：保存内存地址到字节的映射，可能是跟踪被修改或读取过的内存。
- Set<String> foundStrings：存储已找到的字符串，避免重复。
- int interval：触发搜索的时间间隔，比如每执行多少块后搜索一次。
- int minLength：字符串的最小长度。
- long count：计数器，记录执行的块数。
- List<long[]> activePlace：维护活动内存区域的范围列表，可能是连续的地址范围。
- PrintWriter output：输出结果到文件。

## 构造函数：
构造函数有两个，主要初始化参数，设置输出文件，然后调用hookReadAndWrite和hookBlock方法。
这两个方法应该是用来Hook内存的读写和执行块，以便监控内存变化。

## hook方法：
1.hookReadAndWrite方法（记录读写的内存地址以及内容）
在hookReadAndWrite方法中，添加了WriteHook和ReadHook的钩子。当有写入或读取操作时，将对应的内存地址和字节记录到memoryData中，并更新activePlace。
比如，写入时，将每个偏移地址的字节存入memoryData，并调用updateActivePlace。读取时，同样记录读取到的字节到memoryData，并更新activePlace。

2.hookBlock方法（对代码块计数，达到一定次数就触发字符串搜索）
在hookBlock方法添加了一个BlockHook钩子，每当执行一个代码块时，计数器count增加，当达到设定的interval时，触发searchString方法进行字符串搜索。

## 字符串搜索
1.searchString方法
searchString方法遍历activePlace中的所有内存范围，对每个范围调用searchStringInRange。

2.searchStringInRange方法
searchStringInRange方法则从memoryData中提取该范围的字节数组，并调用searchVisibleStringsInMemory进行字符串分析。

3.searchVisibleStringsInMemory方法
searchVisibleStringsInMemory方法遍历字节数组，将可打印字符（ASCII 32-126）组成字符串，
遇到不可打印字符或结束时，调用processVisibleString处理；这样可以将连续的ASCII字符提取为字符串。

4.processVisibleString方法
processVisibleString方法检查字符串长度是否符合要求，如果是新的字符串，则记录地址和字符串到输出文件，并添加到foundStrings集合中避免重复。

5.updateActivePlace方法
updateActivePlace方法在每次内存读写时被调用，将新的地址加入addressUsed集合，并检查是否需要合并相邻的地址范围。
比如，当新地址与现有范围的上下边界相邻时，合并这些范围，维护activePlace中的连续区间。

## 总结实现原理：
1. 通过Hook内存的读写操作，记录所有被访问的内存地址及其字节数据到memoryData中。
2. 使用addressUsed集合跟踪所有被访问的地址，并通过updateActivePlace维护连续的内存区域范围，保存在activePlace列表中。
3. 在每次执行一定数量的代码块（由interval控制）后，遍历所有活动内存区域，提取其中的字节数据，查找连续的可打印ASCII字符组成的字符串。
4. 过滤掉长度不足的字符串，并将结果输出到文件，避免重复记录。







实现原理总结
1.内存监控机制
通过ReadHook和WriteHook钩子捕获所有内存读写操作，将访问过的地址及数据记录到memoryData映射中。
使用TreeSet<Long>维护有序的已访问地址集合addressUsed，便于快速查找相邻地址。

2.动态区间维护
通过updateActivePlace方法动态合并相邻地址，将离散的访问地址合并为连续的内存区间（activePlace列表），大幅减少后续搜索范围。

3.定时触发机制
通过BlockHook在每执行指定数量（interval）的代码块后触发字符串搜索，平衡性能与实时性。

4.字符串提取算法
在活跃内存区间内扫描连续ASCII可打印字符（32-126），使用StringBuilder积累字符串。
遇到非打印字符或扫描结束时，验证字符串长度并通过foundStrings集合去重后输出。

高效输出管理
使用PrintWriter直接写入文件，避免内存堆积。
每次发现新字符串立即刷新输出流（flush()），确保实时性。

该方案通过Hook技术实现细粒度内存监控，结合区间合并算法优化搜索范围，在保证功能性的同时最大限度提升搜索效率。