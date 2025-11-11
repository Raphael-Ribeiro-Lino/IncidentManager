package br.com.incidentemanager.helpdesk.services;

import java.io.InputStream;
import java.time.Duration;

import org.springframework.stereotype.Service;

import br.com.incidentemanager.helpdesk.exceptions.S3SaveFileErrorException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

	private final S3Client s3;

	public S3Service() {
		String accessKey = System.getenv("BUCKET_ACCESS_KEY");
		String secretKey = System.getenv("BUCKET_SECRET_KEY");
		String regionEnv = System.getenv("AWS_REGION");

		Region region = Region.of(regionEnv != null ? regionEnv : "us-east-2");

		if (accessKey != null && secretKey != null) {
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
			this.s3 = S3Client.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(awsCreds))
					.build();
		} else {
			this.s3 = S3Client.builder().region(region)
					.credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
		}
	}

	public String saveFile(String key, InputStream inputStream, long contentLength, String contentType,
			String bucketName) {
		try {
			key = key.trim().replaceAll("\\s+", "-");

			s3.putObject(
					PutObjectRequest.builder().bucket(bucketName).key(key)
							.contentType(contentType != null ? contentType : "application/octet-stream").build(),
					RequestBody.fromInputStream(inputStream, contentLength));

			return generatePresignedUrl(key, bucketName);
		} catch (S3Exception e) {
			System.err.println("Erro ao salvar arquivo no S3: " + e.getMessage());
			throw new S3SaveFileErrorException();
		}
	}

	public String generatePresignedUrl(String key, String bucketName) {
		try {
			Region region = s3.serviceClientConfiguration().region();

			S3Presigner presigner = S3Presigner.builder().region(region)
					.credentialsProvider(s3.serviceClientConfiguration().credentialsProvider()).build();

			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10)).getObjectRequest(getObjectRequest).build();

			String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

			presigner.close();
			return presignedUrl;

		} catch (Exception e) {
			System.err.println("Erro ao gerar URL temporária: " + e.getMessage());
			return null;
		}
	}
}
