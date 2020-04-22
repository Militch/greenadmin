package com.esiran.greenpay.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.esiran.greenpay.pay.entity.Type;
import com.esiran.greenpay.pay.mapper.TypeMapper;
import com.esiran.greenpay.pay.service.ITypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付类型 服务实现类
 * </p>
 *
 * @author Militch
 * @since 2020-04-13
 */
@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements ITypeService {

    @Override
    public Type findTypeByCode(String code) {
        LambdaQueryWrapper<Type> typeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        typeLambdaQueryWrapper.eq(Type::getTypeCode,code);
        return getOne(typeLambdaQueryWrapper);
    }
}
