package com.axgrid.lock.repository;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Repository
public class AxLockRepository {

    /**
     * Хранит константу времени,
     * нужно чтоб не создавать постоянно объект Date()
     */
    static long dateTick = new Date().getTime();

    @EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
    @Data
    static final class AxLock extends ReentrantLock {
        long updateDate;

        @EqualsAndHashCode.Include
        final String key;

        private static final long ttl = 3_600_000; //Час

        public boolean isOutOfDate() {
            return (this.updateDate + ttl) < dateTick;
        }

        public AxLock update() {
            this.updateDate = dateTick;
            return this;
        }

        public AxLock(String key) {
            this.key = key;
            updateDate = dateTick;
        }
    }

    Map<String, AxLock> lockMap = new ConcurrentHashMap<>();
    Queue<AxLock> timeoutQueue = new ConcurrentLinkedQueue<>();

    @Scheduled(fixedDelay = 10000)
    public void updateMetrics() {
        if (log.isTraceEnabled()) log.trace("AxLockRepository:{}", lockMap.size());
        while (timeoutQueue.size() > 0 && timeoutQueue.peek().isOutOfDate()) {
            AxLock l = timeoutQueue.poll();
            if (l.isLocked()) {
                log.warn("!Remove locked LOCK! {}", l.key);
                l.unlock();
            }
            lockMap.remove(l.key);
        }
    }

    public int size() { return lockMap.size(); }

    public void lock(String key) {
        Lock l = lockMap.compute(key, (k, v) -> v == null ? new AxLock(k) : v.update());
        l.lock();
    }

    public void release(String key) {
        lockMap.computeIfPresent(key, (k, v) -> {
            timeoutQueue.removeIf(item -> item == v);
            timeoutQueue.add(v);
            v.update().unlock();
            return v;
        });
    }

    private AxLock remove(String key) {
        return lockMap.compute(key, (k,v) -> {
            if (v == null) return null;
            if (v.isLocked() || v.getQueueLength() > 0) return v;
            return null;
        });
    }

    /**
     * Только для тестов
     * @return
     */
    public void removeAll() {
        lockMap.clear();
    }


}
