package com.kp.mall.module.integral_record.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.mapper.IntegralRecordMapper;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 积分记录Service实现类
 */
@Service
public class IntegralRecordServiceImpl extends ServiceImpl<IntegralRecordMapper, IntegralRecord> implements IntegralRecordService {

    @Autowired
    private IntegralRecordMapper integralRecordMapper;

    @Override
    public IPage<IntegralRecord> pageQuery(Integer current, Integer size, IntegralRecord entity) {
        Long offset = (long) (current - 1) * size;
        Long limit = (long) size;
        List<IntegralRecord> records = integralRecordMapper.pageQuery(entity, offset, limit);
        Long total = integralRecordMapper.countQuery(entity);

        Page<IntegralRecord> page = new Page<>(current, size);
        page.setRecords(records);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<IntegralRecord> listQuery(IntegralRecord entity) {
        return integralRecordMapper.listQuery(entity);
    }

    @Override
    public IntegralRecord getById(Long id) {
        return integralRecordMapper.getById(id);
    }

    /**
     * 下拉数据：user
     */
    @Override
    public List<SelectVO> listUser() {
        return integralRecordMapper.listUser();
    }

}
