datax-oraclejdbcwriter
======================

Oracle jdbc writer plugin of Taobao DataX

淘宝开源的数据导入导出工具datax的oracle只提供了基于OCI的writer plugin。本项目提供了基于oracle jdbc驱动的writer。

###用法

 * 下载datax，`svn co http://code.taobao.org/svn/datax`
 * 下载本项目源码，并将插件源码放入对应的源码包里
 * 修改conf/plugins.xml，插入oraclejdbcwriter的配置

 ```xml
 	<plugin>
		<version>1</version>
		<name>ojdbcwriter</name>
		<type>writer</type>
		<target>oracle</target>
		<jar>ojdbcwriter-1.0.0.jar</jar>
		<class>com.taobao.datax.plugins.writer.ojdbcwriter.OracleJdbcWriter</class>
		<maxthreadnum>40</maxthreadnum>
	</plugin>
 ```

 * 修改build.xml，新增ojdbcwriter target

 ```xml
 	<target name="oraclejdbcwriter" depends="clean,compile">
		<foreach target="eachplugindist" param="var">
			<path id="plugins">
				<pathelement path="${classes.dir}/com/taobao/datax/plugins/writer/oraclejdbcwriter/1.0.0" />
			</path>
		</foreach>
	</target>	
 ```
 
* 修改build.xml，在plugindist target里新增一行

 ```xml
 	<pathelement path="${classes.dir}/com/taobao/datax/plugins/writer/oraclejdbcwriter/1.0.0" />
 ```
 
* 参照datax的文档打包，writer选择是选择oraclejdbcwriter即可。
