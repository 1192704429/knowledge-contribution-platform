package com.kp.mall.basic.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kp.mall.common.Result;
import com.kp.mall.module.integral_record.entity.IntegralRecord;
import com.kp.mall.module.integral_record.service.IntegralRecordService;
import com.kp.mall.module.user.entity.User;
import com.kp.mall.module.user.service.UserService;
import com.kp.mall.util.JwtUtil;
import com.kp.mall.util.MailUtil;
import com.kp.mall.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author wuming
 * @description: 用户登录接口
 */
@RestController
@RequestMapping("/user")
public class UserLoginController {

    /** Redis 用户 token 前缀，完整 key：user:token:{token}，value：登录时间戳，TTL 7 天，重启后仍有效 */
    private static final String REDIS_USER_TOKEN_PREFIX = "user:token:";
    /** 用户 token 在 Redis 中的过期时间（7 天），重启服务后无需重新登录 */
    private static final Duration USER_TOKEN_TTL = Duration.ofDays(7);

    /** Redis 登录锁 key 前缀，完整 key：user:login_lock:{登录账号}，value：failCount:lockUntilMs */
    private static final String REDIS_LOGIN_LOCK_PREFIX = "user:login_lock:";
    /** 最大错误次数后锁定 */
    private static final int LOGIN_MAX_FAIL_COUNT = 5;
    /** 锁定时长（毫秒），10分钟 */
    private static final long LOGIN_LOCK_DURATION_MS = 10 * 60 * 1000;
    /** 仅记录失败次数时的 key 过期时间（1 小时） */
    private static final Duration LOGIN_LOCK_FAIL_TTL = Duration.ofHours(1);

    /** Redis 签到 key 前缀，完整 key：user:signin:{userId}:{yyyyMMdd}，value：1，TTL 至次日 0 点 */
    private static final String REDIS_SIGNIN_PREFIX = "user:signin:";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    /** 签到奖励积分 */
    private static final int SIGNIN_INTEGRAL = 5;
    /** 积分类型：1-获取 2-消耗 */
    private static final int INTEGRAL_TYPE_GET = 1;
    /** 积分来源：1-每日签到（与业务约定一致即可） */
    private static final int INTEGRAL_SOURCE_SIGNIN = 1;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IntegralRecordService integralRecordService;

    // 邮箱验证码缓存：key为邮箱，value包含验证码和时间戳
    static final Map<String, Map<String, Object>> EMAIL_CODE_CACHE_MAP = new HashMap<>();

    // 验证码有效时间（毫秒），5分钟
    public static final long EMAIL_CODE_EXPIRATION_TIME = 5 * 60 * 1000;

    // 验证码发送间隔（毫秒），60秒
    public static final long EMAIL_CODE_SEND_INTERVAL = 60 * 1000;

    @Value("${spring.application.name}")
    private String secret;

    @Autowired
    private JwtUtil<UserLoginUser> jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private MailUtil mailUtil;

    /**
     * 用户登录接口
     *
     * @param loginVO 登录信息
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginVO loginVO) {
        if (StrUtil.isEmpty(loginVO.getUsername())) {
            return Result.error("登录账号不能为空");
        }
        if (StrUtil.isEmpty(loginVO.getPassword())) {
            return Result.error("登录密码不能为空");
        }

        String loginKey = loginVO.getUsername().trim().toLowerCase();
        // 检查是否处于锁定状态（Redis）
        LoginLockInfo lockInfo = getLoginLockInfo(loginKey);
        if (lockInfo != null && lockInfo.getLockUntilTime() > System.currentTimeMillis()) {
            long remainMinutes = (lockInfo.getLockUntilTime() - System.currentTimeMillis()) / (60 * 1000);
            return Result.error("账号已锁定，请" + (remainMinutes > 0 ? remainMinutes + "分钟" : "稍候") + "后再试");
        }
        if (lockInfo != null && lockInfo.getLockUntilTime() <= System.currentTimeMillis()) {
            clearLoginLock(loginKey);
            lockInfo = null;
        }

        // 查询用户信息（支持账号、手机号、邮箱登录）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wrapper ->
            wrapper.eq(User::getUserAccount, loginVO.getUsername())
                   .or()
                   .eq(User::getUserPhone, loginVO.getUsername())
                   .or()
                   .eq(User::getUserEmail, loginVO.getUsername())
        );
        User user = userService.getOne(queryWrapper);

        // 校验用户信息
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 校验用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("用户已被禁用");
        }

        // 校验密码（前端传过来的密码已经是SHA-256加密后的）
        String encryptedPassword = sha256(user.getUserPassword());
        if (!loginVO.getPassword().equals(encryptedPassword)) {
            int failCount = 1;
            if (lockInfo != null) {
                failCount = lockInfo.getFailCount() + 1;
            }
            if (failCount >= LOGIN_MAX_FAIL_COUNT) {
                long lockUntil = System.currentTimeMillis() + LOGIN_LOCK_DURATION_MS;
                setLoginLock(loginKey, failCount, lockUntil, Duration.ofMinutes(10));
                return Result.error("密码错误次数过多，账号已锁定10分钟");
            }
            setLoginLock(loginKey, failCount, 0L, LOGIN_LOCK_FAIL_TTL);
            return Result.error("密码错误，还可尝试" + (LOGIN_MAX_FAIL_COUNT - failCount) + "次");
        }
        // 登录成功，清除该账号的失败记录（Redis）
        clearLoginLock(loginKey);

        // 更新最后登录时间
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        userService.updateById(updateUser);

        // 组装登录用户信息
        UserLoginUser loginUser = new UserLoginUser();
        loginUser.setId(user.getId());
        loginUser.setName(user.getUserName());
        loginUser.setAvatar(StrUtil.isNotEmpty(user.getAvatar()) ? user.getAvatar() : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        loginUser.setEmail(user.getUserEmail());
        loginUser.setPhone(user.getUserPhone());
        loginUser.setTotalIntegral(user.getTotalIntegral() != null ? user.getTotalIntegral() : 0);

        String token = jwtUtil.createToken(loginUser);
        // 登录信息存入 Redis，重启后仍有效
        String redisKey = REDIS_USER_TOKEN_PREFIX + token;
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(System.currentTimeMillis()), USER_TOKEN_TTL);

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
        String redisKey = REDIS_USER_TOKEN_PREFIX + authorization;
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            return Result.error(401, "登录状态已过期~");
        }
        boolean verifiedToken = jwtUtil.verifyToken(authorization);
        if (!verifiedToken) {
            return Result.error(401, "登录状态已过期~");
        }
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        User user = userService.getById(loginUser.getId());
        if (user == null) {
            return Result.error(401, "用户不存在~");
        }

        // 返回用户信息
        UserLoginUser userInfo = new UserLoginUser();
        userInfo.setId(user.getId());
        userInfo.setName(user.getUserName());
        userInfo.setAvatar(StrUtil.isNotEmpty(user.getAvatar()) ? user.getAvatar() : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        userInfo.setEmail(user.getUserEmail());
        userInfo.setPhone(user.getUserPhone());
        userInfo.setTotalIntegral(user.getTotalIntegral() != null ? user.getTotalIntegral() : 0);

        // 今日是否已签到（用于前端展示）
        String signinKey = REDIS_SIGNIN_PREFIX + user.getId() + ":" + LocalDate.now(ZONE_ID).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        userInfo.setSignedToday(Boolean.TRUE.equals(stringRedisTemplate.hasKey(signinKey)));

        return Result.success(userInfo);
    }

    /**
     * 更新用户基本信息（需登录）：姓名、头像、邮箱、手机号
     */
    @PutMapping("/info")
    public Result updateInfo(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (body.get("name") != null) {
            user.setUserName(body.get("name").toString().trim());
        }
        if (body.get("avatar") != null) {
            user.setAvatar(body.get("avatar").toString().trim());
        }
        if (body.get("email") != null) {
            user.setUserEmail(body.get("email").toString().trim());
        }
        if (body.get("phone") != null) {
            user.setUserPhone(body.get("phone").toString().trim());
        }
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("修改成功", null);
        }
        return Result.error("修改失败");
    }

    /**
     * 修改密码（需登录）：请求体 oldPassword、newPassword，与登录同规则加密
     */
    @PutMapping("/password")
    public Result updatePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String oldPwd = body != null ? body.get("oldPassword") : null;
        String newPwd = body != null ? body.get("newPassword") : null;
        if (StrUtil.isBlank(oldPwd) || StrUtil.isBlank(newPwd)) {
            return Result.error("原密码和新密码不能为空");
        }
        if (newPwd.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!oldPwd.equals(user.getUserPassword())) {
            return Result.error("原密码错误");
        }
        user.setUserPassword(newPwd);
        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("密码修改成功", null);
        }
        return Result.error("密码修改失败");
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) return null;
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(REDIS_USER_TOKEN_PREFIX + authorization))) return null;
        if (!jwtUtil.verifyToken(authorization)) return null;
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        return loginUser != null ? loginUser.getId() : null;
    }

    /**
     * 每日签到（需登录）
     */
    @PostMapping("/sign-in")
    public Result signIn(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isEmpty(authorization)) {
            return Result.error(401, "请先登录");
        }
        String tokenRedisKey = REDIS_USER_TOKEN_PREFIX + authorization;
        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenRedisKey))) {
            return Result.error(401, "登录状态已过期~");
        }
        if (!jwtUtil.verifyToken(authorization)) {
            return Result.error(401, "登录状态已过期~");
        }
        UserLoginUser loginUser = jwtUtil.parseToken(authorization, UserLoginUser.class);
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "用户信息获取失败~");
        }
        Long userId = loginUser.getId();
        String dateStr = LocalDate.now(ZONE_ID).format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String signinKey = REDIS_SIGNIN_PREFIX + userId + ":" + dateStr;
        Boolean absent = stringRedisTemplate.opsForValue().setIfAbsent(signinKey, "1");
        if (Boolean.FALSE.equals(absent)) {
            return Result.error("今日已签到，请明天再来");
        }
        // TTL 到次日 0 点（上海时区）
        long secondsUntilMidnight = LocalDate.now(ZONE_ID).plusDays(1).atStartOfDay(ZONE_ID).toEpochSecond()
                - java.time.Instant.now().atZone(ZONE_ID).toEpochSecond();
        if (secondsUntilMidnight > 0) {
            stringRedisTemplate.expire(signinKey, Duration.ofSeconds(secondsUntilMidnight));
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        int currentIntegral = user.getTotalIntegral() != null ? user.getTotalIntegral() : 0;
        int newBalance = currentIntegral + SIGNIN_INTEGRAL;
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setTotalIntegral(newBalance);
        userService.updateById(updateUser);
        IntegralRecord record = new IntegralRecord();
        record.setUserId(userId);
        record.setIntegralType(INTEGRAL_TYPE_GET);
        record.setIntegralSource(INTEGRAL_SOURCE_SIGNIN);
        record.setIntegralNum(SIGNIN_INTEGRAL);
        String signDate = LocalDate.now(ZONE_ID).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
        record.setRelatedDesc("每日签到-获得" + SIGNIN_INTEGRAL + "积分-签到日期:" + signDate);
        record.setBalance(newBalance);
        record.setCreateTime(LocalDateTime.now());
        integralRecordService.save(record);
        return Result.success("签到成功，获得 " + SIGNIN_INTEGRAL + " 积分");
    }

    /**
     * 发送邮箱验证码接口
     *
     * @param request 请求体，包含email字段
     */
    @PostMapping("/send-email-code")
    public Result sendEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (StrUtil.isEmpty(email)) {
            return Result.error("邮箱不能为空");
        }

        // 校验邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }

        // 检查发送间隔（防止频繁发送）
        Map<String, Object> existingCodeInfo = EMAIL_CODE_CACHE_MAP.get(email);
        if (existingCodeInfo != null) {
            long lastSendTime = (Long) existingCodeInfo.get("sendTime");
            if (System.currentTimeMillis() - lastSendTime < EMAIL_CODE_SEND_INTERVAL) {
                long remainingSeconds = (EMAIL_CODE_SEND_INTERVAL - (System.currentTimeMillis() - lastSendTime)) / 1000;
                return Result.error("验证码发送过于频繁，请" + remainingSeconds + "秒后再试");
            }
        }

        // 生成6位数字验证码
        String code = generateEmailCode();

        // 发送邮件
        try {
            String subject = "【知识贡献平台】邮箱验证码";
            String text = "您的验证码是：" + code + "，有效期为5分钟，请勿泄露给他人。";
            mailUtil.sendSimpleMail(subject, text, email);

            // 缓存验证码信息
            Map<String, Object> codeInfo = new HashMap<>();
            codeInfo.put("code", code);
            codeInfo.put("timestamp", System.currentTimeMillis());
            codeInfo.put("sendTime", System.currentTimeMillis());
            EMAIL_CODE_CACHE_MAP.put(email, codeInfo);

            return Result.success("验证码已发送，请查收邮箱");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateEmailCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 是否验证通过
     */
    private boolean verifyEmailCode(String email, String code) {
        if (StrUtil.isEmpty(email) || StrUtil.isEmpty(code)) {
            return false;
        }

        Map<String, Object> codeInfo = EMAIL_CODE_CACHE_MAP.get(email);
        if (codeInfo == null) {
            return false;
        }

        // 检查验证码是否过期
        long timestamp = (Long) codeInfo.get("timestamp");
        if (System.currentTimeMillis() - timestamp > EMAIL_CODE_EXPIRATION_TIME) {
            EMAIL_CODE_CACHE_MAP.remove(email);
            return false;
        }

        // 验证验证码是否正确
        String cachedCode = (String) codeInfo.get("code");
        if (code.equals(cachedCode)) {
            // 验证成功后删除验证码（防止重复使用）
            EMAIL_CODE_CACHE_MAP.remove(email);
            return true;
        }

        return false;
    }

    /**
     * 用户注册接口
     *
     * @param registerDTO 注册信息（包含用户信息和邮箱验证码）
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        User user = registerDTO.getUser();
        String emailCode = registerDTO.getEmailCode();

        // 校验必填字段
        if (user == null) {
            return Result.error("用户信息不能为空");
        }
        if (StrUtil.isEmpty(user.getUserAccount())) {
            return Result.error("账号不能为空");
        }
        if (StrUtil.isEmpty(user.getUserPassword())) {
            return Result.error("密码不能为空");
        }
        if (user.getUserPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        if (StrUtil.isEmpty(user.getUserName())) {
            return Result.error("姓名不能为空");
        }
        if (StrUtil.isEmpty(user.getUserPhone())) {
            return Result.error("手机号不能为空");
        }
        if (StrUtil.isEmpty(user.getUserEmail())) {
            return Result.error("邮箱不能为空");
        }
        if (StrUtil.isEmpty(emailCode)) {
            return Result.error("邮箱验证码不能为空");
        }

        // 校验邮箱验证码
        if (!verifyEmailCode(user.getUserEmail(), emailCode)) {
            return Result.error("邮箱验证码错误或已过期");
        }

        // 校验账号格式（6-20位字母、数字或下划线）
        if (!user.getUserAccount().matches("^[a-zA-Z0-9_]{6,20}$")) {
            return Result.error("账号格式不正确，应为6-20位字母、数字或下划线");
        }

        // 校验手机号格式
        if (!user.getUserPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.error("手机号格式不正确");
        }

        // 校验邮箱格式
        if (!user.getUserEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return Result.error("邮箱格式不正确");
        }

        // 检查账号是否已存在
        LambdaQueryWrapper<User> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(User::getUserAccount, user.getUserAccount());
        if (userService.count(accountWrapper) > 0) {
            return Result.error("账号已存在");
        }

        // 检查手机号是否已存在
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getUserPhone, user.getUserPhone());
        if (userService.count(phoneWrapper) > 0) {
            return Result.error("手机号已被注册");
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(User::getUserEmail, user.getUserEmail());
        if (userService.count(emailWrapper) > 0) {
            return Result.error("邮箱已被注册");
        }

        // 设置默认值
        user.setStatus(1); // 状态：1-正常
        user.setTotalIntegral(0); // 初始积分为0（可以改为注册赠送积分，如100）
        user.setCreateTime(LocalDateTime.now());

        // 密码存储：直接存储原始密码（登录时前端会加密）
        // 注意：这里存储的是原始密码，登录时前端会进行SHA-256加密后与数据库中的密码进行对比
        // 但为了安全，建议在后端也进行加密存储，这里先按现有逻辑处理

        // 保存用户
        boolean success = userService.save(user);
        if (success) {
            return Result.success("注册成功，请登录", null);
        }
        return Result.error("注册失败，请稍后重试");
    }

    /**
     * 忘记密码接口（通过邮箱验证码重置密码）
     *
     * @param forgotPasswordDTO 忘记密码信息
     */
    @PostMapping("/forgot-password")
    public Result forgotPassword(@RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        String email = forgotPasswordDTO.getEmail();
        String code = forgotPasswordDTO.getCode();
        String newPassword = forgotPasswordDTO.getNewPassword();

        // 校验必填字段
        if (StrUtil.isEmpty(email)) {
            return Result.error("邮箱不能为空");
        }
        if (StrUtil.isEmpty(code)) {
            return Result.error("验证码不能为空");
        }
        if (StrUtil.isEmpty(newPassword)) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("密码长度不能少于6位");
        }

        // 校验邮箱验证码
        if (!verifyEmailCode(email, code)) {
            return Result.error("邮箱验证码错误或已过期");
        }

        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserEmail, email);
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            return Result.error("该邮箱未注册");
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(newPassword);
        boolean success = userService.updateById(updateUser);

        if (success) {
            return Result.success("密码重置成功，请登录");
        }
        return Result.error("密码重置失败，请稍后重试");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotEmpty(token)) {
            stringRedisTemplate.delete(REDIS_USER_TOKEN_PREFIX + token);
            jwtUtil.expireTokenImmediately(token);
        }
        return Result.success("退出登录成功");
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

    /**
     * 登录失败锁定信息（从 Redis 解析）
     */
    @Data
    private static class LoginLockInfo {
        private final int failCount;
        /** 锁定截止时间戳（毫秒），0 表示未锁定 */
        private final long lockUntilTime;
    }

    private String redisLoginLockKey(String loginKey) {
        return REDIS_LOGIN_LOCK_PREFIX + loginKey;
    }

    /** 从 Redis 读取登录锁信息，value 格式：failCount:lockUntilMs */
    private LoginLockInfo getLoginLockInfo(String loginKey) {
        String val = stringRedisTemplate.opsForValue().get(redisLoginLockKey(loginKey));
        if (StrUtil.isEmpty(val)) {
            return null;
        }
        String[] parts = val.split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            int failCount = Integer.parseInt(parts[0].trim());
            long lockUntil = Long.parseLong(parts[1].trim());
            return new LoginLockInfo(failCount, lockUntil);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 写入 Redis 登录锁，并设置 TTL */
    private void setLoginLock(String loginKey, int failCount, long lockUntilMs, Duration ttl) {
        String key = redisLoginLockKey(loginKey);
        stringRedisTemplate.opsForValue().set(key, failCount + ":" + lockUntilMs, ttl);
    }

    /** 清除 Redis 登录锁 */
    private void clearLoginLock(String loginKey) {
        stringRedisTemplate.delete(redisLoginLockKey(loginKey));
    }

    /**
     * 用户登录信息
     */
    @Data
    static class UserLoginUser {
        private Long id;
        private String name;
        private String avatar;
        private String email;
        private String phone;
        private Integer totalIntegral;
        /** 今日是否已签到（仅 /user/info 返回时有效） */
        private Boolean signedToday;
    }

    /**
     * 注册DTO
     */
    @Data
    static class RegisterDTO {
        private User user;
        private String emailCode;
    }

    /**
     * 忘记密码DTO
     */
    @Data
    static class ForgotPasswordDTO {
        private String email;
        private String code;
        private String newPassword;
    }
}
