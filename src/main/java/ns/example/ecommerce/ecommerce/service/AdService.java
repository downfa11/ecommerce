package ns.example.ecommerce.ecommerce.service;

import static ns.example.ecommerce.ecommerce.domain.enums.KafkaTopicToConfig.JOIN_AD_TO_PURCHASE;

import ns.example.ecommerce.ecommerce.domain.LogData.AdLog;
import ns.example.ecommerce.ecommerce.domain.LogData.PurchaseProductLog;
import ns.example.ecommerce.ecommerce.domain.stream.AdConversionResult;
import ns.example.ecommerce.ecommerce.domain.enums.KafkaTopicToConfig;
import ns.example.ecommerce.ecommerce.domain.LogData.PurchaseLog;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdService {

    private static final String AD_LOG_TOPIC = "adLog";
    private static final String PURCHASE_LOG_TOPIC = "purchaseLog";
    private static final String PURCHASE_LOG_ONE_PRODUCT_TOPIC = "purchaseLogOneProduct";
    private static final String AD_EVALUATION_COMPLETE_TOPIC = "AdEvaluationComplete";

    private static final Serde<AdLog> AD_LOG_SERDE = createSerde(AdLog.class);
    private static final Serde<PurchaseLog> PURCHASE_LOG_SERDE = createSerde(PurchaseLog.class);
    private static final Serde<PurchaseProductLog> PURCHASE_LOG_ONE_PRODUCT_SERDE = createSerde(PurchaseProductLog.class);
    private static final Serde<AdConversionResult> EFFECT_OR_NOT_SERDE = createSerde(AdConversionResult.class);

    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public void topologyPipeline(StreamsBuilder sb) {
        // 중복 Join할 필요 없으므로 최신 데이터만 가져오는 KTable 이용
        KTable<String, AdLog> adTable = createAdTable(sb);

        // 여러 상품들의 구매 이력(PurchaseLog)을 각 상품별 구매 이력(PurchaseProductLog)으로 정제
        sb.stream(PURCHASE_LOG_TOPIC, Consumed.with(Serdes.String(), PURCHASE_LOG_SERDE))
                .flatMapValues(this::extractPurchasesToProduct)
                .foreach((k, v) -> sendMessageFor(JOIN_AD_TO_PURCHASE, v));

        KTable<String, PurchaseProductLog> purchaseToProductTable = createPurchaseProductLogTable(sb);

        // 광고 데이터(AdTable)과 각 상품별 구매 이력(purchaseToProductTable)을 Join -> 광고 전환율 데이터(AdConversionResult) 수집
        adTable.join(purchaseToProductTable, this::createAdConversionResult)
                .toStream()
                .to(AD_EVALUATION_COMPLETE_TOPIC, Produced.with(Serdes.String(), EFFECT_OR_NOT_SERDE));
    }


    // 재사용을 위해서 정적으로 Serde 관리
    private static <T> Serde<T> createSerde(Class<T> type) {
        return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(type));
    }


    // 광고 실제 시청 시간이 10초 이상인 경우만 유의미한 지표로 사용해서 KTable에 담는다
    private KTable<String, AdLog> createAdTable(StreamsBuilder sb) {
        return sb.stream(AD_LOG_TOPIC, Consumed.with(Serdes.String(), AD_LOG_SERDE))
                .selectKey((k, v) -> v.getUserId() + "_" + v.getProductId())
                .filter((k, v) -> Integer.parseInt(v.getWatchingTime()) > 10) // filtering
                .toTable(Materialized.<String, AdLog, KeyValueStore<Bytes, byte[] >> as("adStore")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(AD_LOG_SERDE));
    }


    // 구매 이력(PurchaseLog)에서 각 상품별로 구매 이력(PurchaseProductLog)로 분리
    // 단, 가격이 100만원 이상의 고가 상품은 제외해서 필터링한다. >> 광고 구매력
    private List<PurchaseProductLog> extractPurchasesToProduct(PurchaseLog v) {
        List<PurchaseProductLog> purchaseLogs = new ArrayList<>();
        for (Map<String, String> prodInfo : v.getProductInfo()) {
            if (Integer.parseInt(prodInfo.get("price")) < 1000000) {
                purchaseLogs.add(new PurchaseProductLog(v.getUserId(), prodInfo.get("productId"), v.getOrderId(), prodInfo.get("price"), v.getPurchasedDate()));
            }
        }
        return purchaseLogs;
    }


    private KTable<String, PurchaseProductLog> createPurchaseProductLogTable(StreamsBuilder sb) {
        return sb.stream(PURCHASE_LOG_ONE_PRODUCT_TOPIC, Consumed.with(Serdes.String(), PURCHASE_LOG_ONE_PRODUCT_SERDE))
                .selectKey((k, v) -> v.getUserId() + "_" + v.getProductId())
                .toTable(Materialized.<String, PurchaseProductLog, KeyValueStore<Bytes, byte[]>>as("purchaseLogStore")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(PURCHASE_LOG_ONE_PRODUCT_SERDE));
    }

    private AdConversionResult createAdConversionResult(AdLog adLog, PurchaseProductLog purchaseProductLog) {
        return new AdConversionResult(
                purchaseProductLog.getUserId(),
                adLog.getAdvertisementId(),
                purchaseProductLog.getOrderId(),
                Map.of("productId", purchaseProductLog.getProductId(), "price", purchaseProductLog.getPrice()));
    }

    public void sendMessageFor(KafkaTopicToConfig topic, Object message){
        kafkaTemplate = topic.getKafkaTemplate();
        kafkaTemplate.send(topic.name(), message);
    }
}