# EasyMQ
# 实现简单的消息队列功能

# 模块介绍
mqcommon是公共模块<br/>
mqsender是发送消息模块<br/>
mqserver是消息接收服务器<br/>
mqreceiver是消息接收端<br/>
mqclient是消息发送端<br/>

# 如何启动测试用例：<br/>
  1、启动mqserver，clean package -D maven.test.skip=true tomcat7:run<br/>
  2、启动消息消费端，clean package -D maven.test.skip=true tomcat7:run<br/>
  3、执行mqclient客户中SenderTest的main方法<br/>
 
通过zookeeper管理消息消费者
