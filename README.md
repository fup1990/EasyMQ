# EasyMQ
实现简单的消息队列功能

mqcommon是公共模块
mqsender是发送消息模块
mqserver是消息接受服务器
mqreceiver是消息接受端
mqclient是消息发送端

如何启动测试用例：
  1、启动mqserver，clean package -D maven.test.skip=true tomcat7:run
  2、启动消息消费端，clean package -D maven.test.skip=true tomcat7:run
  3、执行mqclient客户中SenderTest的main方法
