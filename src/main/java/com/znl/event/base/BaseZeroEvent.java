package com.znl.event.base;

import org.apache.mina.core.session.IoSession;

/**
 * 零点事件接口
 * @author yxh
 *
 */
public interface BaseZeroEvent {
	
	/**
	 * 个人零点处理方法
	 */
	public void zeroHandler();
	

}
