package com.example.VieTicketSystem.model.service;

import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretName;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import java.io.IOException;


public class SecretManagerService {
    public void storeSecret(String projectId, String secretId, String secretValue) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            Secret secret = client.createSecret(
                    ProjectName.of(projectId),
                    secretId,
                    Secret.newBuilder()
                            .setReplication(Replication.newBuilder()
                                    .setAutomatic(Replication.Automatic.newBuilder().build())
                                    .build())
                            .build());

            SecretVersion secretVersion = client.addSecretVersion(
                    SecretName.of(projectId, secretId),
                    SecretPayload.newBuilder()
                            .setData(ByteString.copyFromUtf8(secretValue))
                            .build());
        } catch (IOException e) {
            System.out.println("Error storing secret: " + e.getMessage());
        }
    }

    public String accessSecret(String projectId, String secretId) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            return response.getPayload().getData().toStringUtf8();
        } catch (IOException e) {
            System.out.println("Error accessing secret: " + e.getMessage());
            return null;
        }
    }
}
