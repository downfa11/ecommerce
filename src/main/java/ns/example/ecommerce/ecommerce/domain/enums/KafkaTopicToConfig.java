package ns.example.ecommerce.ecommerce.domain.enums;

import lombok.AllArgsConstructor;

import ns.example.ecommerce.ecommerce.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;

@AllArgsConstructor
public enum KafkaTopicToConfig {
    GENERAL("general", KafkaConfig.kafkaTemplateForGeneral()),
    ADVERTISEMENT("advertisement", KafkaConfig.kafkaTemplateForAdvertisement()),
    PURCHASE("purchase", KafkaConfig.kafkaTemplateForGeneral()),
    JOIN_AD_TO_PURCHASE("purchaseLogOneProduct", KafkaConfig.kafkaTemplateForAdToPurchase());


    private final String topicName;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public String getTopicName() {
        return topicName;
    }

    public KafkaTemplate<String, Object> getKafkaTemplate() {
        return kafkaTemplate;
    }
}
