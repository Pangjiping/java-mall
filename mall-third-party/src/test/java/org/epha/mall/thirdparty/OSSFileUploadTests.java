package org.epha.mall.thirdparty;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;

@SpringBootTest
public class OSSFileUploadTests {

    private String bucketName = "jmall-pjp";

    @Autowired
    OSSClient ossClient;

    @Test
    public void testSimpleFileUpload() throws Exception {
        String objectName = "exampledir/test.txt";
        String filePath = "/Users/pangjiping/test.txt";

        // 测试文件上传
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, fileInputStream);

            putObjectRequest.setProcess("true");
            PutObjectResult result = ossClient.putObject(putObjectRequest);

            // 如果上传成功，返回200
            System.out.println(result.getResponse().getStatusCode());
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }
    }
}
