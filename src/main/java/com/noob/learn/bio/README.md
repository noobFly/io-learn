com.noob.learn.bio 基础socket实现阻塞IO通讯

两次阻塞 1、等待连接  ServerSocket.accept() 2、 等待数据读取 read、readLine...
<P>
总结：
<p>
1.
单线程处理(BioServer.requestHandler)的阻塞是由于单线程导致的，与阻塞IO的真实原因不是一个概念。
<p>
2.
<p>
无论于服务端还是客户端，当Socket与ServerSocket连接上后，都是通过 "Socket"的InputStream的读入与OutputStream的写出来实现消息的通讯。(例子中的这部分可以抽取公共模块)
<p>
InputStream中读取数据的方式要与OutputStream中的输出数据相匹配才能有效快速的解析出数据。
读取方式可以不同，但相同的是解析（读取）数据时阻塞；不同读取方式在客户端socket正常关闭OutputStream与非正常断开的呈现不一样。
具体参看RequestHandler中处理reqesut, 推荐使用handleWithoutLineBreak();
<p>
3.
<p>
OutputStream： 在buffer中，如果消息的字节小于buffer容量，是不会立刻推送的，只有输出流中的缓冲区填满时，输出流才真正推送消息。
<p>
flush()方法可以强迫输出流(或缓冲的流)发送数据，即使此时缓冲区还没有填满 。close() 关闭输出流时存储在输出流的缓冲区中的数据就会丢失
所以关闭(close)输出流时，应先刷新(flush)换冲的输出流：“迫使所有缓冲的输出数据被写出到底层输出流中”
<p>
4. shutdownOutput()后再write是无效的.

