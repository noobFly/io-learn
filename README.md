# io-learn
<p>
com.noob.learn.bio  socket/socketServer accept()实现阻塞IO通讯
<p>
 com.noob.learn.nio  SocketChannel/ServerSocketChannel Selector open() 实现非阻塞IO 多路复用 通讯 

<p>
类比于维护双方通讯的信道：
站在Socket/ServerSocket->ServerSocket.accept()、SocketChannel/ServerSocketChannel->ServerSocketChannel.accept()各自的视角:
<p>己方的输出是对方的输入, 己方的输入是对方的输出; 己方的localAddress是对面的RemoteAddress, 己方的RemoteAddress是对面的localAddress.

blog：
https://my.oschina.net/u/3434392/blog/2999202