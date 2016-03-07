/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foo.bar;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;
import java.util.concurrent.ExecutorService;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onosproject.net.host.HostService;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.core.CoreService;
import org.onosproject.core.ApplicationId;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


/**
 * Skeletal ONOS application component.
 */
@Component(immediate = true)
public class AppComponent {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private ApplicationId appId;
    private ExecutorService eventHandler;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("com.foo.bar");
        eventHandler = newSingleThreadScheduledExecutor(groupedThreads("foo/bar", "event-handler"));
        log.info("Started with Application ID {}", appId.id());
    }

    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);
        eventHandler.shutdown();
        log.info("Stopped");
    }

    public class MyKafkaProducer{

        private final KafkaProducer<String, String> producer;
        private final String topic;
        private final Boolean isAsync;

        public MyKafkaProducer(String topic, Boolean isAsync){
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
            producer = new KafkaProducer<String, String>(props);
            this.topic = topic;
            this.isAsync = isAsync;
        }
        public void message(String key, String value){
            ProducerRecord<String,String> producerRecord = new ProducerRecord<String, String>(topic, key, value);
            if (isAsync) { // Send asynchronously
                producer.send(producerRecord);
            } else { // Send synchronously
                try {
                    producer.send(producerRecord).get();
                    System.out.println("Sent message: (" + key + ", " + value + ")");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        public void close(){
            producer.close();
        }
    }

}
