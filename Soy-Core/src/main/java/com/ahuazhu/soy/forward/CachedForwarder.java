package com.ahuazhu.soy.forward;

import com.ahuazhu.soy.cache.Cache;
import com.ahuazhu.soy.cache.MessageCache;
import com.ahuazhu.soy.modal.QuestionKey;
import com.ahuazhu.soy.modal.ResponseContext;
import org.xbill.DNS.Message;

import java.io.IOException;

/**
 * Created by zhuzhengwen on 2017/4/20.
 */
public class CachedForwarder implements Forwarder {

    private Cache<QuestionKey, Message> cache;

    Upstream upstream ;

    public CachedForwarder() {
        cache = new MessageCache();
        upstream = new UdpUpstream("8.8.8.8", 53);
        upstream.establish();
    }

    @Override
    public void forward(Message message, ResponseContext response) throws IOException {
        Message answer = cache.getValue(new QuestionKey(message));
        if (answer != null) {
            send(answer, response);
            return;
        }
        upstream.ask(message, new WriteHandler(response.getWriter()));
    }

    private void send(Message message, ResponseContext responseContext) throws IOException {
        responseContext.getWriter().write(message.toWire());
    }
}
