package com.kp.mall.module.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kp.mall.module.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.kp.mall.vo.SelectVO;

import java.util.List;

/**
 * @author wuming
 * @description: 公告Mapper接口
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 分页查询
     */
    List<Notice> pageQuery(@Param("entity") Notice entity,
                             @Param("offset") Long offset,
                             @Param("limit") Long limit);

    /**
     * 查询总数
     */
    Long countQuery(@Param("entity") Notice entity);

    /**
     * 列表查询（不分页）
     */
    List<Notice> listQuery(@Param("entity") Notice entity);

    /**
     * 根据ID查询
     */
    Notice getById(@Param("id") Long id);

    /**
     * 下拉选项查询
     */
    /**
     * 下拉数据：admin
     */
    @Select("select admin_name as label, id as value from admin")
    List<SelectVO> listAdmin();

    /**
     * 用户端分页查询（按发布时间排序）
     */
    List<Notice> pageQueryForUser(@Param("entity") Notice entity,
                                   @Param("offset") Long offset,
                                   @Param("limit") Long limit);

    /**
     * 用户端查询总数
     */
    Long countQueryForUser(@Param("entity") Notice entity);

}
