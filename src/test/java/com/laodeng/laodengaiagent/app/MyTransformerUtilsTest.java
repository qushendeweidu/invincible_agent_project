package com.laodeng.laodengaiagent.app;

import com.laodeng.laodengaiagent.utils.MyTransformerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyTransformerUtilsTest {
    @Autowired
    private MyTransformerUtils myTransformerUtils;

    @Test
    void reWriteTransformer() {
        String input = "啊啊啊啊啊啊啊鱼皮是哪个？";
        String output = myTransformerUtils.reWriteTransformer(input);
        System.out.println("打印的输出结果"+output);
    }


}