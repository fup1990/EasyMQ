# EasyMQ
# 消息队列
使用netty进行远程通信<br/>
通过zookeeper进行消费者管理<br/>
使用disruptor并发框架<br/>
使用guava cache缓存<br/>
使用kryo高性能的序列化框架<br/>

# 模块介绍
mqcommon是公共模块<br/>
mqsender是发送消息模块<br/>
mqserver是消息接收服务器<br/>
mqreceiver是消息接收端<br/>
mqclient是消息发送端<br/>

# 如何启动测试用例：<br/>
  1、启动zookeeper，通过zookeeper管理消费者<br/>
  2、启动mqserver，clean package -D maven.test.skip=true tomcat7:run<br/>
  3、启动消息消费端，clean package -D maven.test.skip=true tomcat7:run<br/>
  4、执行mqclient客户中SenderTest的main方法<br/>
 
