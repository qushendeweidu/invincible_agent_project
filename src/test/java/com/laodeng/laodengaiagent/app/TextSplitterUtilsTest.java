package com.laodeng.laodengaiagent.app;

import com.laodeng.laodengaiagent.utils.TextSplitterUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author laodeng
 * @version v1.0
 * @date 2026/3/3 14:10
 * @description
 */
@Log4j2
@SpringBootTest
public class TextSplitterUtilsTest {
    @Autowired
    private TextSplitterUtils textSplitterUtils;

    @Test
    void testTextSplitter() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:document/*/*.md");

        for (Resource resource : resources) {
            textSplitterUtils.updateDoc(resource);

            log.info("成功处理并包装文件: {}", resource.getFilename());
        }
    }


}
