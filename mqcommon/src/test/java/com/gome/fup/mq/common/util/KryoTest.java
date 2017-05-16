package com.gome.fup.mq.common.util;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.gome.fup.mq.common.http.Request;

/**
 * kryo序列化测试
 *
 * @author fupeng-ds
 */
public class KryoTest {

	public static void main(String[] args) {
		//序列化
		Kryo kryo = new Kryo();
		Request request = new Request();
		request.setId("abc");
		request.setGroupName("test");
		request.setMsg("测试");
		//Output output = new Output(new byte[2046]);
		Output output = new Output(new ByteArrayOutputStream());
		kryo.writeObject(output, request);
		byte[] bytes = output.toBytes();
		//反序列化
		Input input = new Input(bytes);
		Request result = kryo.readObject(input, Request.class);
		System.out.println(result);
	}
}
