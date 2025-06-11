package ru.fox.orion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class S3Config {

    @Value("${s3.access.key}")
    private String accessKey;

    @Value("${s3.access.secret}")
    private String accessSecret;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.region}")
    private String region;


    @Bean
    public S3Client s3client() throws URISyntaxException {

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, accessSecret);
        URI uri = new URI(endpoint);
        Region r = Region.of(region);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .region(r)
                .endpointOverride(uri)
                .build();
    }

}