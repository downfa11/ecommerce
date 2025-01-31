package ns.example.ecommerce.ecommerce.config;

import java.util.HashMap;
import java.util.Map;
import ns.example.ecommerce.ecommerce.utils.serializer.PurchaseLogSerializer;
import ns.example.ecommerce.ecommerce.utils.serializer.AdLogSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfig {

    @Bean
    public static KafkaTemplate<String, Object> kafkaTemplateForGeneral() {
        return new KafkaTemplate<>(producerFactoryForGeneral());
    }

    @Bean
    public static ProducerFactory<String, Object> producerFactoryForGeneral() {
        Map<String, Object> produceConfig = new HashMap<>();
        produceConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:10000");
        produceConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        produceConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(produceConfig);
    }

    @Bean
    public static KafkaTemplate<String, Object> kafkaTemplateForAdvertisement() {
        return new KafkaTemplate<>(producerFactoryForAdvertisement());
    }

    @Bean
    public static ProducerFactory<String, Object> producerFactoryForAdvertisement() {
        Map<String, Object> produceConfig = new HashMap<>();
        produceConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:10000");
        produceConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        produceConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AdLogSerializer.class);
        return new DefaultKafkaProducerFactory<>(produceConfig);
    }

    @Bean
    public static KafkaTemplate<String, Object> kafkaTemplateForPurchaseLog() {
        return new KafkaTemplate<>(producerFactoryForPurchaseLog());
    }

    @Bean
    public static ProducerFactory<String, Object> producerFactoryForPurchaseLog() {
        Map<String, Object> produceConfig = new HashMap<>();
        produceConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:10000");
        produceConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        produceConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, PurchaseLogSerializer.class);
        return new DefaultKafkaProducerFactory<>(produceConfig);
    }

    @Bean
    public static KafkaTemplate<String, Object> kafkaTemplateForAdToPurchase() {
        return new KafkaTemplate<>(producerFactoryForAdToPurchase());
    }

    @Bean
    public static ProducerFactory<String, Object> producerFactoryForAdToPurchase() {
        Map<String, Object> produceConfig = new HashMap<>();
        produceConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:10000");
        produceConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        produceConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AdLogSerializer.class);
        return new DefaultKafkaProducerFactory<>(produceConfig);
    }
}
