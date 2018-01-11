package com.restful.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TransferSourceAgentCache {

	private static Logger logger_ = LoggerFactory.getLogger(TransferSourceAgent.class);
	/**
	 * 数据源URL、数据源的标识
	 */
	private String sourceUrl;
	private String transferType;
	private String sourceKey;

	private String uniqueCode;
	private String username;
	private String password;
	private int maxAgentNum=1;//至少一个

	// 闲置的连接, 通过队列实现agent轮流使用, 避免agent闲置后出现的超时重连现象
	private volatile LinkedBlockingQueue<TransferSourceAgent> idle_list = new LinkedBlockingQueue<>();
 	private volatile Set<TransferSourceAgent> active_list = new HashSet<TransferSourceAgent>();// 活跃连接(正在使用的)

	private Lock lock_ = new ReentrantLock();


	public TransferSourceAgentCache(String sourceUrl ,String transferType ,String uniqueCode ,String username ,String password) {
		this.sourceUrl = sourceUrl;
		this.transferType = transferType;
		this.uniqueCode = uniqueCode ;
		this.username = username;
		this.password = password;
	}

	/**
	 * 获取数据源 1.在空闲队列获取一个空闲数据源 2.创建一个新的数据源
	 * 
	 * @return
	 * @throws Exception
	 * @throws Throwable
	 */
	public TransferSourceAgent applySource() throws Exception {
		lock_.lock();
		try {
 			TransferSourceAgent agent = null;
			if (!idle_list.isEmpty()) {
				logger_.debug("idle_list 不为空， size[{}] ",idle_list.size());

				Iterator<TransferSourceAgent> iterator = idle_list.iterator();
				//取agent之前进行检查, 销毁不可用的agent
			 
				while (iterator.hasNext()) {					
					agent = iterator.next();
					if (!agent.isActive()) {
						logger_.debug("需要销毁 agent ,数据源ID[{}]，原因 {isTimout[{}],isActive[{}]} ",agent.getSourceID() ,agent.isActive());
						destroy(agent);
						idle_list.remove(agent); // 断开后需要从连接池中清除
 					} 
				}
				for(int i=0;i<idle_list.size();i++){
 				//从idle_list首部取出一个agent, 用于转发, 同时需要添加到active_list
					agent = idle_list.poll();
					if(agent.isActive()){
						active_list.add(agent);
						logger_.debug("申请到旧数据源ID: {}", agent.getSourceID());
						return agent;
					}else{
						idle_list.add(agent);
					}
				}
			}
			// create a new source
			logger_.debug("Connect URL is : [{}], transferType is:  [{}]", sourceUrl, transferType);
			try {
				//限制最大的agent连接数
				if ((idle_list.size() + active_list.size()) < maxAgentNum) {
					agent = createAgent();
					if (agent != null && agent.isActive()) {
						agent.setSourceKey(getSourceKey());
						active_list.add(agent);
						logger_.debug("申请新的数据源ID: [{}], transferType is:  [{}]", agent.getSourceID(), transferType);
						return agent;
					}
				}else{
					logger_.error("不能申请新的数据源，idle_list size [{}], active_list size [{}],maxAgentNum [{}]", idle_list.size(),active_list.size(),maxAgentNum);
				}
			} catch (Exception e) {
				// can not create data source by exception
				logger_.error("申请数据源失败 : {} {} ", sourceUrl, e);
				e.printStackTrace();
			}

			logger_.error("无法获取数据源 URL[{}] , transferType [{}]", sourceUrl, transferType);
			logger_.error("请检测相应服务是否启动!");
		} catch (Exception e) {
			logger_.error("申请数据源异常 {} ", e);
			e.printStackTrace();
		} finally {
			lock_.unlock();
		}
		return null;
	}

	/**
	 * 创建新的数据源连接
	 * @return
	 * @throws Exception
	 */
	private TransferSourceAgent createAgent() throws Exception {
		try {
			TransferSourceAgent agent = new TransferSourceAgent(uniqueCode ,username ,password);
			agent.connect(sourceUrl);
			return agent;
		} catch (Exception e) {
			logger_.error("!!!!创建数据源失败, 发生异常", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 释放数据源
	 */
	public void releaseSource(TransferSourceAgent agent) {
		try {
			lock_.lock();
			logger_.debug("释放数据源，ID[{}]", agent.getSourceID());
			if (active_list.contains(agent)) {
				active_list.remove(agent);
			}
			idle_list.add(agent);
		} catch (Exception e) {
			logger_.error("释放数据源异常 {} ", e);
			e.printStackTrace();
		} finally {
			lock_.unlock();
		}

	}

	/**
	 * 检查是否失活 失活就重连
	 */
	public void activeCheck() {
		lock_.lock();
		try {
			logger_.debug(
					"TransferConnect activeCheck ,idle_list Size [{}],active_list Size [{}],URL [{}], transferType [{}]",
					idle_list.size(), active_list.size(), sourceUrl, transferType);
			if (idle_list.size() > 0) {
				for (TransferSourceAgent agent : idle_list) {
					int recCount = agent.getRecCount();
					long lastConnectTime = agent.getLastConnectTime();

					if (!agent.isActive()) {
						long currTime = (new Date()).getTime();
						if (recCount < 3 && (currTime - lastConnectTime >= 60 * 1000)) {// 如果重连次数小于3次,则判断是否间隔大于1分钟
							recCount++;
							lastConnectTime = currTime;
							logger_.debug("数据源id【{}】开始第[{}]次重连,Connect URL is : [{}], transferType is:  [{}]",agent.getSourceID(), recCount,
									sourceUrl, transferType);
							agent.reConnect();
						} else if (recCount >= 3) {
							if (currTime - lastConnectTime >= 30 * 60 * 1000) {// 如果重连次数超过3次，
																				// 则判断是否间隔大于30分钟
								recCount = 1;

								logger_.debug("30分钟后，数据源id【{}】重新开始第1次重连,Connect URL is : [{}], transferType is:  [{}]", agent.getSourceID(),recCount,
										sourceUrl, transferType);
								lastConnectTime = currTime;
								agent.reConnect();
							} else {
								logger_.error("数据源id【{}】重连超过3次，需要等待30分钟,Connect URL is : [{}], transferType is:  [{}]", agent.getSourceID(),recCount,
										sourceUrl, transferType);
							}
						}

					} else {// 如果连接是成功的 此变量就为0
						agent.setRecCount(0);
						agent.setLastConnectTime(0);

						logger_.debug("TransferConnect isActive ,数据源id【{}】,URL [{}], transferType [{}]", agent.getSourceID(),sourceUrl, transferType);
					}
				}
			}
		} catch (Exception e) {
			logger_.error("agent激活检测出现异常,{}",e);
			e.printStackTrace();
		} finally {
			lock_.unlock();
		}
	}

	/**
	 * 销毁
	 * 
	 * @param source
	 * @throws Exception
	 */

	public void destroy(TransferSourceAgent source) throws Exception {
		source.disconnect();
	}

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setMaxAgentNum(int maxAgentNum) {
		this.maxAgentNum = maxAgentNum;
	}
	


}
