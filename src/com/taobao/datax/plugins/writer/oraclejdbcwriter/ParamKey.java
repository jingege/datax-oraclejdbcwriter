/**
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.taobao.datax.plugins.writer.oraclejdbcwriter;

public final class ParamKey {

	 /*
     * @name: ip
     * @description: Mysql database ip address
     * @range:
     * @mandatory: true
     * @default:
     */
	public final static String ip = "ip";
	/*
     * @name: port
     * @description: Mysql database port
     * @range:
     * @mandatory: true
     * @default:3306
     */
	public final static String port = "port";
	
	/*
	 * @name: dbname
	 * @description: Oracle database dbname
	 * @range:
	 * @mandatory: true
	 * @default:
	 */
	public final static String dbname = "dbname";
	
	/*
       * @name: schema
       * @description: Oracle database schema
       * @range:
       * @mandatory: true
       * @default:
       */
	public final static String schema = "schema";
	
	/*
	 * @name: table
	 * @description: table to be dumped data into
	 * @range:
	 * @mandatory: true
	 * @default:
	 */
	public final static String table = "table";
    /*
	 * @name: username
	 * @description: oracle database login username
	 * @range:
	 * @mandatory: true
	 * @default:
	 */
	public final static String username = "username";
	
	/*
	 * @name: password
	 * @description: oracle database login password
	 * @range:
	 * @mandatory: true
	 * @default:
	 */
	public final static String password = "password";

	/*
	 * @name: pre
	 * @description: execute pre sql before writing data .
	 * @range:
	 * @mandatory: true
	 * @default:
	 */
	public final static String pre = "pre";
	/*
	 * @name: post
	 * @description: execute post sql after writing data .
	 * @range:
	 * @mandatory: false
	 * @default:
	 */
	public final static String post = "post";
	
	/*
	 * @name: insert
	 * @description: prepared insert statement
	 * @range:
	 * @mandatory: false
	 * @default:
	 */
	public final static String insert = "insert";
	
	/*
	 * @name: encoding
	 * @description: oracle encode
	 * @range: UTF-8|GBK|GB2312
	 * @mandatory: false
	 * @default: UTF-8
	 */
	public final static String encoding = "encoding";
	
	/*
	 * @name: limit
	 * @description: limit amount of errors
	 * @range:
	 * @mandatory: false
	 * @default: 1000
	 */
	public final static String limit = "limit";
	
	/*
	 * @name: dtfmt
	 * @description: date column formate string
	 * @range:
	 * @mandatory: false
	 * @default: yyyy-mm-dd hh24:mi:ss
	 */
	public final static String dtfmt = "dtfmt";
	
	/*
	 * @name: colorder
	 * @description: order of columns
	 * @range:col1,col2
	 * @mandatory: false
	 * @default: 
	 */
	public final static String colorder = "colorder";
	
	 /*
       * @name:concurrency
       * @description:concurrency of the job
       * @range:1-100
       * @mandatory: false
       * @default:1
       */
	public final static String concurrency = "concurrency";
	
	/*
	 * @name:commitCount
	 * @description:how many lines per commit
	 * @default: 50000
	 */
	public final static String commitCount = "commitCount";
	
	/*
	 * @name:duplicatedThreshold
	 * @description:how many duplicated lines to deal with together
	 * @default: 10000
	 */
	public final static String duplicatedThreshold = "duplicatedThreshold";
	
	/*
	 * @name:onDuplicatedSql
	 * @description:sql to deal with duplicated lines
	 * @default: delete from TB_NAME_HERE where KEY_COL_NAME_HERE=?
	 */
	public final static String onDuplicatedSql = "onDuplicatedSql";
	
	/*
	 * @name:duplidatedKeyIndex
	 * @description:the indices of the duplicated key,seperated by comma
	 * @default: 0,
	 */
	public final static String duplicatedKeyIndices = "duplicatedKeyIndices";
}
