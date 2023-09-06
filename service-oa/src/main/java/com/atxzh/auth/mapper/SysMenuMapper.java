package com.atxzh.auth.mapper;


import com.atguigu.model.system.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2023-08-24
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    //夺标联查
    List<SysMenu> findMenuListByUserId(@Param("userId") Long userId);

}
