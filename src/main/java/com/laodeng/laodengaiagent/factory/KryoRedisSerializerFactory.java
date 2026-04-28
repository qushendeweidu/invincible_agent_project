package com.laodeng.laodengaiagent.factory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/2/25 18:36
 * @description Kryo 线程安全序列化工厂
 */
@Component
@SuppressWarnings("unchecked")
public class KryoRedisSerializerFactory<T> implements RedisSerializer<T> {

    // 使用 ThreadLocal 确保每个线程拥有独立的 Kryo 实例
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 1. 禁用注册表，提高兼容性
        kryo.setRegistrationRequired(false);
        // 2. 支持循环引用
        kryo.setReferences(true);
        // 3. 设置实例化策略（处理无默认构造函数的类）
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        // 4. 预注册常用类（建议注册，虽不是强制，但能减小体积并提高安全性）
        kryo.register(ArrayList.class);
        kryo.register(UserMessage.class);
        kryo.register(AssistantMessage.class);
        kryo.register(SystemMessage.class);

        return kryo;
    });

    /**
     * 序列化方法
     * @param t 需要被序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializationException 抛出序列化异常
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) return new byte[0];

        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Output output = new Output(outputStream)) {
            kryo.writeClassAndObject(output, t);
            output.flush();
            return outputStream.toByteArray();
        }
    }

    /**
     * 反序列化方法
     * @param bytes 需要被反序列化的字节数组
     * @return 反序列化后的对象
     * @throws SerializationException 抛出反序列化异常
     */
    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) return null;

        Kryo kryo = kryoThreadLocal.get();
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            return (T) kryo.readClassAndObject(input);
        }
    }
}