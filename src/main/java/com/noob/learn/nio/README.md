com.noob.learn.nio 基础jdk nio 的socket通讯




<P>
总结：需要开启客户端、服务端、服务端维持的通讯管道的模式为unBlock!
/**
 * AbstractSelectableChannel: public final SelectableChannel configureBlocking(boolean block) throws IOException
**/
<p>

ServerSocketChannel: 用于监听客户端的连接，所有客户端连接的父管道.
SocketChannel: 单个客户端的连接管道.
/**
 * SelectableChannel: public final SelectionKey register(Selector sel, int ops) throws ClosedChannelException
**/
Selector: 多路复用器, 通过显示注册各个不同的SelectionKey事件来达到调度：。
<p>
DirectByteBuffer:
不使用JVM堆栈而是通过操作系统来创建内存块用作缓冲区，它与当前操作系统能够更好的耦合，因此能进一步提高I/O操作速度,比较适合读写操作。但是分配直接缓冲区的系统开销很大，因此只有在缓冲区较大并长期存在，或者需要经常重用时，才使用这种缓冲区
p>
HeapByteBuffer: 比较适合创建新的缓冲区，并且重复读写不会太多的应用

<p>
1. 客户端发起的连接操作是异步的，可以通过在多路复用器注册OP_CONNECT等待后续结果，不需要像之前的客户端那样被同步阻塞。
2. SocketChannel的读写操作都是异步的，如果没有可读写的数据它不会同步等待，直接返回，这样I/O通信线程就可以处理其他的链路，不需要同步等待这个链路可用。
3. 线程模型的优化：由于JDK的Selector在Linux等主流操作系统上通过epoll实现，它没有连接句柄数的限制（只受限于操作系统的最大句柄数或者对单个进程的句柄限制），
这意味着一个Selector线程可以同时处理成千上万个客户端连接，而且性能不会随着客户端的增加而线性下降，因此，它非常适合做高性能、高负载的网络服务器。

4. JDK1.7升级了NIO类库，升级后的NIO类库被称为NIO2.0，正式提供了异步文件I/O操作，同时提供了与UNIX网络编程事件驱动I/O对应的AIO.




6. 同一个端口只能被一个ServerSocket绑定监听。 否则报错：
Exception in thread "main" java.net.BindException: Address already in use: bind
	at sun.nio.ch.Net.bind0(Native Method)
	at sun.nio.ch.Net.bind(Unknown Source)
	at sun.nio.ch.Net.bind(Unknown Source)
	at sun.nio.ch.ServerSocketChannelImpl.bind(Unknown Source)
	at java.nio.channels.ServerSocketChannel.bind(Unknown Source)
	at com.noob.learn.nio.NioServer.main(NioServer.java:21)

7. 若服务端下线，则客户端报连接拒绝
java.net.ConnectException: Connection refused: no further information
	at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method) ~[na:1.8.0_201]
	at sun.nio.ch.SocketChannelImpl.finishConnect(Unknown Source) ~[na:1.8.0_201]
	at com.noob.learn.nio.IOHandler.handleMsgFromInput(IOHandler.java:100) ~[classes/:na]
	at com.noob.learn.nio.IOHandler.exectue(IOHandler.java:60) ~[classes/:na]
	at com.noob.learn.nio.NioClient.main(NioClient.java:28) [classes/:na]