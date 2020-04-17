package com.esiran.greenpay.order.entity;

import com.esiran.greenpay.common.entity.BaseMapperEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 订单详情
 * </p>
 *
 * @author Militch
 * @since 2020-04-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pay_order_detail")
public class PayOrderDetail extends BaseMapperEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付类型编码
     */
    private String payTypeCode;

    /**
     * 支付产品ID
     */
    private Integer payProductId;

    /**
     * 支付通道ID
     */
    private Integer payPassageId;

    /**
     * 支付通道子账户ID
     */
    private Integer payPassageAccId;

    /**
     * 支付接口ID
     */
    private Integer payInterfaceId;

    /**
     * 支付接口请求参数
     */
    private String payInterfaceParams;

    /**
     * 上游订单编号
     */
    private String upstreamOrderNo;

    /**
     * 上游扩展参数
     */
    private String upstreamExtra;



}
