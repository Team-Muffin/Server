package com.pda.boardapplication.config;

import com.pda.kafkautils.KafkaJson;
import com.pda.kafkautils.KafkaUtils;
import com.pda.kafkautils.alert.AlertMessageDto;
import com.pda.kafkautils.credit.AddCreditDto;
import com.pda.kafkautils.user.UserUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class ProducerConfig {
    @Value("${spring.kafka.producer.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, KafkaJson> factory() {
        Map<String, Object> config = new HashMap<>();
        config.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());
        config.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.TYPE_MAPPINGS, KafkaUtils.getJsonTypeMappingInfo(
                AddCreditDto.class, AlertMessageDto.class, UserUpdateDto.class));
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, KafkaJson> kafkaTemplate(){
        return new KafkaTemplate<>(factory());
    }
}