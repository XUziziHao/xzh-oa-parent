package com.atxzh.auth.controller;

import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.atxzh.auth.service.SysMenuService;
import com.atxzh.auth.service.SysUserService;
import com.atxzh.common.config.excption.GuiguException;
import com.atxzh.common.jwt.JwtHelper;
import com.atxzh.common.result.Result;
import com.atxzh.common.utils.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台登录管理")
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 登入
     * @return
     */
    //login
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
//        HashMap<String, Object> map = new HashMap<>();
//        map.put("token","admin-token");
//        return Result.ok(map);

        //1 获取输入得用户名和密码
        //2 根据用户名查询数据库
        String username = loginVo.getUsername();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername,username);
        SysUser sysUser = sysUserService.getOne(wrapper);
        //3 用户信息是否存在
        if (sysUser == null){
            throw new GuiguException(201,"用户不存在");
        }
        //4判断密码
        //数据库中得密码
        String password = sysUser.getPassword();
        //获取输入得密码
        String password1 = loginVo.getPassword();
        String password_input = MD5.encrypt(password1);
        if (!password.equals(password_input)){
            throw new GuiguException(201,"密码错误");
        }
        //判断用户是否被禁用
        if (sysUser.getStatus().intValue() == 0){
            throw new GuiguException(201,"用户被禁用");
        }
        //使用jwt根据用户id和用户名称生成token字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getUsername());
        //返回
        Map<String,Object> map = new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }

    /**
     xin取用户信息
     * @return
     */
    @GetMapping("info")
    public Result info(HttpServletRequest request) {
        //1 从请求头里面获取用户信息（请求头token 字符串）
        String token = request.getHeader("token");
        //2 从token字符串中获取id 或者 用户名称
        Long userId = JwtHelper.getUserId(token);
        //3 根据用户id 查询数据库，把用户信息获取出来
        SysUser sysUser = sysUserService.getById(userId);
        //4 根据用户id 获取用户可以操作得菜单列表
        //查询数据库 动态构建路由结构，进行显示
        List<RouterVo> routerList =  sysMenuService.findUserMenuListByIdList(userId);
        //5 根据在用户id 获取用户可以操作菜单列表
        List<String> permsList = sysMenuService.findUserPermsById(userId);
        //6 返回相应数据


        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        //返回用户可以操作得菜单
        map.put("routers",routerList);
        //返回用户可以操作按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }

    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }
}
