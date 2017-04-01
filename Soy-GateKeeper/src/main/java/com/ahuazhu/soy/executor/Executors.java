package com.ahuazhu.soy.executor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.ahuazhu.soy.Soy;
import com.ahuazhu.soy.modal.Query;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhengwenzhu on 2017/3/31.
 */
public enum Executors implements Executor {
    SyncExecutor {
        @Override
        public void execute(Query query) {
            Soy.fire(query);
        }
    },

    ThreadPoolExecutor {
        private final ExecutorService es = createBlockingExecutors(10);
        private Logger LOGGER = Logger.getLogger(ThreadPoolExecutor.class);

        @Override
        public void execute(Query query) {
            es.execute(() -> Soy.fire(query));
        }

        ExecutorService createBlockingExecutors(int size) {
            return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(10000) {
                        @Override
                        public boolean offer(Runnable e) {
                            try {
                                this.put(e);
                            } catch (Exception e1) {
                                LOGGER.error(e1);
                            }
                            return true;
                        }
                    });
        }
    },

    Akka {
        private ActorSystem system = ActorSystem.create("Soy-Actor");
        private ActorRef soyActor = system.actorOf(Props.create(SoyActor.class));

        @Override
        public void execute(Query query) {
            soyActor.tell(query, soyActor);
        }

        class SoyActor extends AbstractActor {

            @Override
            public Receive createReceive() {
                return ReceiveBuilder.create().match(Query.class, Soy::fire).build();
            }
        }
    },

}