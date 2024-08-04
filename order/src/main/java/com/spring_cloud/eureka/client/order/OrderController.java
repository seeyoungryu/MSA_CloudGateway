package com.spring_cloud.eureka.client.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/order")
public class OrderController {

    @GetMapping
    public String getOrder() {
        return "Order details";
    }
}


