package com.laodeng.laodengaiagent.config;

import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.springframework.ai.vectorstore.elasticsearch.SimilarityFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/9 16:26
 * @description
 */

@Configuration
public class VectorStoreConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Bean
    public RestClient restClient() {
        String httpHeader = elasticsearchUri.startsWith("https") ? "https" : "http";
        if (httpHeader.equals("https")){
            String host = elasticsearchUri.replace("https://", "").split(":")[0];
            int port = Integer.parseInt(elasticsearchUri.replace("https://", "").split(":")[1]);
            return RestClient.builder(new HttpHost(host, port, "https")).build();
        } else if (httpHeader.equals("http")) {
            String host = elasticsearchUri.replace("http://", "").split(":")[0];
            int port = Integer.parseInt(elasticsearchUri.replace("http://", "").split(":")[1]);
            return RestClient.builder(new HttpHost(host, port, "http")).build();
        }else {
            return null;
        }
    }

    @Bean
    public RestClientTransport restClientTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchVectorStore vectorStore(
            RestClient restClient,
            EmbeddingModel embeddingModel) {
        ElasticsearchVectorStoreOptions options = new ElasticsearchVectorStoreOptions();
        options.setIndexName("custom-index-v2");
        options.setDimensions(1024);
        options.setSimilarity(SimilarityFunction.cosine);
        return ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true)
                .build();
    }
}
