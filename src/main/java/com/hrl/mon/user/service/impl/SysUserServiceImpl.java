package com.hrl.mon.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hrl.mon.auth.utils.SecurityUtils;
import com.hrl.mon.common.api.ApiAssert;
import com.hrl.mon.common.config.tenant.TenantContextHolder;
import com.hrl.mon.common.constant.CommonConstant;
import com.hrl.mon.common.constant.CommonExceptionEnum;
import com.hrl.mon.common.constant.RedisKeyPrefix;
import com.hrl.mon.sms.service.SmsService;
import com.hrl.mon.user.constant.SysUserTypeEnum;
import com.hrl.mon.user.constant.UserExceptionEnum;
import com.hrl.mon.user.entity.SysUser;
import com.hrl.mon.user.entity.SysUserRole;
import com.hrl.mon.user.mapper.SysUserMapper;
import com.hrl.mon.user.service.ISysUserRoleService;
import com.hrl.mon.user.service.ISysUserService;
import com.hrl.mon.menu.entity.SysMenu;
import com.hrl.mon.menu.service.ISysMenuService;
import com.hrl.mon.role.entity.SysRole;
import com.hrl.mon.role.entity.SysRoleMenu;
import com.hrl.mon.role.service.ISysRoleMenuService;
import com.hrl.mon.role.service.ISysRoleService;
import com.hrl.mon.user.dto.PhoneDTO;
import com.hrl.mon.user.dto.SubUserDTO;
import com.hrl.mon.user.dto.UserDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private ISysUserRoleService iSysUserRoleService;
    @Autowired
    private ISysRoleMenuService iSysRoleMenuService;
    @Autowired
    private ISysRoleService iSysRoleService;
    @Autowired
    private ISysMenuService iSysMenuService;
    @Autowired
    private ISysRoleMenuService sysRoleMenuService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SmsService smsService;

    @Override
    public SysUser getByPhone(String phone) {
        return this.getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone,phone),false);
    }

    @Override
    public List<SysMenu> getUserMenuByUserId(String userId) {
        SysUserRole sysUserRole = iSysUserRoleService.getOne(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        return iSysRoleMenuService.getByRoleId(sysUserRole.getRoleId());
    }

    @Override
    public void sendVerifyCode(String phone){
        String code = RandomUtil.randomNumbers(6);
        PhoneDTO phoneDTO =(PhoneDTO)redisTemplate.opsForValue().get(RedisKeyPrefix.SMS_CODE_PREFIX+phone);
        if(phoneDTO == null){
            phoneDTO = new PhoneDTO();
            phoneDTO.setDayNum(1);
        }else{
            Integer dayNum = phoneDTO.getDayNum();
            ApiAssert.ge(CommonConstant.SMS_CODE_DAY_MAX_TIMES,dayNum, CommonExceptionEnum.SMS_VERIFY_COUNT_MAX);
            phoneDTO.setDayNum(dayNum+1);
        }
        phoneDTO.setCode(code);
        phoneDTO.setLastSendTime(LocalDateTime.now());
        phoneDTO.setVerify(false);
        //今天剩余时间
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        long millSeconds = ChronoUnit.MILLIS.between(LocalDateTime.now(),midnight);
        redisTemplate.opsForValue().set(RedisKeyPrefix.SMS_CODE_PREFIX + phone, phoneDTO, millSeconds, TimeUnit.MILLISECONDS);
        smsService.sendVerificationCode(phone,code);
    }

    @Override
    public Boolean existPhone(String phone) {
        int count = this.count(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getPhone, phone));
        return count >= 1;
    }

    @Override
    public void saveSubUser(SubUserDTO subUser) {
        ApiAssert.isFalse(existPhone(subUser.getPhone()), UserExceptionEnum.EXIST_PHONE);
        String tenantId = TenantContextHolder.getTenantId();
        SysUser sysUser = new SysUser();
        sysUser.setUsername(subUser.getUsername());
        sysUser.setPhone(subUser.getPhone());
        sysUser.setTenantId(tenantId);
        sysUser.setOrganId(SecurityUtils.getOrganId());
        sysUser.setUserType(SysUserTypeEnum.USER_TYPE_SUB_USER.getCode());
        this.save(sysUser);

        //TODO 暂时写死权限逻辑
        SysRole sysRole = new SysRole();
        sysRole.setRoleName("子账号");
        sysRole.setRoleCode("SUB_USER");
        iSysRoleService.save(sysRole);

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setRoleId(sysRole.getId());
        sysUserRole.setUserId(sysUser.getId());
        iSysUserRoleService.save(sysUserRole);

        List<SysMenu> menuList = iSysMenuService.list();
        menuList.forEach( sysMenu -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(sysMenu.getId());
            sysRoleMenu.setRoleId(sysRole.getId());
            sysRoleMenuService.save(sysRoleMenu);
        });

    }

    @Override
    public void editSubUser(SubUserDTO subUser) {
        ApiAssert.isFalse(existPhone(subUser.getPhone()), UserExceptionEnum.EXIST_PHONE);
        SysUser sysUser = this.getById(subUser.getId());
        sysUser.setUsername(subUser.getUsername());
        sysUser.setPhone(subUser.getPhone());
        this.updateById(sysUser);
    }

    @Override
    public void removeSubUser(String userId) {
        this.removeById(userId);
        SysUserRole sysUserRole = iSysUserRoleService.getOne(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId), false);
        sysRoleMenuService.remove(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId,sysUserRole.getRoleId()));
        iSysUserRoleService.remove(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId,userId));
    }

    @Override
    public void saveUser(UserDTO userDTO) {
        ApiAssert.isFalse(existPhone(userDTO.getPhone()), UserExceptionEnum.EXIST_PHONE);
        TenantContextHolder.setTenantId(userDTO.getTenantId());
        SysUser sysUser = new SysUser();
        sysUser.setUsername(userDTO.getUsername());
        sysUser.setPhone(userDTO.getPhone());
        sysUser.setTenantId(userDTO.getTenantId());
        sysUser.setOrganId(userDTO.getOrganId());
        sysUser.setUserType(SysUserTypeEnum.USER_TYPE_ADMIN.getCode());
        this.save(sysUser);

        SysRole sysRole = new SysRole();
        sysRole.setRoleName("管理员");
        sysRole.setRoleCode("ADMIN");
        iSysRoleService.save(sysRole);

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setRoleId(sysRole.getId());
        sysUserRole.setUserId(sysUser.getId());
        iSysUserRoleService.save(sysUserRole);

        List<SysMenu> menuList = iSysMenuService.list();
        menuList.forEach( sysMenu -> {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(sysMenu.getId());
            sysRoleMenu.setRoleId(sysRole.getId());
            sysRoleMenuService.save(sysRoleMenu);
        });
    }
}
