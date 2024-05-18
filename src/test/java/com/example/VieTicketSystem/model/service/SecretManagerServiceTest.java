package com.example.VieTicketSystem.model.service;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

public class SecretManagerServiceTest {
    @Test
    public void testAccessSecret() throws IOException {
        // Arrange
        String projectId = "myProjectId";
        String secretId = "mySecretId";
        String secretValue = "mySecretValue";

        AccessSecretVersionResponse response = AccessSecretVersionResponse.newBuilder()
                .setPayload(AccessSecretVersionResponse.Payload.newBuilder()
                        .setData(ByteString.copyFromUtf8(secretValue)))
                .build();

        when(client.accessSecretVersion(SecretVersionName.of(projectId, secretId, "latest"))).thenReturn(response);

        SecretManagerService service = new SecretManagerService();

        // Act
        String result = service.accessSecret(projectId, secretId);

        // Assert
        assertEquals(secretValue, result);
    }
}