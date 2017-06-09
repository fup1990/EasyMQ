package com.gome.fup.mq.sender;

import com.gome.fup.mq.common.http.Request;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fupeng-ds on 2017/6/9.
 */
public class AbstractSender implements InitializingBean{

    protected String serverAddr;

    private Disruptor<QueueSender> disruptor;

    private RingBuffer<QueueSender> ringBuffer;

    private int size = 1<<10;

    private ExecutorService executorService = Executors.newFixedThreadPool(16);

    protected void sendRequest(final Request request) {
        long next = ringBuffer.next();
        try {
            QueueSender queueSender = ringBuffer.get(next);
            queueSender.setRequest(request);
            queueSender.setServerAddr(serverAddr);
        } finally {
            ringBuffer.publish(next);
        }
    }

    public Disruptor<QueueSender> getDisruptor() {
        return disruptor;
    }

    public void setDisruptor(Disruptor<QueueSender> disruptor) {
        this.disruptor = disruptor;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public void afterPropertiesSet() throws Exception {
        disruptor = new Disruptor<QueueSender>(new EventFactory<QueueSender>() {
            public QueueSender newInstance() {
                return new QueueSender();
            }
        }, size, executorService);
        disruptor.handleEventsWith(new EventHandler<QueueSender>() {
            public void onEvent(QueueSender queueSender, long l, boolean b) throws Exception {
                queueSender.send();
            }
        });
        ringBuffer = getDisruptor().start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                disruptor.shutdown();
                executorService.shutdown();
            }
        }));
    }

    public RingBuffer<QueueSender> getRingBuffer() {
        return ringBuffer;
    }

    public void setRingBuffer(RingBuffer<QueueSender> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }
}
