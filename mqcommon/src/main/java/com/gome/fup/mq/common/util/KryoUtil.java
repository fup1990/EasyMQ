package com.gome.fup.mq.common.util;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo序列化工具类
 *
 * @author fupeng-ds
 */
public class KryoUtil {

	private static Kryo kryo = new Kryo();
	
	public static <T> T byteToObj(byte[] buffer, Class<T> type) {
		kryo.setReferences(false);
		kryo.setRegistrationRequired(false);
		Input input = new Input(buffer);
		return kryo.readObject(input, type);
	}
	
	public static <T> byte[] objToByte(T t) {
		kryo.setReferences(false);
		kryo.setRegistrationRequired(false);
		Output output = new Output(new ByteArrayOutputStream());
		kryo.writeObject(output, t);
		return output.toBytes();
	}
}
