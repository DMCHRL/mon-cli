package com.hrl.mon.user.controller;


import com.hrl.mon.auth.utils.SecurityUtils;
import com.hrl.mon.common.api.R;
import com.hrl.mon.common.constant.CommonConstant;
import com.hrl.mon.common.utils.TreeUtil;
import com.hrl.mon.organ.entity.SysOrgan;
import com.hrl.mon.organ.service.ISysOrganService;
import com.hrl.mon.auth.entity.SystemUser;
import com.hrl.mon.menu.entity.SysMenu;
import com.hrl.mon.user.dto.SubUserDTO;
import com.hrl.mon.user.dto.UserDTO;
import com.hrl.mon.user.entity.SysUser;
import com.hrl.mon.user.service.ISysUserService;
import com.hrl.mon.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@RestController
@RequestMapping("/user")
@Api(tags="用户管理")
public class SysUserController {

    @Autowired
    private ISysUserService iSysUserService;


    @Autowired
    private ISysOrganService iSysOrganService;


    @GetMapping("/info")
    @ApiOperation(value="用户信息")
    @PreAuthorize("hasAuthority('sys:user:info')")
    public R<UserVO> info(){
        UserVO userVO = new UserVO();
        SystemUser user = SecurityUtils.getUser();
        List<SysMenu> sysMenus = iSysUserService.getUserMenuByUserId(user.getUserId());
        userVO.setMenuTreeList(TreeUtil.buildTree(sysMenus, CommonConstant.PARENT_ID));
        SysOrgan sysOrgan = iSysOrganService.getById(user.getOrganId());
        userVO.setUsername(user.getUsername());
        userVO.setUserType(user.getUserType());
        userVO.setCompany(sysOrgan.getName());
        userVO.setLogo(sysOrgan.getLogo());
        return R.ok(userVO);
    }

    @GetMapping("/sendVerifyCode/{phone}")
    @ApiOperation(value="发送登录验证码")
    public R<Void> sendVerifyCode(@PathVariable(value = "phone") String phone){
        iSysUserService.sendVerifyCode(phone);
        return R.ok(null);
    }

    @PostMapping("/edit")
    @ApiOperation(value="修改用户信息")
    //@PreAuthorize("hasAuthority('sys:user:edit')")
    public R<Void> edit(@RequestBody UserDTO userDTO){
        SysUser sysUser = iSysUserService.getById(SecurityUtils.getUserId());
        sysUser.setUsername(userDTO.getUsername());
        SysOrgan sysOrgan = iSysOrganService.getById(sysUser.getOrganId());
        sysOrgan.setLogo(userDTO.getLogo());
        sysOrgan.setName(userDTO.getCompany());
        iSysUserService.save(sysUser);
        iSysOrganService.save(sysOrgan);
        return R.ok(null);
    }

    @PostMapping("/saveSubUser")
    @ApiOperation(value="保存子账号信息")
    @PreAuthorize("hasAuthority('sys:user:child')")
    public R<Void> saveSubUser( @RequestBody SubUserDTO subUserDTO){
        iSysUserService.saveSubUser(subUserDTO);
        return R.ok(null);
    }

    @PostMapping("/editSubUser")
    @ApiOperation(value="编辑子账号信息")
    @PreAuthorize("hasAuthority('sys:user:child')")
    public R<Void> editSubUser( @RequestBody SubUserDTO subUserDTO){
        iSysUserService.editSubUser(subUserDTO);
        return R.ok(null);
    }

    @GetMapping("/removeSubUser/{userId}")
    @ApiOperation(value="删除子账号信息")
    @PreAuthorize("hasAuthority('sys:user:child')")
    public R<Void> removeSubUser(@PathVariable(value = "userId") String userId ){
        iSysUserService.removeSubUser(userId);
        return R.ok(null);
    }

}
