package com.learning.es.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.learning.es.ElasticManager;
import com.learning.es.processor.CanalDataProcessInterface;
import com.learning.es.model.CanalEntryModel;
import com.learning.es.processor.CanalProcessor;
import com.learning.es.utils.AnalysisCanalEntry;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * canal客户端配置类
 *
 * @author wangpenghui
 * @date  2021年08月12日 11:56:00
 */
@Slf4j
@Component
public class CanalClient implements DisposableBean {

    private CanalConnector canalConnector;
    // 只在第一次获取canal连接时去生成连接对象
    private boolean isFirst = true;

    @Resource
    private CanalConfigurationProperties configurationProperties;

    /**
     * 设置canal转ElasticDocModel数据模式
     */
    private CanalDataProcessInterface canalDataProcess;

    /**
     * 获取canal连接
     * @return
     */
    private CanalConnector getCanalConnector() {
        // 第一次没有成功获取连接，则不再去重新获取连接了
        if (isFirst) {
            try {
                if (canalConnector == null) {
                    log.info("----正在获取canal连接----");
                    canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(configurationProperties.getHostname(),
                            configurationProperties.getPort()), configurationProperties.getDestination(), configurationProperties.getUsername(), configurationProperties.getPassword());
                    //连接canalServer
                    canalConnector.connect();
                    //订阅Desctinstion
                    canalConnector.subscribe(configurationProperties.getSubscribeRegex());
                    //回滚寻找上次中断的位置
                    canalConnector.rollback();
                    log.info("----获取canal连接成功----");
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 如果报错，则关闭连接
                destroy();
                log.warn("-----获取canal连接失败----");

            } finally {
                // 如果允许断开连接后重复连接，则会不断的去连接直到成功
                if (!configurationProperties.isRepeatConnect()) {
                    isFirst = false;
                }
            }
        }

        return canalConnector;
    }

    public CanalClient setCanalDataProcess(CanalDataProcessInterface canalDataProcess) {
        this.canalDataProcess = canalDataProcess;

        return this;
    }

    /**
     * 关闭canal连接
     */
    @Override
    public void destroy() {
        if (canalConnector != null) {
            canalConnector.disconnect();
            canalConnector = null;
        }

    }

    /**
     * 批量发布canal日志信息到队列，进行解析处理
     */
    public void batchPublish() {
        long batchId = -1;
        CanalConnector canalConnector = getCanalConnector();
        if (canalConnector != null) {
            try {
                int batchSize = 1000;
                ClusterHealthResponse clusterHealthResponse = ElasticManager.cluster().clusterHealth();
                // 如果es断开连接则消息队列等待到es重新连上后再执行
                if (clusterHealthResponse == null) {
                    // 重置es连接
                    ElasticManager.reset();
                } else {
                    // withOutAck, 告诉canal接收消息后先不ack, 等处理完手动ack
                    Message message = canalConnector.getWithoutAck(batchSize);
                    batchId = message.getId();
                    List<CanalEntry.Entry> entries = message.getEntries();
                    if (batchId != -1 && entries.size() > 0) {
                        entries.forEach(entry -> {
                            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                                //解析处理
                                try {
                                    // 解析日志信息
                                    CanalEntryModel model = AnalysisCanalEntry.parseEntryToModel(entry);
                                    // 添加到消息队列
                                    CanalProcessor.getInstance(canalDataProcess).add(model);
                                } catch (InvalidProtocolBufferException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    log.info("canal: "+entries.size());
                    canalConnector.ack(batchId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    canalConnector.rollback(batchId);
                } catch (CanalClientException canalClientException) {
                    destroy();
                }
            }
        }

    }

}