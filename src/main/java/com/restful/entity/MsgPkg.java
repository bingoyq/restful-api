package com.restful.entity;

import com.alibaba.fastjson.JSONObject;
import com.gdcp.common.util.StringUtil;

import java.util.Date;

public class MsgPkg {
	private byte[] data; // 原始数据信息 完整数据包 从0x23开始到最后一个校验值
	private byte[] context; // 真正的业务数据信息 
	private byte[] trans; // 转义后数据信息（去掉0x23,0x23和最后一个校验byte）
	private byte[] response; // 应答数据信息
	private String devcode; // 车机ID
	private Date recvTime; // 平台时间 接收时间
	private Date respTime; // 平台时间 应答时间
	private Date devTime; // 车机时间
	private byte cmdId; // 功能ID
	private byte respId; // 应答ID
	private byte encryption;//加密方式0x01:不加密  0x02:RSA  0x03:AES128 	0xFE:表异常  0xFF:表无效  其他预留

	private String clientId;
	private JSONObject parsedData;
	
	private boolean isValidate=true;
	
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte getRespId() {
		return respId;
	}

	public void setRespId(byte respId) {
		this.respId = respId;
	}

	/**
	 * 获得设备ID
	 * 
	 * @return
	 */
	public String getDevcode() {
		return devcode;
	}

	/**
	 * 设置车机ID
	 * 
	 * @param devcode
	 */
	public void setDevcode(String devcode) {
		this.devcode = devcode;
	}

	/**
	 * 获得平台时间（接收时间）
	 * 
	 * @return
	 */
	public Date getRecvTime() {
		return recvTime;
	}

	/**
	 * 设置平台时间（接收时间）
	 * 
	 * @param recvTime
	 */
	public void setRecvTime(Date recvTime) {
		this.recvTime = recvTime;
	}

//	public DBObject toDBObject() {
//		DBObject dbObj = new BasicDBObject();
//		dbObj.put("devcode", devcode);
//		dbObj.put("recvTime", recvTime);
//		dbObj.put("cmdId", cmdId);
//		dbObj.put("data", StringUtil.bytesToString(data).trim());
//		dbObj.put("response", (response == null) ? "NULL" : StringUtil.bytesToString(response).trim());
//		dbObj.put("responseTime", respTime);
//		return dbObj;
//	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("devcode:" + devcode);
		sb.append("\trecvTime:" + recvTime);
		sb.append("\tdevTime:" + devTime);
		sb.append("\tcmdId:" + cmdId);
		sb.append("\tdata:" + StringUtil.bytesToString(data));
		// String str = Base64.encodeToString(data, Base64.DEFAULT);
		// String encode = Base64.getEncoder().encodeToString(data);
		// byte[]decode = Base64.getDecoder().decode(encode);
		return sb.toString();
	}

	public String bytesToString(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(" " + Integer.toHexString(data[i] & 0xff));
		}
		return sb.toString();
	}

	public Date getDevTime() {
		return devTime;
	}

	public void setDevTime(Date devTime) {
		this.devTime = devTime;
	}

	public byte[] getResponse() {
		return response;
	}

	public void setResponse(byte[] response) {
		this.response = response;
	}

	public byte[] getTrans() {
		return trans;
	}

	public void setTrans(byte[] trans) {
		this.trans = trans;
	}

	public Date getRespTime() {
		return respTime;
	}

	public void setRespTime(Date respTime) {
		this.respTime = respTime;
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] context) {
		this.context = context;
	}

	public byte getEncryption() {
		return encryption;
	}

	public void setEncryption(byte encryption) {
		this.encryption = encryption;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public byte getCmdId() {
		return cmdId;
	}

	public void setCmdId(byte cmdId) {
		this.cmdId = cmdId;
	}

	public JSONObject getParsedData() {
		return parsedData;
	}

	public void setParsedData(JSONObject parsedData) {
		this.parsedData = parsedData;
	}

	public boolean isValidate() {
		return isValidate;
	}

	public void setValidate(boolean isValidate) {
		this.isValidate = isValidate;
	}
}
