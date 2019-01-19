# walle-rpc
This is WalleRpc Project
can see the demo to how to do.
https://github.com/pany-young/walle-demo


WalleRpc 提供了2种使用方式：注解和xml，大大的方便了开发
如果使用xml的方式，使用walle标签需在beans标签里添加:
xmlns:walle="http://walle.pany.cn/schema/walle"
xsi:schemaLocation="
http://walle.pany.cn/schema/walle
http://walle.pany.cn/schema/walle/walle.xsd"
标签有如下：
<walle:registry>
<walle:app>
<walle:reference>



WalleRpc在服务注册与发现方面的实现方式，具体可以看
http://www.lofter.com/lpost/1fea4630_12b3e0ab7

