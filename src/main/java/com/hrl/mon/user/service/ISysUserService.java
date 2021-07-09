package com.hrl.mon.user.service;

import com.hrl.mon.user.entity.SysUser;
import com.hrl.mon.menu.entity.SysMenu;
import com.hrl.mon.user.dto.SubUserDTO;
import com.hrl.mon.user.dto.UserDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
public interface ISysUserService extends IService<SysUser> {

    SysUser getByPhone(String phone);

    List<SysMenu> getUserMenuByUserId(String userId);

    void sendVerifyCode(String phone);

    /**
     * 是否存在电话号码
     * @param phone
     * @return
     */
    Boolean existPhone(String phone);

    /**
     * 保存子用户信息
     * @param subUser
     */
    void saveSubUser(SubUserDTO subUser);

    void editSubUser(SubUserDTO subUser);

    void removeSubUser(String userId);

    void saveUser(UserDTO userDTO);


}
