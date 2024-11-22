package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.io.File;

public class StorageUploader {

    private BlobContainerClient containerClient;

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString("DefaultEndpointsProtocol=https;" +
                        "AccountName=" +
                            "sherrycsc311storage;" +
                        "AccountKey=" +
                            "2ynEpyVd/JKDPiFu0JwAq2dlVThFXWtsxpErgacuwH9XNGUs6kp4OYJ5pFXmU8HWZ6IAVXVpaaXn+AStcmKJcw==;" +
                        "EndpointSuffix=" +
                            "core.windows.net")
                .containerName("media-files")
                .buildClient();
    }

    public BlobContainerClient getContainerClient(){
        return containerClient;
    }
}
