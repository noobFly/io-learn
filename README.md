# io-learn
<p>
com.noob.learn.bio  socket/socketServer accept()实现阻塞IO通讯
<p>
 com.noob.learn.nio  SocketChannel/ServerSocketChannel Selector open() 实现非阻塞IO 多路复用 通讯 

<p>
类比于维护双方通讯的信道：
站在Socket/ServerSocket->ServerSocket.accept()、SocketChannel/ServerSocketChannel->ServerSocketChannel.accept()各自的视角:
<p>己方的输出是对方的输入, 己方的输入是对方的输出; 己方的localAddress是对面的RemoteAddress, 己方的RemoteAddress是对面的localAddress.
<p> 经过测试发现，BIO/NIO的单次的读入多少由定义的buffer大小决定。数据长度没有超出buffer大小将会一次性读入。 
<p>因为BIO的Inputstream的read方法是不会主动清空byte[],当复用这个byte[]时需要程序显示清空.
<p>对于NIO的byteBuffer而言，在使用的时候结合get、flip、clear方法可以通过控制内部的索引位置来读写，避免出现重复数据。

blog：
https://my.oschina.net/u/3434392/blog/2999202