package com.esiran.greenpay.openapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.esiran.greenpay.actuator.Plugin;
import com.esiran.greenpay.actuator.PluginLoader;
import com.esiran.greenpay.common.entity.APIException;
import com.esiran.greenpay.common.exception.ResourceNotFoundException;
import com.esiran.greenpay.common.util.EncryptUtil;
import com.esiran.greenpay.common.util.IdWorker;
import com.esiran.greenpay.merchant.entity.Merchant;
import com.esiran.greenpay.merchant.entity.MerchantProduct;
import com.esiran.greenpay.merchant.entity.MerchantProductDTO;
import com.esiran.greenpay.merchant.entity.MerchantProductPassage;
import com.esiran.greenpay.merchant.service.IMerchantProductPassageService;
import com.esiran.greenpay.merchant.service.IMerchantService;
import com.esiran.greenpay.message.delayqueue.impl.RedisDelayQueueClient;
import com.esiran.greenpay.openapi.entity.Invoice;
import com.esiran.greenpay.openapi.entity.InvoiceInputDTO;
import com.esiran.greenpay.openapi.entity.PassageAndSubAccount;
import com.esiran.greenpay.openapi.entity.PayOrder;
import com.esiran.greenpay.openapi.plugins.PayOrderFlow;
import com.esiran.greenpay.openapi.service.IInvoiceService;
import com.esiran.greenpay.pay.entity.*;
import com.esiran.greenpay.pay.service.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InvoiceService implements IInvoiceService {
    private static final ModelMapper modelMapper = new ModelMapper();
    private final IMerchantService merchantService;
    private final IProductService productService;
    private final IPassageAccountService passageAccountService;
    private final IPassageService passageService;
    private final IInterfaceService interfaceService;
    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;
    private final IdWorker idWorker;
    private final RedisDelayQueueClient redisDelayQueueClient;
    private final PluginLoader pluginLoader;
    private final IMerchantProductPassageService merchantProductPassageService;
    public InvoiceService(
            IMerchantService merchantService,
            IProductService productService,
            IPassageAccountService passageAccountService,
            IPassageService passageService,
            IInterfaceService interfaceService, IOrderService orderService,
            IOrderDetailService orderDetailService, IdWorker idWorker,
            RedisDelayQueueClient redisDelayQueueClient,
            PluginLoader pluginLoader, IMerchantProductPassageService merchantProductPassageService) {
        this.merchantService = merchantService;
        this.productService = productService;
        this.passageAccountService = passageAccountService;
        this.passageService = passageService;
        this.interfaceService = interfaceService;
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
        this.idWorker = idWorker;
        this.redisDelayQueueClient = redisDelayQueueClient;
        this.pluginLoader = pluginLoader;
        this.merchantProductPassageService = merchantProductPassageService;
    }

    private Product checkFlowByProduct(Integer mchId,Integer productId) throws APIException {
        MerchantProductDTO mp = null;
        try {
            mp = merchantService.
                    selectMchProductById(mchId,productId);
        } catch (ResourceNotFoundException e) {
            throw new APIException("支付产品ID不存在","PAY_PRODUCT_NOT_FOUND");
        }
        if (!mp.getStatus() ){
            throw new APIException("商户产品未开通","PAY_PRODUCT_NOT_SUPPORT");
        }
        Product product = productService.getById(mp.getProductId());
        if (product == null){
            throw new APIException("系统错误，获取支付产品失败","PAY_PRODUCT_NOT_FOUND");
        }
        if (!product.getStatus()){
            throw new APIException("支付产品已停用","PAY_PRODUCT_NOT_SUPPORT");
        }
        Integer mode = mp.getInterfaceMode();
        if (mode == 2){
            LambdaQueryWrapper<MerchantProductPassage> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(MerchantProductPassage::getPassageId)
//            merchantProductPassageService.get()
        }
        return product;
    }

    private PassageAndSubAccount checkFlowByPassage(Product product) throws APIException {
        Passage passage = null;
        PassageAccount passageAccount = null;
        if (product.getInterfaceMode() == 1){
            passage = passageService.getById(product.getDefaultPassageId());
            passageAccount = passageAccountService.getById(product.getDefaultPassageAccId());
        }
        if (passage == null){
            throw new APIException("系统错误，获取支付通道失败","PAY_PASSAGE_NOT_FOUND");
        }
        if (!passage.getStatus()){
            throw new APIException("支付通道已停用","PAY_PASSAGE_NOT_SUPPORT");
        }
        if (passageAccount == null){
            throw new APIException("系统错误，获取通道账户失败","PAY_PASSAGE_ACCOUNT_NOT_FOUND");
        }
        if (!passageAccount.getStatus()){
            throw new APIException("支付通道账户已停用","PAY_PASSAGE_NOT_SUPPORT");
        }
        return new PassageAndSubAccount(passage,passageAccount);
    }

    private Interface checkFlowByInterface(String interfaceCode) throws APIException {
        LambdaQueryWrapper<Interface> lambdaQueryWrapper =
                new QueryWrapper<Interface>().lambda()
                        .eq(Interface::getInterfaceCode,interfaceCode);
        Interface ints = interfaceService.getOne(lambdaQueryWrapper);
        if (ints == null){
            throw new APIException("系统错误，获取支付接口失败","PAY_INTERFACE_NOT_FOUND");
        }
        if (!ints.getStatus()){
            throw new APIException("支付接口已停用","PAY_INTERFACE_NOT_SUPPORT");
        }
        return ints;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Invoice createInvoiceByInput(InvoiceInputDTO invoiceInputDTO, Merchant merchant) throws Exception {
        Invoice invoice = modelMapper.map(invoiceInputDTO,Invoice.class);
        invoice.setOrderNo(String.valueOf(idWorker.nextId()));
        invoice.setOrderSn(EncryptUtil.baseTimelineCode());
        invoice.setCreatedAt(LocalDateTime.now());
        if (!merchant.getStatus()){
            throw new APIException("商户状态已锁定","MERCHANT_STATUS_LOCKED");
        }
        Product product = checkFlowByProduct(merchant.getId(),invoice.getChannel());
        PassageAndSubAccount pasa = checkFlowByPassage(product);
        Passage passage = pasa.getPassage();
        PassageAccount passageAccount = pasa.getPassageAccount();
        Interface ints = checkFlowByInterface(passage.getInterfaceCode());
        // 构造订单
        Order order = modelMapper.map(invoice,Order.class);
        order.setStatus(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setMchId(merchant.getId());
        LambdaQueryWrapper<Order> orderQueryWrapper = new QueryWrapper<Order>().lambda()
                .eq(Order::getMchId,order.getMchId())
                .eq(Order::getAppId,order.getAppId())
                .eq(Order::getOutOrderNo,order.getOutOrderNo());
        Order oldOrder = orderService.getOne(orderQueryWrapper);
        if (oldOrder != null){
            throw new APIException("商户订单号不能重复","ORDER_NO_DUPLICATE");
        }
        orderService.save(order);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderNo(invoice.getOrderNo());
        orderDetail.setPayProductId(product.getId());
        orderDetail.setPayPassageId(passage.getId());
        orderDetail.setPayPassageAccId(passageAccount.getId());
        orderDetail.setPayInterfaceId(ints.getId());
        orderDetail.setPayInterfaceAttr(passageAccount.getInterfaceAttr());
        orderDetail.setCreatedAt(LocalDateTime.now());
        orderDetail.setUpdatedAt(LocalDateTime.now());
        orderDetailService.save(orderDetail);
        try {
            Plugin<PayOrder> plugin = pluginLoader.loadForClassPath(ints.getInterfaceImpl());
            PayOrder payOrder = new PayOrder();
            PayOrderFlow payOrderFlow = new PayOrderFlow(payOrder);
            plugin.apply(payOrderFlow);
            payOrderFlow.execDependent("create");
            redisDelayQueueClient.sendDelayMessage("greenpay:queue:order_task",order.getOrderNo(),2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        return invoice;
    }
}
