package com.hrl.mon.organ.controller;


import com.baomidou.mybatisplus.extension.api.R;
import com.hrl.mon.organ.entity.SysOrgan;
import com.hrl.mon.organ.service.ISysOrganService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 机构管理 前端控制器
 * </p>
 *
 * @author Liang
 * @since 2021-06-11
 */
@RestController
@RequestMapping("/organ/sys-organ")
@Api(value="租户管理",tags = "租户管理")
public class SysOrganController {

    @Autowired
    private ISysOrganService sysOrganService;


    @PostMapping("/add")
    @ApiOperation(value="新增租户")
    public R<Void> add(@RequestBody SysOrgan sysOrgan){
        sysOrganService.add(sysOrgan);
        return R.ok(null);

    }

}
