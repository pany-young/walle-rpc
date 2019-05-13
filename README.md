walle-rpc

This is WalleRpc Project can see the demo to how to use. https://github.com/pany-young/walle-demo

目前实现的功能有：
1.服务注册与发现 ，目前只支持zookpeer（独特的实现方式，具体可以看 http://www.lofter.com/lpost/1fea4630_12b3e0ab7）

2.负载均衡

3.自动序列化以及反序列化

4.2种简便使用方式：注解和xml，大大的方便了开发 如果使用xml的方式，使用walle标签需在beans标签里添加: xmlns:walle="http://walle.pany.cn/schema/walle" xsi:schemaLocation=" http://walle.pany.cn/schema/walle http://walle.pany.cn/schema/walle/walle.xsd" 
标签有如下：
<walle:registry> <walle:app> <walle:reference>

5.多版本
 

后续文档及功能变更请大家多留意项目github上的wiki：https://github.com/pany-young/walle-rpc/wiki
