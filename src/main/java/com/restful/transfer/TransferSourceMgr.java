package com.restful.transfer;

import com.gdcp.common.util.StringUtil;
import com.gdcp.common.util.TimeScheduler;
import com.restful.transfer.cach.SourceAgentCach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 国家平台cach，可多个转发平台
 */
public class TransferSourceMgr {

	private static Logger logger_ = LoggerFactory.getLogger(TransferSourceMgr.class);

	private Map<String, TransferSourceAgentCache> cacheMap = new ConcurrentHashMap<String, TransferSourceAgentCache>();
	private Map<String ,String > devMap = new ConcurrentHashMap<>(); // 车机通道绑定关系

 	private volatile static TransferSourceMgr instance_ = null;

	private int maxAgentNum ;// 连接池大小
	private String sourceUrl; // 连接地址 0.0.0.0:8000
	private String transferType ; // 平台标识
	private String uniqueCode ; // 平台唯一编码
	private String username ; // 平台登录用户
	private String password; // 平台登录密码

	private int pattern = 1; // 模式，1、单通道 2、单车多通道 3、多车多通道
	private String patternKey ; // 单通道模式，patternKey 缓存一个值。
	private boolean online = false; // 在线状态，配合断线动作。

	private TransferSourceMgr() {
	}

	public static TransferSourceMgr getInstance() {
		if (instance_ == null) {
			synchronized (TransferSourceMgr.class) {
				if (instance_ == null) {
					instance_ = new TransferSourceMgr();
//					instance_.checkActiveScheduleTask();
				}
			}
		}
		return instance_;
	}

	/**
	 * 车机绑定通道
	 * @param devcode
	 * @param transferType
	 * @return
	 */
	public boolean banding(String devcode ,String transferType){
		boolean f = false;
		TransferSourceAgentCache cache = cacheMap.get(transferType);
		if(cache != null) {
			devMap.put(devcode ,transferType);
			f = true;
		}
		return f;
	}

	/**
	 * 初始化cache参数
	 * @param maxAgentNum
	 * @param sourceUrl
	 * @param transferType
	 * @param uniqueCode
	 * @param username
	 * @param password
	 */
	public void initCasheParameter(int maxAgentNum ,String sourceUrl ,String transferType ,String uniqueCode ,String username ,String password) {
		this.maxAgentNum = maxAgentNum ;
		this.sourceUrl = sourceUrl;
		this.transferType = transferType;
		this.uniqueCode = uniqueCode;
		this.username = username;
		this.password = password;

//		this.sourceUrl = "192.168.3.253:8888";
//		this.maxAgentNum = 1;
//		this.transferType = "xy";
//		this.uniqueCode = "12345678901234567";
//		this.username = "admin";
//		this.password = "password1";
	}

	/**
	 * 根据车机号
	 * @param devcode
	 * @return
	 * @throws Exception
	 */
	public TransferSourceAgent getSource(String devcode) throws Exception {

		TransferSourceAgentCache agentCache = null;
		TransferSourceAgent agent = null;

		if(!online || cacheMap.size() <= 0) // 连接状态为断开，并且连接缓存没有有值。
			return agent;

		try {
			switch (pattern) {
				case 1 :

					if(StringUtil.isNullOrBlank(patternKey))
						resetPattern();

					agentCache = cacheMap.get(patternKey);
					agent = agentCache.applySource();

					break;
				case 2 :

					List<String> list = new ArrayList<>();
					list.addAll(cacheMap.keySet());
					if(list.size() == 1) {
						agentCache = cacheMap.get(list.get(0));
					} else {
						Random r = new Random();
						agentCache = cacheMap.get(list.get(r.nextInt(list.size())));
					}
					agent = agentCache.applySource();

					break;
				case 3 :

					agentCache = cacheMap.get(devMap.get(devcode));
					agent = agentCache.applySource();

					break;
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		return agent;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public TransferSourceAgent initSource() throws Exception {

		if(StringUtil.isNullOrBlank(transferType))
			return null;

		String devcode = null;

		TransferSourceAgentCache cache = this.getSourceAgentCache();

		if (cache == null) {
			logger_.error("无法找到对应的转发配置信息 transferType [{}], devcode [{}]", transferType, devcode);
			throw new Exception("无法找到对应的转发配置信息 ");
		}

		TransferSourceAgent agent = cache.applySource();
		if(null != agent)
			logger_.info("申请转发数据源  ，DEV[{}],数据源ID[{}]  ", devcode,agent.getSourceID());

		return agent;
	}


	/**
	 * 根据转发类型 创建转发代理缓存
	 * @return
	 * @throws Exception
	 */
	public TransferSourceAgentCache getSourceAgentCache()
			throws Exception {

		TransferSourceAgentCache cache;

		if (cacheMap.containsKey(transferType)) {
			return cacheMap.get(transferType);
		} else {
			cache = new TransferSourceAgentCache(sourceUrl ,transferType ,uniqueCode ,username ,password);

			cache.setSourceKey(transferType);
			cache.setMaxAgentNum(maxAgentNum);
			cacheMap.put(transferType, cache);
		}

		return cache;
	}

	/**
	 * 释放数据源
	 * @param agent
	 */
	public void releaseSource(TransferSourceAgent agent) {
		if (agent != null && cacheMap.containsKey(agent.getSourceKey())) {
			cacheMap.get(agent.getSourceKey()).releaseSource(agent);
		}
	}

	/**
	 * 提交定时任务 检查是否失活 失活就重连
	 * 
	 */
	private void checkActiveScheduleTask() {
		TimeScheduler.instance().registerScheduledTask(new Runnable() {
			/**
			 * 超时处理
			 */
			@Override
			public void run() {

				for (String cache : cacheMap.keySet()) {
					cacheMap.get(cache).activeCheck();
				}
			}

		}, 1, 30, TimeUnit.SECONDS);

	}

	public void setPattern(int pattern) {
		this.pattern = pattern;
	}

	public void setOnline(boolean online) {
		this.online = online;

		if(!online) {
//			for(String key : cacheMap.keySet()) {
//				TransferSourceAgentCache cache = cacheMap.get(key);
//				TransferSourceAgent agent = null;
//				try {
//					agent = cache.applySource();
//					if(agent!= null && agent.isActive()) {
//						agent.disconnect();
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				} finally {
//					this.releaseSource(agent);
//				}
//			}

			SourceAgentCach.getInstance().removeAll(); // 删除缓存、退出登录、断开连接
		}

	}

	/**
	 * 重置单通道值
	 */
	private void resetPattern(){
		if(StringUtil.isNullOrBlank(patternKey)) {
			for(String key : cacheMap.keySet()) {
				patternKey = key;
				break;
			}
		}
	}
}
