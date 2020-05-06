package com.esiran.greenpay.admin.controller.merchant;

import com.esiran.greenpay.admin.controller.CURDBaseController;
import com.esiran.greenpay.agentpay.entity.AgentPayPassage;
import com.esiran.greenpay.agentpay.service.IAgentPayPassageService;
import com.esiran.greenpay.common.exception.ResourceNotFoundException;
import com.esiran.greenpay.framework.annotation.PageViewHandleError;
import com.esiran.greenpay.merchant.entity.*;
import com.esiran.greenpay.merchant.service.IMerchantAgentPayPassageService;
import com.esiran.greenpay.merchant.service.IMerchantProductService;
import com.esiran.greenpay.merchant.service.IMerchantService;
import com.esiran.greenpay.merchant.service.IMerchantProductPassageService;
import com.esiran.greenpay.pay.entity.Passage;
import com.esiran.greenpay.pay.entity.PassageAccount;
import com.esiran.greenpay.pay.service.IPassageAccountService;
import com.esiran.greenpay.pay.service.IPassageService;
import com.esiran.greenpay.pay.service.IProductService;
import com.esiran.greenpay.pay.service.ITypeService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/merchant")
public class AdminMerchantController extends CURDBaseController {
    private static final Gson gson = new GsonBuilder().create();
    private final IMerchantService merchantService;
    private final IProductService productService;
    private final ITypeService typeService;
    private final IPassageService passageService;
    private final IPassageAccountService passageAccountService;
    private final IMerchantProductService merchantProductService;
    private final IMerchantProductPassageService productPassageService;
    private final IMerchantAgentPayPassageService merchantAgentPayPassageService;
    private final IAgentPayPassageService agentPayPassageService;
    public AdminMerchantController(
            IMerchantService merchantService,
            IProductService productService,
            ITypeService typeService,
            IPassageService passageService,
            IPassageAccountService passageAccountService,
            IMerchantProductService merchantProductService,
            IMerchantProductPassageService productPassageService,
            IMerchantAgentPayPassageService merchantAgentPayPassageService, IAgentPayPassageService agentPayPassageService) {
        this.merchantService = merchantService;
        this.productService = productService;
        this.typeService = typeService;
        this.passageService = passageService;
        this.passageAccountService = passageAccountService;
        this.merchantProductService = merchantProductService;
        this.productPassageService = productPassageService;
        this.merchantAgentPayPassageService = merchantAgentPayPassageService;
        this.agentPayPassageService = agentPayPassageService;
    }

    @GetMapping("/list")
    public String list(){
        return "admin/merchant/list";
    }
    @GetMapping("/list/{mchId}/edit")
    public String edit(@PathVariable Integer mchId, ModelMap modelMap){
        MerchantDetailDTO merchantDTO = merchantService.findMerchantById(mchId);
        modelMap.addAttribute("merchant",merchantDTO);
        return "admin/merchant/edit";
    }

    @GetMapping("/list/{mchId}/product/list")
    public String password(@PathVariable String mchId){
        return "admin/merchant/product/list";
    }

    @GetMapping("/list/{mchId}/agentpay/list")
    public String agentpay(@PathVariable String mchId,ModelMap modelMap){
        modelMap.addAttribute("mchId",mchId);
        return "admin/merchant/agentpay/list";
    }

    @GetMapping("/list/{mchId}/product/list/{productId}/edit")
    @PageViewHandleError
    public String product(
            @PathVariable Integer mchId,
            @PathVariable Integer productId,
            ModelMap modelMap) throws Exception {
        MerchantProductDTO merchantProduct = merchantService.selectMchProductById(mchId,productId);
        List<Passage> availPassages = passageService.listByPayTypeCode(merchantProduct.getPayTypeCode());
        List<PassageAccount> availPassagesAcc = passageAccountService.listByPayTypeCode(merchantProduct.getPayTypeCode());
        List<MerchantProductPassage> usagePassages = productPassageService.listByProductId(mchId, productId);
        String usagePassagesJson = gson.toJson(usagePassages);
        String availPassagesJson = gson.toJson(availPassages);
        modelMap.addAttribute("merchantProduct", merchantProduct);
        modelMap.addAttribute("availPassages", availPassages);
        modelMap.addAttribute("availPassagesAcc", availPassagesAcc);
        modelMap.addAttribute("availPassagesJson", availPassagesJson);
        modelMap.addAttribute("usagePassagesJson", usagePassagesJson);
        modelMap.addAttribute("mchId", mchId);
        return "admin/merchant/product/edit";
    }
    @PostMapping("/list/{mchId}/product/list/{productId}/edit")
    public String productPost(
            @PathVariable Integer mchId,
            @PathVariable Integer productId,
            @Valid MerchantProductInputDTO inputDTO) throws Exception {
        merchantProductService.updateById(inputDTO);
        return redirect("/admin/merchant/list/%s/product/list/%s/edit",mchId,productId);
    }

    @GetMapping("/add")
    @PageViewHandleError
    public String add(){
        return "admin/merchant/add";
    }

    @PostMapping("/add")
    public String add(@Valid MerchantInputDTO merchant) throws Exception {
        merchantService.addMerchant(merchant);
        return "redirect:/admin/merchant/list";
    }



    @GetMapping("/list/{mchId}/agentpay/list/{passageId}/edit")
    @PageViewHandleError
    public String agentPayPassage(
            @PathVariable Integer mchId,
            @PathVariable Integer passageId,
            ModelMap modelMap) throws Exception {
        Merchant merchant = merchantService.getById(mchId);
        if (merchant == null) throw new ResourceNotFoundException("商户不存在");
        MerchantAgentPayPassageDTO data = merchantService.selectMchAgentPayPassageByMchId(mchId,passageId);
        if (data == null) throw new ResourceNotFoundException("代付通道不存在");
//        MerchantProductDTO merchantProduct = merchantService.selectMchProductById(mchId,productId);
//        List<Passage> availPassages = passageService.listByPayTypeCode(merchantProduct.getPayTypeCode());
//        List<PassageAccount> availPassagesAcc = passageAccountService.listByPayTypeCode(merchantProduct.getPayTypeCode());
//        List<MerchantProductPassage> usagePassages = productPassageService.listByProductId(mchId, productId);
//        String usagePassagesJson = gson.toJson(usagePassages);
//        String availPassagesJson = gson.toJson(availPassages);
//        modelMap.addAttribute("merchantProduct", merchantProduct);
//        modelMap.addAttribute("availPassages", availPassages);
//        modelMap.addAttribute("availPassagesAcc", availPassagesAcc);
//        modelMap.addAttribute("availPassagesJson", availPassagesJson);
//        modelMap.addAttribute("usagePassagesJson", usagePassagesJson);
        modelMap.addAttribute("mchId", mchId);
        modelMap.addAttribute("data", data);
        return "admin/merchant/agentpay/edit";
    }
}
