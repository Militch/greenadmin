package com.esiran.greenpay.order.entity;

import com.esiran.greenpay.common.entity.BaseMapperEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 支付订单
 * </p>
 *
 * @author Militch
 * @since 2020-04-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PayOrder extends BaseMapperEntity {

    private static final long serialVersionUID = 1L;


    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 交易流水号
     */
    private String orderSn;

    /**
     * 商户ID
     */
    private Integer mchId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 商品标题
     */
    private String subject;

    /**
     * 商户订单号
     */
    private String outOrderNo;

    /**
     * 订单金额（单位：分）
     */
    private Integer amount;

    /**
     * 商品描述
     */
    private String body;

    /**
     * 客户端IP
     */
    private Integer clientIp;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 跳转地址
     */
    private String redirectUrl;

    /**
     * 订单状态（0：待付款，2：已支付，3：订单完成，-1：交易取消，-2：交易失败）
     */
    private Boolean status;

    /**
     * 支付时间
     */
    private LocalDateTime paidAt;

    /**
     * 过期时间
     */
    private LocalDateTime expiredAt;


}
