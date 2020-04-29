package com.mtli.config;

import com.mtli.utils.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author : JCccc
 * @CreateTime : 2019/9/3
 * @Description :
 **/

@Configuration
public class DirectRabbitConfig {

    private Logger logger = LoggerUtil.loggerFactory(this.getClass());

    @Bean
    public Queue mailQueue() {
        logger.info("邮件队列初始化成功");
        return new Queue(RabbitMqConfig.MAIL_QUEUE);
    }

    @Bean
    public Queue blogQueue() {
        logger.info("博客队列初始化成功");
        return new Queue(RabbitMqConfig.BLOG_QUEUE);
    }

    @Bean
    DirectExchange exchange() {
        logger.info("交换机初始化成功");
        return new DirectExchange ("directExchange");
    }


    //将mailQueue和topicExchange绑定,而且绑定的键值为MAIL_QUEUE
    // 这样只要是消息携带的路由键是MAIL_QUEUE,才会分发到该队列
    @Bean
    Binding bindingExchangeMessage() {
        return BindingBuilder.bind(mailQueue()).to(exchange()).with(RabbitMqConfig.MAIL_QUEUE);
    }

    //将blogQueue和topicExchange绑定,而且绑定的键值为用上通配路由键规则BLOG_QUEUE
    @Bean
    Binding bindingExchangeMessage2() {
        return BindingBuilder.bind(blogQueue()).to(exchange()).with(RabbitMqConfig.BLOG_QUEUE);
    }

}