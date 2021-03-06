package com.sx.mmt.internal.task.command;

import com.sx.mmt.internal.EncodedDataSendingDelayQueue;
import com.sx.mmt.internal.api.DataPacketBuilder;
import com.sx.mmt.internal.protocol.Afn;
import com.sx.mmt.internal.protocol.ControlField;
import com.sx.mmt.internal.protocol.ProtocolAttribute;
import com.sx.mmt.internal.protocolBreakers.DecodedPacket;
import com.sx.mmt.internal.protocolBreakers.EncodedPacket;
import com.sx.mmt.internal.protocolBreakers.GBDecodedPacketFactory;
import com.sx.mmt.internal.protocolBreakers.GBProtocolBreakerPool;
import com.sx.mmt.internal.util.PropertiesUtil;
import com.sx.mmt.internal.util.SpringBeanUtil;

public class DataInitCommand extends SinglePacketCommand<DataInitCommand>{
	@Override
	public void sendOrder(String from, String to, String event, DecodedPacket context) throws Exception {

		DecodedPacket decodedPacket=GBDecodedPacketFactory.getDefaultGBSingleFrame();
		task.setTerminalAddressAndSeq(decodedPacket);
		decodedPacket.put(ProtocolAttribute.AUX_IS_USE_PW, true);
		decodedPacket.put(ProtocolAttribute.CONTROLFIELD_FUNCTION_CODE, ControlField.PRM1_RESET);
		decodedPacket.put(ProtocolAttribute.AFN_FUNCTION,Afn.AFN_RESET);
		decodedPacket.put(ProtocolAttribute.SEQ_IS_NEED_CONFIRM, true);
		decodedPacket.put(ProtocolAttribute.DATAUNITIDENTIFY_FN, 2);
		decodedPacket.put(ProtocolAttribute.DATAUNITIDENTIFY_PN, 0);
		
		GBProtocolBreakerPool pbPool=(GBProtocolBreakerPool) 
				SpringBeanUtil.getBean("gBProtocolBreakerPool");
		DataPacketBuilder dataPacketBuilder=pbPool.getDataPacketBuilder();
		EncodedPacket encodedPacket= dataPacketBuilder
				.build(decodedPacket,task.getTaskConfig().getProtocolArea());
		pbPool.returnObject(dataPacketBuilder);
		encodedPacket.setTaskId(task.getId());
		EncodedDataSendingDelayQueue sendQueue=
				(EncodedDataSendingDelayQueue) SpringBeanUtil.getBean("encodedDataSendingDelayQueue");
		sendQueue.addPacket(encodedPacket);
		task.updateActionNow("发送数据区初始化报文");
		task.setDeadlineTime();
	}
	@Override
	public void finish(String from, String to, String event, DecodedPacket context) throws Exception {
		task.updateActionNow("数据区初始化成功");
		task.setNextActionTime(System.currentTimeMillis());
	}
	@Override
	public void orderDeny(String from, String to, String event, DecodedPacket context) throws Exception {
		task.updateActionNow("数据区初始化失败");
		task.setNextActionTime(System.currentTimeMillis());
		
	}
}
