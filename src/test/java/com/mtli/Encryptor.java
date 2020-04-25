package com.mtli;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @Description:
 * @Author: Mt.Li
 * @Create: 2020-04-25 10:49
 */
public class Encryptor {
    @Autowired
    private ApplicationContext appCtx;

    @Autowired
    private StringEncryptor codeSheepEncryptorBean;

    @Test
    public void getPass() {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        // textEncryptor.setPassword("盐");
        String url = textEncryptor.encrypt("jdbc:路径");


//        System.out.println(url + "----------------");
//        System.out.println(name + "----------------");
//        System.out.println(password + "----------------");
//        System.out.println(password1 + "----------------");
//        System.out.println(mail + "----------------");
//        System.out.println(mailcode + "----------------");
//        System.out.println(host + "----------------");
//        Assert.assertTrue(name.length() > 0);
//        Assert.assertTrue(password.length() > 0);

    }
}
