package com.rfonseca.example.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

@ExtendWith(MockitoExtension.class)
public class TestBusLogFileActivity {
    static class ExecutionContextMock implements ExecutionContext {

        @InjectMocks
        Logger logger;

        @Override
        public Logger getLogger() {
            return logger;
        }

        @Override
        public String getInvocationId() {
            return "invocation_id";
        }

        @Override
        public String getFunctionName() {
            return "function_name";
        }
    }

    @InjectMocks
    private ExecutionContextMock executionContext;

    @Mock
    private Logger logger;

    @BeforeEach
    void init() {

        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            System.out.println("Called with arguments: " + Arrays.toString(args));
            return null;
        }).when(logger).info(anyString());
    }

    @Test
    @DisplayName("Test recovering file from the bus")
    void testRecoveryBusLogFiles() {

        try {

            BusLogFileActivity spyBusLogFileActivity = spy(new BusLogFileActivity());

            doReturn(Arrays.asList(
                    mockCloudBlob("file1.txt"),
                    mockCloudBlob("file2.txt"))).when(spyBusLogFileActivity).listBlobs();

            // Call the method to be tested
            List<String> result = spyBusLogFileActivity.recoveryBusLogFiles("21/10/2023", executionContext);

            // Verify the expected behavior
            assertEquals(Arrays.asList("file1.txt", "file2.txt"), result);

        } catch (InvalidKeyException | URISyntaxException | StorageException e) {
            // TODO Auto-generated catch block

            fail("Exception received: " + e.toString() + " " + e.getStackTrace().toString());
        }
    }

    @Test
    @DisplayName("Test processing files")
    void processBusLogFile() {
        try {
            // Arrange
            BusLogFileActivity spyBusLogFileActivity = spy(new BusLogFileActivity());

            // Mock the behavior of getReaderFileName
            BufferedReader mockedReader = Mockito.mock(BufferedReader.class);

            doReturn(mockedReader).when(spyBusLogFileActivity).getReaderFileName("example.txt");

            // Mock the behavior of readLine
            when(mockedReader.readLine()).thenReturn("line1", "line2", null);

            // Act
            int result = spyBusLogFileActivity.processBusLogFile("example.txt", executionContext);

            // Assert
            assertEquals(2, result); // Assuming 2 lines in the mocked file

        } catch (InvalidKeyException | URISyntaxException | StorageException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private CloudBlockBlob mockCloudBlob(String fileName) {
        CloudBlockBlob mockBlob = mock(CloudBlockBlob.class);
        when(mockBlob.getName()).thenReturn(fileName);
        return mockBlob;
    }

}
