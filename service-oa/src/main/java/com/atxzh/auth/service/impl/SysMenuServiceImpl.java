package com.atxzh.auth.service.impl;


import com.atguigu.model.system.SysMenu;
import com.atguigu.model.system.SysRoleMenu;
import com.atguigu.vo.system.AssginMenuVo;
import com.atguigu.vo.system.MetaVo;
import com.atguigu.vo.system.RouterVo;
import com.atxzh.auth.mapper.SysMenuMapper;
import com.atxzh.auth.service.SysMenuService;
import com.atxzh.auth.service.SysRoleMenuService;
import com.atxzh.auth.utils.MenuHelper;
import com.atxzh.common.config.excption.GuiguException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2023-08-24
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    //菜单接口列表
    @Override
    public List<SysMenu> findNodes() {
        //1 查询所有菜单数据
        List<SysMenu> sysMenuList = baseMapper.selectList(null);

        //2 构建树形的结构
        List<SysMenu> resultList =  MenuHelper.buildTree(sysMenuList);
        return resultList;
    }

    //删除菜单
    @Override
    public void removeMenuById(Long id) {
        //判断当前的菜单是否有下一层菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getParentId,id);
        Integer count = baseMapper.selectCount(queryWrapper);
        if (count > 0){
            throw new GuiguException(201,"菜单不能删除");
        }
        baseMapper.deleteById(id);
    }

    /**
     * 查询所有菜单和角色分配菜单
     * @param roleId
     * @return
     */
    @Override
    public List<SysMenu> findMenuByRoleId(Long roleId) {
        //1查询所有菜单 - 添加条件 status = 1
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getStatus,1);
        List<SysMenu> allSysMenuList = baseMapper.selectList(queryWrapper);
        //2 根据角色id roleId查询 角色菜单关系表里面 角色id对应所有菜单id
        LambdaQueryWrapper<SysRoleMenu> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(SysRoleMenu::getRoleId,roleId);
        List<SysRoleMenu> sysRoleMenuList = sysRoleMenuService.list(queryWrapper1);
        //3 根据菜单获取id，获取对应菜单对象
        List<Long> menuIdList = sysRoleMenuList.stream().map(c -> c.getMenuId()).collect(Collectors.toList());
        //3.1 拿着菜单id 和所有菜单集合里面id进行比较，如果相同封装
        allSysMenuList.stream().forEach(item -> {
            if (menuIdList.contains(item.getId())){
                item.setSelect(true);
            }else {
                item.setSelect(false);
            }
        });
        //4 返回规定树形显示格式菜单
        List<SysMenu> sysMenuList = MenuHelper.buildTree(allSysMenuList);

        return sysMenuList;
    }

    /**
     * 为角色分配菜单
     * @param assginMenuVo
     */
    @Override
    public void doAssign(AssginMenuVo assginMenuVo) {
        //1 根据角色id 删除菜单角色表 分配数据
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId,assginMenuVo.getRoleId());
        sysRoleMenuService.remove(wrapper);

        //2 从参数里面获取角色新分配菜单id
        //进行遍历，把每个id数据添加菜单角色表
        List<Long> menuIdList = assginMenuVo.getMenuIdList();
        for (Long menuId:menuIdList) {
            if (StringUtils.isEmpty(menuId)){
                continue;
            }
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(menuId);
            sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
            sysRoleMenuService.save(sysRoleMenu);
        }
    }

    //根据用户id获取用户可以操作菜单列表
    @Override
    public List<RouterVo> findUserMenuListByIdList(Long userId) {

        List<SysMenu> sysMenuList = null;
        //1 判断当前用户是否是管理员
        //1.1 如果是管理员，查询所有菜单列表
        if (userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus,1);
            queryWrapper.orderByAsc(SysMenu::getSortValue);
            sysMenuList = baseMapper.selectList(queryWrapper);
        }else {
            //1.2 如果不是管理员， 根据userid查询可以操作得菜单列表
            sysMenuList = baseMapper.findMenuListByUserId(userId);
        }

        //多表关联查询

        //2 把查询出来得数据列表变成前端路由得结构形式
        List<SysMenu> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        List<RouterVo> routerList = this.buildRouter(sysMenuTreeList);
        return routerList;
    }

    //构建要求得路由结构
    private List<RouterVo> buildRouter(List<SysMenu> menus) {
        // 创建 list 集合，存值最终数据
        List<RouterVo> routers = new ArrayList<>();
        // menus 遍历
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden(false);
            router.setAlwaysShow(false);
            router.setPath(getRouterPath(menu));
            router.setComponent(menu.getComponent());
            router.setMeta(new MetaVo(menu.getName(), menu.getIcon()));
            // 下一层数据
            List<SysMenu> children = menu.getChildren();
            if (menu.getType().intValue() == 1) {
                // 加载隐藏路由
                List<SysMenu> hiddenMenuList = children.stream().filter(item -> !StringUtils.isEmpty(item.getComponent())).collect(Collectors.toList());
                for (SysMenu hiddenMenu : hiddenMenuList) {
                    RouterVo hiddenRouter = new RouterVo();
                    hiddenRouter.setHidden(true);
                    hiddenRouter.setAlwaysShow(false);
                    hiddenRouter.setPath(getRouterPath(hiddenMenu));
                    hiddenRouter.setComponent(hiddenMenu.getComponent());
                    hiddenRouter.setMeta(new MetaVo(hiddenMenu.getName(), hiddenMenu.getIcon()));
                    routers.add(hiddenRouter);
                }
            }else {
                if (!CollectionUtils.isEmpty(children)) {
                    if(children.size() > 0) {
                        router.setAlwaysShow(true);
                    }
                    // 递归
                    router.setChildren(buildRouter(children));
                }
            }
            routers.add(router);
        }
        return routers;
    }

    //获取路由地址
    public String getRouterPath(SysMenu menu) {
        String routerPath = "/" + menu.getPath();
        if (menu.getParentId().intValue() != 0) {
            routerPath = menu.getPath();
        }
        return routerPath;
    }

    //根据用户id获取用户可以操作按钮列表
    @Override
    public List<String> findUserPermsById(Long userId) {
        //1 判断是否是管理员，如果是管理员，查询所有按钮列表
        List<SysMenu> sysMenuList = null;

        if (userId.longValue() == 1){
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus,1);
            sysMenuList = baseMapper.selectList(queryWrapper);

        }else {
            //1.2 如果不是管理员， 根据userid查询可以操作得按钮列表
            //多表关联查询
            sysMenuList = baseMapper.findMenuListByUserId(userId);

        }
        // 从查询出来得数据里面，获取可以操作按钮值得list集合
        List<String> permsList = sysMenuList.stream()
                //filter 判断 值等于2
                .filter(item -> item.getType() == 2)
                .map(item -> item.getPerms())
                .collect(Collectors.toList());
        return permsList;


    }
}
