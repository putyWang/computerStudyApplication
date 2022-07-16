package com.learning.es.processor;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.learning.es.ElasticManager;
import com.learning.es.constants.ElasticConst;
import com.learning.es.model.CanalEntryModel;
import com.learning.es.model.elastic.ElasticDocModel;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * canal数据消息队列执行工具
 */
@Slf4j
public final class CanalProcessor {
    // 批量处理数据的大小
    private final static int batchSize = 5000;
    // 线程池线程数量
    private final static int poolSize = 5;
    //插入数据的时间间隔，每间隔intervalTime的时间自动插入一次
    private final static long intervalTime = 1000*60*5;

    private static CanalProcessor processor;

    private final CanalDataProcessInterface dataProcess;
    /*
     * 当前激活的线程数量计数器
     */
    private static final AtomicInteger activeThreadCount = new AtomicInteger(0);
    /**
     * 
     * 初始化队列， 作为数据缓存池。
     * 缓存池的大小为：batchSize * (poolSize + 1)
     */
    private static final BlockingQueue<CanalEntryModel> blockingQueue = new LinkedBlockingQueue<>(batchSize * (poolSize + 20));
    /**
     * 可重用固定个数的线程池
     * 可控制线程最大并发数，超出的线程会在队列中等待,当处理完一个马上就会去接着处理排队中的任务
     */
    private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolSize);

    public CanalProcessor(CanalDataProcessInterface dataProcess) {
        this.dataProcess = dataProcess;
    }

    /**
     * 获取单例实例
     *
     * @return ElasticBulkProcessor
     */
    public static CanalProcessor getInstance(CanalDataProcessInterface comsume) {
        if (null == processor) {
            // 多线程同步
            synchronized (CanalProcessor.class) {
                if (null == processor) {
                    processor = new CanalProcessor(comsume);
                }
            }
        }

        return processor;
    }

    /**
     * 同步执行add,往队列中添加一条数据
     *
     * @param entry
     */
    public synchronized void add(CanalEntryModel entry) {
        try {
            if (entry != null){
                log.info("向canal消息队列插入一条数据：" + entry.toString());
                // 将指定元素插入此队列中，将等待可用的空间.当>maxSize 时候，阻塞，直到能够有空间插入元素
                blockingQueue.put(entry);
                // 执行线程
                execute();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 线程池执行
     */
    private void execute() {
        // 获取当前活动的线程数
        int curActiveCount = activeThreadCount.get();
        // 如果激活的线程池为0，创建一个新的线程
        Future<Long> future;

        if (curActiveCount == 0) {
            ExecuteClass executeClass = new ExecuteClass();
            // 开启一个线程，和execute区别为有返回值
            future = fixedThreadPool.submit(executeClass);
            activeThreadCount.incrementAndGet();
        } else if (blockingQueue.size() >= batchSize) {
            // 如果blockingQueue队列中的数量大于batchSize， 则创建一个新的线程
            int freeThreadCount = poolSize - curActiveCount;
            if (freeThreadCount >= 1) {
                ExecuteClass executeClass = new ExecuteClass();
                future = fixedThreadPool.submit(executeClass);
                activeThreadCount.incrementAndGet();
            }
        }

    }

    /**
     * 实现Callable可以返回现线程执行结果
     * 返回结果为执行成功的数量
     */
    class ExecuteClass
            implements Callable<Long> {
        @Override
        public Long call() throws Exception {
            log.info("start thread -" + Thread.currentThread().getName());
            // 空闲时间
            long freeTime = 0;
            long sleep = 100;
            long longSleep = 1000*60;
            List<CanalEntryModel> entries;

            // 无限循环从blockQueue中取数据
            while (true) {
                try{
                    ClusterHealthResponse clusterHealthResponse = ElasticManager.cluster().clusterHealth();
                    // 如果es断开连接则消息队列等待到es重新连上后再执行
                    if (clusterHealthResponse == null){
                        // 重置es连接
                        ElasticManager.reset();
                        // 等待1分钟
                        Thread.sleep(longSleep);
                        freeTime += sleep;
                    }else {

                        // 只要消息队列有数据就立即执行
                        if (!CollectionUtils.isEmpty(blockingQueue) && blockingQueue.size() >= 1) {
                            freeTime = 0;
                            entries = new ArrayList<>();
                            blockingQueue.drainTo(entries, batchSize);
                            if (!CollectionUtils.isEmpty(entries)) {
                                // 消费者进行数据处理
                                List<ElasticDocModel> elasticDocModels = dataProcess.dataProcess(entries);
                                // 将数据插入es
                                consume(elasticDocModels);
                            }
                        } else {
                            // 等待100ms
                            Thread.sleep(sleep);
                            freeTime += sleep;
                        }
                    }

                    // 如果总空闲时间超过5分钟， 结束当前线程
                    if (freeTime >= intervalTime) {
                        log.info("stop Thread-" + Thread.currentThread().getName());
                        activeThreadCount.decrementAndGet();
                        break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    activeThreadCount.decrementAndGet();
                    break;
                }
            }

            return null;
        }

    }

    /**
     * 将数据批量插入es或删除
     * @param elasticDocModels
     */
    private void consume(List<ElasticDocModel> elasticDocModels) {
        if (!CollectionUtils.isEmpty(elasticDocModels)){
            log.info("批量消费canal消息队列数据：" + elasticDocModels.toString());
            String docId = ElasticConst.ELASTIC_FIELD_DOC_ID;
            String empiField = ElasticConst.ELASTIC_FIELD_EMPI;
            // 判断索引不存在则新建索引
//            List<String> indexs = elasticDocModels.stream()
//                    .map(ElasticDocModel::getIndex).distinct().collect(Collectors.toList());
//            try {
//                for (String index : indexs) {
//                    if (!FormElasticManager.index().indexExists(index)){
//                        FormElasticManager.index().addIndex(index, new ElasticSetting());
//                    }
//                }
//            } catch (Exception e) {
//                log.error("es连接异常", e);
//            }
            // 将数据按新增、修改及删除分类
            for (ElasticDocModel elasticDocModel : elasticDocModels) {
                CanalEntry.EventType eventTypeEnum = elasticDocModel.getEventTypeEnum();
                switch (eventTypeEnum){
                    case INSERT:
                    case UPDATE:
                        // 同步更新表单填写数据至es
                        ElasticManager.document().bulkInsert(elasticDocModel.getIndex(), elasticDocModel.getDocList());
                        break;
                    case DELETE:
                        List<Map<String, Object>> docList = elasticDocModel.getDocList();
                        if (!CollectionUtils.isEmpty(docList)){
                            List<String> ids = docList.stream().map(p -> p.getOrDefault(docId, "").toString())
                                    .filter(p -> !StringUtils.isEmpty(p)).collect(Collectors.toList());

                            // 删除指定id es文档
                            ElasticManager.document().deleteByDocIds(elasticDocModel.getIndex(), ids);

                        }
                        break;
                    default:
                        break;
                }
            }
        }

    }

}