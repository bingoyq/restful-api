package com.restful.entity;

import com.gdcp.common.pool.bytebuf.FixedByteBufPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Date;

/**
 * @author guozhen314
 * @Description: TODO(消息包装类) 
 * @date 2017年5月31日 下午4:30:51 
 */
public class MsgPacker {
	ByteBuf buf;

	public MsgPacker() {
		buf = Unpooled.buffer(1024 * 10);
	}

	public void addBytes(byte[] bytes) {
		buf.writeBytes(bytes);
	}

	 
	/**   拆包
	 * @param buf
	 * @return      
	 */ 
	public MsgPkg unpack(ByteBuf buf) {
		byte identifier = 0x23;
		//查找是否有0x23字节
		int firstIdentifier = buf.indexOf(buf.readerIndex(), buf.writerIndex(), identifier);
		if (firstIdentifier == -1) {
			return null;
		}
		//顺延读取下一个字节
		byte secondIdentifier = buf.getByte(firstIdentifier + 1);
		if (secondIdentifier != identifier) {
			//读指针下移一个
			buf.readerIndex(firstIdentifier + 1);
			buf.discardReadBytes();//丢弃字节
			return null;
		}
		buf.readerIndex(firstIdentifier);//将当前字节第一个位置作为下标
		buf.discardReadBytes();//丢弃前面的字节
		try {
			MsgPkg pkg = new MsgPkg();
			buf.readerIndex(22);
			int datalenth = buf.readShort();//数据单元长度
			int pckLength = 25 + datalenth;//总包长度
			int contextLength = 22 + datalenth;//业务数据包长度
			byte[] pck = new byte[pckLength];
			byte[] context = new byte[contextLength];
			buf.readerIndex(0);
			if (buf.readableBytes() < pckLength) {
				buf.readerIndex(0);
				return null;
			}
			buf.readBytes(pck);//读入到总包
			buf.readerIndex(2);
			buf.readBytes(context);//读入到业务数据包

			
			 
			 //校验最后一个字节码 
			  
			if (!this.validateCheckSum(context, pck[pck.length - 1])) {
				//校验码出错 继续生成对象
				pkg.setValidate(false);//校验码出错
//				buf.discardReadBytes();
//				return null;
			}

			pkg.setData(pck);
			byte[] trans = this.decodeTrans(context);//特殊字节转义
			pkg.setTrans(trans);
			pkg.setRecvTime(new Date());
			
			//定义一个临时byte数组
			ByteBuf data = FixedByteBufPool.getInstance().borrowObject(trans.length);
			data.writeBytes(trans);//写入数据 (去掉0x23,0x23和最后一个校验byte)
			try {
				byte cmdId = data.readByte();//命令标识
				pkg.setCmdId(cmdId);
				byte respId = data.readByte();//应答标志
				pkg.setRespId(respId);
				byte[] vid = new byte[17];//车辆识别码 VIN
				data.readBytes(vid);
				for (int i = 0; i < vid.length; i++) {
					if( vid[i] == 0b0 ){
						vid[i] = 32;//把车机号中的\0替换为空格
					}
				}
				String devcode = new String(vid);
				pkg.setDevcode(devcode);
				byte encryption = data.readByte();//加密方式
				pkg.setEncryption(encryption);
				data.readShort();
				byte[] databyte = new byte[datalenth];
				data.readBytes(databyte);
				pkg.setContext(databyte);
				return pkg;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				buf.discardReadBytes();
				FixedByteBufPool.getInstance().returnObject(trans.length, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**   尝试拆包，保留ByteBuf的数据
	 * @param buf
	 * @return      
	 */ 
	public MsgPkg tryUnpack(ByteBuf buf) {
		byte identifier = 0x23;
		//查找是否有0x23字节
		int firstIdentifier = buf.indexOf(buf.readerIndex(), buf.writerIndex(), identifier);
		if (firstIdentifier == -1) {
			return null;
		}
		//顺延读取下一个字节
		byte secondIdentifier = buf.getByte(firstIdentifier + 1);
		if (secondIdentifier != identifier) {
			//读指针下移一个
			buf.readerIndex(firstIdentifier + 1);
			buf.discardReadBytes();//丢弃字节
			return null;
		}
		buf.readerIndex(firstIdentifier);//将当前字节第一个位置作为下标
		buf.discardReadBytes();//丢弃前面的字节
		try {
			MsgPkg pkg = new MsgPkg();
			buf.readerIndex(22);
			int datalenth = buf.readShort();//数据单元长度
			int pckLength = 25 + datalenth;//总包长度
			int contextLength = 22 + datalenth;//业务数据包长度
			byte[] pck = new byte[pckLength];
			byte[] context = new byte[contextLength];
			buf.readerIndex(0);
			if (buf.readableBytes() < pckLength) {
				buf.readerIndex(0);
				return null;
			}
			buf.readBytes(pck);//读入到总包
			buf.readerIndex(2);
			buf.readBytes(context);//读入到业务数据包

			
			 
			 //校验最后一个字节码 
			  
			if (!this.validateCheckSum(context, pck[pck.length - 1])) {
				//校验码出错 继续生成对象
				pkg.setValidate(false);//校验码出错
//				buf.discardReadBytes();
//				return null;
			}

			pkg.setData(pck);
			byte[] trans = this.decodeTrans(context);//特殊字节转义
			pkg.setTrans(trans);
			pkg.setRecvTime(new Date());
			
			//定义一个临时byte数组
			ByteBuf data = FixedByteBufPool.getInstance().borrowObject(trans.length);
			data.writeBytes(trans);//写入数据 (去掉0x23,0x23和最后一个校验byte)
			try {
				byte cmdId = data.readByte();//命令标识
				pkg.setCmdId(cmdId);
				byte respId = data.readByte();//应答标志
				pkg.setRespId(respId);
				byte[] vid = new byte[17];//车辆识别码 VIN
				data.readBytes(vid);
				for (int i = 0; i < vid.length; i++) {
					if( vid[i] == 0b0 ){
						vid[i] = 32;//把车机号中的\0替换为空格
					}
				}
				String devcode = new String(vid);
				pkg.setDevcode(devcode);
				byte encryption = data.readByte();//加密方式
				pkg.setEncryption(encryption);
				data.readShort();
				byte[] databyte = new byte[datalenth];
				data.readBytes(databyte);
				pkg.setContext(databyte);
				return pkg;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
//				buf.discardReadBytes();
				FixedByteBufPool.getInstance().returnObject(trans.length, data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public MsgPkg unpack() {
		return unpack(buf);
	}
	 
	/**   组响应包
	 * @param devcode 车机id
	 * @param cmdId 命令标识
	 * @param respId 响应标识
	 * @param encryption 加密方式
	 * @param data 数据内容
	 * @return      
	 */ 
	public byte[] pack(String devcode, int cmdId, int respId, int encryption, byte[] data) {
		byte[] msg = new byte[data.length + 22];
		msg[0] = (byte) cmdId;
		msg[1] = (byte) respId;
		byte[] vid = devcode.getBytes();
		if (vid.length != 17) {
			throw new RuntimeException("DEVCODE(VIN) BYTES NOT SUPPORT");
		}
		System.arraycopy(vid, 0, msg, 2, vid.length);
		msg[19] = (byte) encryption;
		msg[20] = (byte) ((data.length >> 8) & 0xff);
		msg[21] = (byte) ((data.length) & 0xff);
		System.arraycopy(data, 0, msg, 22, data.length);

		byte[] trans = this.encodeTrans(msg);
		byte sum = this.makeCheckSum(trans);
		byte[] pkg = new byte[trans.length + 3];
		pkg[0] = 0x23;
		pkg[1] = 0x23;
		System.arraycopy(trans, 0, pkg, 2, trans.length);
		pkg[pkg.length - 1] = sum;
		return pkg;
	}
	
	
	/**   生成校验码
	 * @param msg
	 * @return      
	 */ 
	public byte makeCheckSum(byte[] msg) {
		byte sum = 0;
		for (int i = 0; i < msg.length; i++) {
			sum ^= (byte) (msg[i] & 0xff);
		}
		return sum;
	}

	/**   校验“校验值” 按异或
	 * @param msg
	 * @param ssum
	 * @return      
	 */ 
	public boolean validateCheckSum(byte[] msg, byte ssum) {
		byte csum = makeCheckSum(msg);
		if (csum == ssum) {
			return true;
		}
		return false;
	}

	public byte[] encodeTrans(byte[] msg) {
		//20170605 国标不需要转义

//		byte tranbyte = 0x23;
//		for (int i = 1; i < msg.length - 1; i++) {
//			if (msg[i] == tranbyte && msg[i + 1] == tranbyte) {
//				msg[i] = (byte) 0xff;
//				msg[i + 1] = (byte) 0xfc;
//			}
//		}
		return msg;
	}

	/**
	 * 
	 * @Title: decodeTrans   
	 * @Description: TODO 特殊字节转义 
	 * @param msg
	 * @return      
	 *      
	 * @throws
	 */
	public byte[] decodeTrans(byte[] msg) {
		//20170605 国标不需要转义
//		byte tranbyte = (byte) 0xff;
//		for (int i = 1; i < msg.length - 1; i++) {
//			if (msg[i] == tranbyte && msg[i + 1] == (byte) 0xfc) {
//				msg[i] = 0x23;
//				msg[i + 1] = 0x23;
//			}
//		}
		return msg;
	}

}
