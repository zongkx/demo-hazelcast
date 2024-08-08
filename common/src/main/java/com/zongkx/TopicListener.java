package com.zongkx;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TopicListener implements MessageListener<String> {

    @Override
    public void onMessage(Message<String> message) {
        Object msg = message.getMessageObject();
        log.info("订阅者，收到Topic消息：{}", msg);
    }
}
