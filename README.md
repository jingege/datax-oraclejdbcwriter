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
		<name>oraclejdbcwriter</name>
		<type>writer</type>
		<target>oracle</target>
		<jar>oraclejdbcwriter-1.0.0.jar</jar>
		<class>com.taobao.datax.plugins.writer.oraclejdbcwriter.OracleJdbcWriter</class>
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

###说明
* dtfmt选项用以处理oracle Date类型的格式化
* 插入数据时优先使用insert配置
* 未配置insert时，程序根据oracle列属性拼接insert
* 未配置insert，且oracle列属性和数据源不一致时，按数据源的列顺序配置colorder
* limit表示可以容忍的最多的插入错误行
* commitCount表示一批commit的insert行数
* duplicatedThreshold表示主键冲突的行数阀值，高于此值暂停导入，转而处理冲突数据
* onDuplicatedSql表示当发生主键冲突时如何解决
* duplicatedKeyIndices向onDuplicatedSql传参
