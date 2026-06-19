package com.ems.paymentservice.receipt

import com.ems.paymentservice.config.ReceiptProperties
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception

@Component
class S3ReceiptStorage(
    private val s3Client: S3Client,
    private val properties: ReceiptProperties,
) : ReceiptStorage {
    override fun store(objectKey: String, content: ByteArray): StoredReceipt {
        ensureBucketExists()
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(properties.bucket)
                .key(objectKey)
                .contentType("application/pdf")
                .build(),
            RequestBody.fromBytes(content),
        )
        return StoredReceipt(
            objectKey = objectKey,
            url = "${properties.publicBaseUrl.trimEnd('/')}/$objectKey",
        )
    }

    private fun ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket).build())
        } catch (_: NoSuchBucketException) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket).build())
        } catch (exception: S3Exception) {
            if (exception.statusCode() != 404) {
                throw exception
            }
            s3Client.createBucket(CreateBucketRequest.builder().bucket(properties.bucket).build())
        }
    }
}
