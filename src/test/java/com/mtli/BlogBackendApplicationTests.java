package com.mtli;


import com.mtli.service.CodeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.OutputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BlogBackendApplicationTests {

    @Autowired
    CodeService codeService;

    //    向数据库中插入100条激活码
//    @Test
//    public void uuidTest(){
//        for (int i=0 ; i<100; i++) {
//            String str = codeService.generateCode().getId();
//            System.out.println(str);
//        }
//    }
    @Test
    public void contextLoads() {
    }

//    @Test(expected = IOException.class)//期望报IO异常
////    public void when_thenThrow() throws IOException{
////        OutputStream mock = Mockito.mock(OutputStream.class);
////        //预设当流关闭时抛出异常
////        Mockito.doThrow(new IOException()).when(mock).close();
////        mock.close();
////    }

}
