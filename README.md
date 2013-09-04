datax-oraclejdbcwriter
======================

Oracle jdbc writer plugin of Taobao DataX

淘宝开源的数据导入导出工具datax的oracle只提供了基于OCI的writer plugin。本项目提供了基于oracle jdbc驱动的writer。

###用法

 * 下载datax，`svn co http://code.taobao.org/svn/datax`
 * 下载本项目源码，并将插件源码放入对应的源码包里
 * 修改conf/plugins.xml，插入oraclejdbcwriter的配置
 ```
 	<plugin>
		<version>1</version>
		<name>ojdbcwriter</name>
		<type>writer</type>
		<target>oracle</target>
		<jar>ojdbcwriter-1.0.0.jar</jar>
		<class>com.taobao.datax.plugins.writer.ojdbcwriter.</class>
		<maxthreadnum>40</maxthreadnum>
	</plugin>
	
 ```