package com.kp.mall.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.kp.mall.module.admin.entity.Admin;
import com.kp.mall.module.admin.service.AdminService;
import com.kp.mall.common.Result;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuming
 * @description: 系统用户登录接口
 */
@RestController
@RequestMapping("/admin/login")
public class AdminLoginController {

    static final Map<String, Long> TOKEN_CACHE_MAP = new HashMap<>();

    @Value("${check-captcha}")
    private boolean checkCaptcha;

    @Value("${spring.application.name}")
    private String secret;

    @Autowired
    private JwtUtil<LoginUser> jwtUtil;

    @Autowired
    private AdminService adminService;

    /**
     * 登录接口
     *
     * @param loginVO 登录信息
     */
    @PostMapping
    public Result login(@RequestBody LoginVO loginVO) {
        if (StrUtil.isEmpty(loginVO.getUsername())) {
            return Result.error("登录用户名不能为空");
        }
        if (StrUtil.isEmpty(loginVO.getPassword())) {
            return Result.error("登录密码不能为空");
        }
        // 是否开启验证码校验
        if (checkCaptcha) {
            // 获取验证码
            String captcha = loginVO.getCaptcha();
            if (!StringUtils.hasLength(captcha)) {
                return Result.error("验证码不能为空");
            }
            // 拿到服务器中的验证码信息
            Map<String, Object> captchaInfo = CaptchaController.CAPTCHA_CACHE_MAP.get(loginVO.getCaptchaKey());
            if (captchaInfo == null) {
                return Result.error("验证码不存在");
            }
            // 获取验证码和时间戳
            String code = (String) captchaInfo.get("code");
            long timestamp = (long) captchaInfo.get("timestamp");
            // 校验验证码是否过期
            if (System.currentTimeMillis() - timestamp > CaptchaController.EXPIRATION_TIME) {
                CaptchaController.CAPTCHA_CACHE_MAP.remove(loginVO.getCaptchaKey());
                return Result.error("验证码已过期");
            }
            // 校验验证码是否正确
            if (!captcha.equalsIgnoreCase(code)) {
                return Result.error("验证码错误");
            }
            // 校验通过删除验证码缓存
            CaptchaController.CAPTCHA_CACHE_MAP.remove(loginVO.getCaptchaKey());
        }
        // 查询用户信息
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getAdminAccount, loginVO.getUsername());
        Admin admin = adminService.getOne(queryWrapper);
        // 校验用户信息
        if (admin == null) {
            return Result.error("用户不存在");
        }
        // 校验密码
        if (!loginVO.getPassword().equals(sha256(admin.getAdminPassword()))) {
            return Result.error("密码错误");
        }
        // 组装登录用户信息
        LoginUser loginUser = new LoginUser();
        loginUser.setId(admin.getId());
        loginUser.setName(admin.getAdminName());
        loginUser.setAvatar(StrUtil.isNotEmpty(admin.getAvatar()) ? admin.getAvatar() : "https://code-graden-server-dev.oss-cn-beijing.aliyuncs.com/2022-05-20/1ef4592a-ff86-424a-845a-260a58a550a4_logo.jpg");
        loginUser.setIntroduction("I am a super administrator");
        loginUser.setRole(String.valueOf(admin.getRoleType()));
        String token = jwtUtil.createToken(loginUser);
        // 设置 token 到缓存中
        TOKEN_CACHE_MAP.put(token, System.currentTimeMillis());
        return Result.success(token);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "尚未登录~");
        }
        Long startTime = TOKEN_CACHE_MAP.get(authorization);
        if (startTime == null) {
            return Result.error(401, "登录状态已过期~");
        }
        // 过期时间为 24 小时
        if (System.currentTimeMillis() - startTime > (1000 * 60 * 60 * 24)) {
            return Result.error(401, "登录状态已过期~");
        }
        boolean verifiedToken = jwtUtil.verifyToken(authorization);
        if (!verifiedToken) {
            return Result.error(401, "登录状态已过期~");
        }
        LoginUser loginUser = jwtUtil.parseToken(authorization, LoginUser.class);
        Admin admin = adminService.getById(loginUser.getId());
        LoginUser loginUserNew = new LoginUser();
        loginUserNew.setId(admin.getId());
        loginUserNew.setName(admin.getAdminName());
        loginUserNew.setAvatar(StrUtil.isNotEmpty(admin.getAvatar()) ? admin.getAvatar() : "https://code-graden-server-dev.oss-cn-beijing.aliyuncs.com/2022-05-20/1ef4592a-ff86-424a-845a-260a58a550a4_logo.jpg");
        loginUserNew.setIntroduction("I am a super administrator");
        loginUserNew.setRole(String.valueOf(admin.getRoleType()));
        return Result.success(loginUserNew);
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotEmpty(token)) {
            TOKEN_CACHE_MAP.remove(token);
            jwtUtil.expireTokenImmediately(token);
        }
        return Result.success("退出登录成功");
    }

    /**
     * 根据用户编号获取用户信息
     *
     * @param id 用户编号
     * @return 用户信息
     */
    @GetMapping("/getUserInfoById")
    public Result getUserInfoById(@RequestParam("id") Integer id) {
        return Result.success(adminService.getById(id));
    }

    /**
     * 修改个人信息
     *
     * @param user 用户实体
     * @return 结果
     */
    @PostMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody Admin user) {
        adminService.updateById(user);
        return Result.success("修改成功");
    }

    /**
     * 修改密码
     *
     * @param updatePasswordDTO 修改密码参数
     * @return 结果
     */
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        Admin user = adminService.getById(updatePasswordDTO.getId());
        if (user == null) {
            return Result.error("修改失败，用户信息不存在~");
        }
        if (!sha256(user.getAdminPassword()).equals(updatePasswordDTO.getOldPassword())) {
            return Result.error("修改失败，旧密码不一致~");
        }
        if (user.getAdminPassword().equals(updatePasswordDTO.getNewPassword())) {
            return Result.error("修改失败，新密码与旧密码一致~");
        }
        Admin updateUser = new Admin();
        updateUser.setId(updatePasswordDTO.getId());
        updateUser.setAdminPassword(updatePasswordDTO.getNewPassword());
        adminService.updateById(updateUser);
        return Result.success("修改成功~");
    }

    /**
     * SHA-256 加密方法
     *
     * @param input 原始字符串
     * @return 加密后的字符串
     */
    private String sha256(String input) {
        try {
            String newInput = secret + input;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(newInput.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    static class UpdatePasswordDTO {

        private String oldPassword;

        private String newPassword;

        private Long id;
    }

    /**
     * 登录用户信息
     */
    @Data
    static class LoginUser {

        private Long id;

        private String name;

        private String avatar;

        private String introduction;

        private String role;
    }

}
