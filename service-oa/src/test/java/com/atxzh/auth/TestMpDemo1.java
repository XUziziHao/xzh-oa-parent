package com.atxzh.auth;


import com.atguigu.model.system.SysRole;
import com.atxzh.auth.mapper.SysRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestMpDemo1 {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    //查询所有记录
    @Test
    public void getAll(){
        List<SysRole> list = sysRoleMapper.selectList(null);
        System.out.println(list);
    }

    //添加操作
    @Test
    public void add(){
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("角色管理员");
        sysRole.setRoleCode("role");
        sysRole.setDescription("角色管理员");

        int res = sysRoleMapper.insert(sysRole);
        System.out.println(res);
    }

    //修改
    @Test
    public void update(){
        SysRole sysRole = sysRoleMapper.selectById(9);
        sysRole.setRoleName("xzh");
        int i = sysRoleMapper.updateById(sysRole);
        System.out.println(i);
    }

    //逻辑删除
    @Test
    public void de(){
        int i = sysRoleMapper.deleteById(9);
    }
    //条件查询;
    @Test
    public void testQuery(){
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getRoleName, "普通管理员");
        List<SysRole> list = sysRoleMapper.selectList(queryWrapper);
        System.out.println(list);
    }
}

