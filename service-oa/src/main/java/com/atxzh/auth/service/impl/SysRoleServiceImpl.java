package com.atxzh.auth.service.impl;

import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atxzh.auth.mapper.SysRoleMapper;
import com.atxzh.auth.service.SysRoleService;
import com.atxzh.auth.service.SysUserRoleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;
    //1.查询所有的角色 和 当前用户所属的角色
    @Override
    public Map<String, Object> findRoleByAdminId(Long userId) {
        //1.查询所有的角色，返回List集合
        List<SysRole> allRoleList = baseMapper.selectList(null);

        //2.根据userid查询角色用户关系表，查询userid所对应的角色id
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> existUserlist = sysUserRoleService.list(queryWrapper);

        //从查询出来的用户id对应角色的list集合，获取所有的id（这一步也可以用for循环来）
        List<Long> existRoleIdList = existUserlist.stream().map(c -> c.getRoleId()).collect(Collectors.toList());

        //3.根据查询出的所有的角色id，找到对应角色信息
        ArrayList<SysRole> assignRoleList = new ArrayList<>();
        for (SysRole sysRole:allRoleList){
            if (existRoleIdList.contains(sysRole.getId())){
                assignRoleList.add(sysRole);
            }
        }
        //4 把得到的数据封装到map集合，返回
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assignRoleList);
        roleMap.put("allRoleList", allRoleList);
        return roleMap;
    }
    //为用户分配角色
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //用户之前分配角色数据删除，根据userid删除
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(queryWrapper);
        //重新分配
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long roleId:roleIdList){
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRole.setRoleId(roleId);
            sysUserRoleService.save(sysUserRole);
        }
    }
}
