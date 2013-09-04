package com.taobao.datax.plugins.writer.ojdbcwriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.taobao.datax.common.exception.DataExchangeException;
import com.taobao.datax.common.plugin.Line;
import com.taobao.datax.common.plugin.LineReceiver;
import com.taobao.datax.common.plugin.PluginParam;
import com.taobao.datax.common.plugin.PluginStatus;
import com.taobao.datax.common.plugin.Writer;
import com.taobao.datax.plugins.common.DBSource;

/**
 * datax oracle jdbc writer
 * @author jingege
 *
 */
public class OracleJdbcWriter extends Writer {

	private Logger logger = Logger.getLogger(OracleJdbcWriter.class);

	private String password;

	private String username;

	private String dbname;

	private String table;

	private String pre;

	private String post;

	private String encoding;

	private String dtfmt;

	private String colorder;

	private int limit;

	private int failCount;// count error lines

	private long concurrency;

	private int commitCount;

	private String sourceUniqKey = "";

	private String port;

	private String insert;

	private String host;

	private String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	private Connection connection;

	private List<Line> duplicatedLineBuffer;

	private int duplicatedThreshold;

	private String onDuplicatedSql;

	private String duplidatedKeyIndices;

	private String schema;

	@Override
	public int init() {
		password = param.getValue(ParamKey.password, "");
		username = param.getValue(ParamKey.username, "");
		host = param.getValue(ParamKey.ip);
		port = param.getValue(ParamKey.port, "3306");
		dbname = param.getValue(ParamKey.dbname, "");
		table = param.getValue(ParamKey.table, "");
		schema = param.getValue(ParamKey.schema, "");
		pre = param.getValue(ParamKey.pre, "");
		post = param.getValue(ParamKey.post, "");
		insert = param.getValue(ParamKey.insert, "");
		encoding = param.getValue(ParamKey.encoding, "UTF-8");
		dtfmt = param.getValue(ParamKey.dtfmt, "");
		colorder = param.getValue(ParamKey.colorder, "");
		limit = param.getIntValue(ParamKey.limit, 1000);
		concurrency = param.getIntValue(ParamKey.concurrency, 1);
		duplicatedThreshold = param.getIntValue(ParamKey.duplicatedThreshold,
				10000);
		onDuplicatedSql = param.getValue(ParamKey.onDuplicatedSql, "");
		duplidatedKeyIndices = param
				.getValue(ParamKey.duplicatedKeyIndices, "");

		this.duplicatedLineBuffer = new ArrayList<Line>();

		commitCount = param.getIntValue(ParamKey.commitCount, 50000);
		this.sourceUniqKey = DBSource.genKey(this.getClass(), host, port,
				dbname);
		this.host = param.getValue(ParamKey.ip);
		this.port = param.getValue(ParamKey.port, "3306");
		this.dbname = param.getValue(ParamKey.dbname);

		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int prepare(PluginParam param) {
		this.setParam(param);

		DBSource.register(this.sourceUniqKey, this.genProperties());

		if (StringUtils.isBlank(this.pre))
			return PluginStatus.SUCCESS.value();

		Statement stmt = null;
		try {
			this.connection = DBSource.getConnection(this.sourceUniqKey);

			stmt = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			for (String subSql : this.pre.split(";")) {
				this.logger.info(String.format("Excute prepare sql %s .",
						subSql));
				stmt.execute(subSql);
			}
			this.connection.commit();
			return PluginStatus.SUCCESS.value();
		} catch (Exception e) {
			throw new DataExchangeException(e.getCause());
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
				if (null != this.connection) {
					this.connection.close();
					this.connection = null;
				}
			} catch (SQLException e) {
			}
		}
	}

	@Override
	public int connect() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int startWrite(LineReceiver receiver) {
		PreparedStatement ps = null;
		try {
			this.connection = DBSource.getConnection(this.sourceUniqKey);

			this.logger.info(String.format("Config encoding %s .",
					this.encoding));

			/* load data begin */
			Line line = null;
			int lines = 0;
			if (StringUtils.isEmpty(this.insert)) {
				this.insert = this.buildInsertString();
			}
			logger.debug("sql=" + insert);
			ps = this.connection.prepareStatement(this.insert,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			this.connection.setAutoCommit(false);
			while ((line = receiver.getFromReader()) != null) {
				try {
					for (int i = 0; i < line.getFieldNum(); i++) {
						ps.setObject(i + 1, line.getField(i));
					}
					ps.execute();
				} catch (SQLException e) {
					if (e.getMessage().contains("ORA-00001")) {// unique
																// constraint
																// violated
						logger.debug("Duplicated line found:" + line);
						duplicatedLineBuffer.add(line);
						if (this.duplicatedLineBuffer.size() >= this.duplicatedThreshold) {
							logger.info("Too much duplicated lines,now process them .");
							this.connection.commit();
							this.flushDuplicatedBuffer();
						}
					} else {
						failCount++;
						logger.debug("Fail line(" + e.getMessage() + "):"
								+ line);
						if (failCount >= this.limit) {
							throw new DataExchangeException(
									"Too many failed lines(" + failCount
											+ ") .");
						} else {
							continue;
						}
					}
				}
				if (lines++ == this.commitCount) {
					logger.info(lines + " committed by worker "
							+ Thread.currentThread().getName() + " .");
					lines = 0;
					this.connection.commit();

				}
			}
			this.connection.commit();
			if (!this.duplicatedLineBuffer.isEmpty()) {
				logger.info("Some duplicated line will now be processed.");
				this.flushDuplicatedBuffer();
			}

			this.connection.setAutoCommit(true);
			this.getMonitor().setFailedLines(this.failCount);
			this.logger.info("DataX write to oracle ends by worker "
					+ Thread.currentThread().getName() + " .");

			return PluginStatus.SUCCESS.value();
		} catch (Exception e2) {
			e2.printStackTrace();
			if (null != this.connection) {
				try {
					this.connection.close();
				} catch (SQLException e) {
				}
			}
			throw new DataExchangeException(e2.getCause());
		} finally {
			if (null != ps)
				try {
					ps.close();
				} catch (SQLException e3) {
				}
		}
	}

	/**
	 * 先删除主键冲突的，再flush缓存中的行到db
	 * 
	 * @throws SQLException
	 */
	private void flushDuplicatedBuffer() throws SQLException {
		if (this.onDuplicatedSql == null || this.onDuplicatedSql.isEmpty()) {
			throw new DataExchangeException(
					"On duplicated sql is empty,duplicated lines processing failed.");
		}
		Iterator<Line> lines = this.duplicatedLineBuffer.iterator();
		PreparedStatement ps = null;
		try {
			ps = this.connection.prepareStatement(this.onDuplicatedSql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Prepare on duplicated sql error.");
			throw new DataExchangeException(e);
		}
		String[] idxs = StringUtils.split(this.duplidatedKeyIndices, ',');
		int[] iidxs = new int[idxs.length];
		for (int i = 0; i < idxs.length; i++) {
			iidxs[i] = Integer.parseInt(idxs[i]);
		}
		int deleteCount = 0;
		int deleteSuccessCount = 0;
		while (lines.hasNext()) {
			Line line = lines.next();
			try {
				for (int i = 0; i < idxs.length; i++) {
					ps.setObject(i + 1, line.getField(iidxs[i]));
					int num = ps.executeUpdate();
					deleteSuccessCount += num;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				// delete failed remove this line
				lines.remove();
				failCount++;
				if (failCount >= this.limit) {
					throw new DataExchangeException("Too many failed lines("
							+ failCount + ") .");
				} else {
					continue;
				}
			}
			if (deleteCount++ >= this.commitCount) {
				this.connection.commit();
				deleteCount = 0;
				logger.info("Delete " + deleteCount + " duplicated lines .");
			}
		}
		logger.info(deleteSuccessCount + "/" + this.duplicatedLineBuffer.size()
				+ " duplicated line(s) are deleted .");
		this.connection.commit();
		ps.close();
		ps = this.connection.prepareStatement(this.insert);
		lines = this.duplicatedLineBuffer.iterator();
		int linesCount = 0;
		int insertSuccessCount = 0;
		while (lines.hasNext()) {
			Line line = lines.next();
			try {
				for (int i = 0; i < line.getFieldNum(); i++) {
					ps.setObject(i + 1, line.getField(i));
				}
				ps.execute();
				insertSuccessCount++;
			} catch (SQLException e) {
				e.printStackTrace();
				failCount++;
				if (failCount >= this.limit) {
					throw new DataExchangeException("Too many failed lines("
							+ failCount + ") .");
				} else {
					continue;
				}
			}
			if (linesCount++ == this.commitCount) {
				logger.info(lines + " committed by worker "
						+ Thread.currentThread().getName()
						+ " after duplicated lines deleted.");
				linesCount = 0;
				this.connection.commit();

			}
		}
		ps.close();
		this.connection.commit();
		logger.info(insertSuccessCount + "/" + this.duplicatedLineBuffer.size()
				+ " duplicated line(s) are inserted again .");
		this.duplicatedLineBuffer.clear();
	}

	@Override
	public int post(PluginParam param) {
		if (StringUtils.isBlank(this.post))
			return PluginStatus.SUCCESS.value();

		Statement stmt = null;
		try {
			this.connection = DBSource.getConnection(this.sourceUniqKey);

			stmt = this.connection.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);

			for (String subSql : this.post.split(";")) {
				this.logger.info(String.format("Excute prepare sql %s .",
						subSql));
				stmt.execute(subSql);
			}

			return PluginStatus.SUCCESS.value();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataExchangeException(e.getCause());
		} finally {
			try {
				if (null != stmt) {
					stmt.close();
				}
				if (null != this.connection) {
					this.connection.close();
					this.connection = null;
				}
			} catch (Exception e2) {
			}

		}
	}

	@Override
	public List<PluginParam> split(PluginParam param) {
		OracleThinWriterSplitter splitter = new OracleThinWriterSplitter();
		splitter.setParam(param);
		splitter.init();
		return splitter.split();
	}

	@Override
	public int commit() {
		return PluginStatus.SUCCESS.value();
	}

	@Override
	public int finish() {
		return PluginStatus.SUCCESS.value();
	}

	private Properties genProperties() {
		Properties p = new Properties();
		p.setProperty("driverClassName", this.DRIVER_NAME);
		String url = "jdbc:oracle:thin:@" + this.host + ":" + this.port + "/"
				+ this.dbname;
		p.setProperty("url", url);
		p.setProperty("username", this.username);
		p.setProperty("password", this.password);
		p.setProperty("maxActive", String.valueOf(this.concurrency + 2));

		return p;
	}

	public String buildInsertString() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ").append(this.schema + "." + this.table)
				.append(" ");
		if (!StringUtils.isEmpty(this.colorder)) {
			sb.append("(").append(this.colorder).append(")");
		}
		sb.append(" VALUES(");
		try {
			ResultSet rs = this.connection.createStatement().executeQuery(
					"SELECT COLUMN_NAME,DATA_TYPE FROM USER_TAB_COLUMNS WHERE TABLE_NAME='"
							+ this.table.toUpperCase() + "'");
			LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
			while (rs.next()) {
				String colName = rs.getString(1);
				String colType = rs.getString(2);
				map.put(colName, colType);
			}
			logger.debug("Column map:size=" + map.size() + ";cols="
					+ map.toString());
			if (StringUtils.isEmpty(this.colorder)) {
				Iterator<Entry<String, String>> it = map.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String colType = entry.getValue();
					if (colType.toUpperCase().equals("DATE")) {
						sb.append("to_date(?,'" + this.dtfmt + "'),");
					} else {
						sb.append("?,");
					}
				}
				sb.deleteCharAt(sb.length() - 1);// remove last comma
				sb.append(")");
			} else {
				String[] arr = colorder.split(",");
				for (String colName : arr) {
					if (!map.containsKey(colName)) {
						throw new DataExchangeException("col " + colName
								+ " not in database");
					}
					String colType = map.get(colName);
					if (colType.toUpperCase().equals("DATE")) {
						sb.append("to_date(?,'" + this.dtfmt + "'),");
					} else {
						sb.append("?,");
					}
				}
				sb.deleteCharAt(sb.length() - 1);// remove last comma
				sb.append(")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataExchangeException(e.getMessage());
		}

		return sb.toString();
	}
}
