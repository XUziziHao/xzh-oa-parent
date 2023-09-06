package com.atxzh.auth.service;

import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface SysRoleService extends IService<SysRole> {

    //1.查询所有的角色 和 当前用户所属的角色
    Map<String, Object> findRoleByAdminId(Long userId);

    //为用户分配角色
    void doAssign(AssginRoleVo assginRoleVo);
}
