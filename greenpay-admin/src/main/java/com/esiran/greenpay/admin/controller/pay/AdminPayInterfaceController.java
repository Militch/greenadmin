package com.esiran.greenpay.admin.controller.pay;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/pay/interface")
public class AdminPayInterfaceController {
    @GetMapping("/list")
    public String list(){
        return "admin/pay/interface/list";
    }
    @GetMapping("/list/add")
    public String add(){
        return "admin/pay/interface/add";
    }
    @PostMapping("/list/add")
    public String addFrom(){
        return "admin/pay/interface/add";
    }
}
