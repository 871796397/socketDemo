package com.minafile.codec;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Random;

import javax.swing.plaf.SliderUI;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minafile.model.ByteFileMessage;

/**
 * 客户端把消息文件实体封装发送给服务器
 * 文件名文件路径在handle中处理 
 * handle从配置文件中获取到对应文件
 * 当把消息封装好之后，发送给服务器，服务器对文件解析。 
 *
 */
public class ByteProtocalEncoder extends ProtocolEncoderAdapter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ByteProtocalEncoder.class);
	// private  AttributeKey TRANSPORTDATA = new AttributeKey(getClass(), "transportData"); //传输的总字节数
	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		LOGGER.info("客户端开始编码，准备发往服务器……");
	/*	int s = (Integer)session.getAttribute("count");
		LOGGER.info("客户端将要发送" + s);
		int i = new Random().nextInt(10);
		if(i % 2 == 0){
			Thread.sleep(1000);
		}*/
		CharsetEncoder character = Charset.forName("UTF-8").newEncoder();
		ByteFileMessage bfm = (ByteFileMessage) message;
		// 输入流
		// 处理流部分
		File file = new File(bfm.getFilePath());
		bfm.setFileName(file.getName()); // 文件名
		bfm.setFileStreamLength(bfm.getFileName().getBytes().length);
		
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate((int)channel.size());
		byteBuffer.clear();
		channel.read(byteBuffer);
		
		// Mina处理部分
		IoBuffer ioBuffer = IoBuffer.allocate(
				// 12 = 4+4+4
				(int) channel.size() + 12 + bfm.getFileName().getBytes().length
						+ bfm.getFilePath().getBytes().length).setAutoExpand(
				true);
		LOGGER.info("【客户端】文件大小：" + channel.size());
		LOGGER.info("【客户端】文件名大小：" + bfm.getFileName().getBytes().length);
		ioBuffer.putInt(bfm.getFileName().getBytes().length);
		ioBuffer.putInt((int)channel.size());
		
		ioBuffer.putString(bfm.getFileName(),character); // 文件名
		ioBuffer.putInt(bfm.getSeq()); // 序号
		ioBuffer.put(byteBuffer);
		ioBuffer.put(byteBuffer.array()); // bfm.getFileStream()
		
		
		
		ioBuffer.flip();
		
		out.write(ioBuffer);
	}

}
