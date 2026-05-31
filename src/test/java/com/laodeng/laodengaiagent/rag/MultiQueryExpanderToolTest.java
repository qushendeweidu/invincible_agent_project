package com.laodeng.laodengaiagent.rag;

import com.laodeng.laodengaiagent.rag.ragTools.MultiQueryExpanderTool;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderToolTest {
    @Autowired
    private MultiQueryExpanderTool multiQueryExpanderTool;

    @Test
    void expand() {
        List<Query> expand = multiQueryExpanderTool.expand("谁是程序员鱼皮啊？");
        for (Query query : expand) {
            System.out.println( query);
        }
    }

}