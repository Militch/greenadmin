package com.esiran.greenpay.admin.controller.pay;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/pay/passage")
public class AdminPayPassageController {
    @GetMapping("/list")
    public String list(){
        return "admin/pay/passage/list";
    }
//    @GetMapping("/add")
//    public String add(){
//        return "admin/merchant/add";
//    }
}
