package com.kp.mall.util;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
public class AlipayUtil {

    /**
     * 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
     */
    @Value("${alipay.appId}")
    private String appId;

    /**
     * 应用私钥，就是工具生成的应用私钥
     */
    @Value("${alipay.merchantPrivateKey}")
    private String merchantPrivateKey;

    /**
     * 支付宝公钥,对应APPID下的支付宝公钥。
     */
    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;

    /**
     * 支付宝通知我方的外网地址，告诉我们支付成功的信息
     */
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    /**
     * 同步通知，支付成功，我方网页该跳转的地址，一般跳转到成功页
     */
    @Value("${alipay.returnUrl}")
    private String returnUrl;

    /**
     * 签名方式
     */
    @Value("${alipay.signType}")
    private String signType;

    /**
     * 字符编码格式
     */
    @Value("${alipay.charset}")
    private String charset;

    /**
     * 订单超时时间
     */
    private String timeout = "1m";

    /**
     * 支付宝网关
     */
    @Value("${alipay.gatewayUrl}")
    private String gatewayUrl;

    /**
     * 调用支付宝沙箱支付
     *
     * @param orderId  我方订单ID
     * @param money    订单总金额
     * @param goodInfo 商品信息
     * @return         支付表单信息
     * @throws AlipayApiException
     */
    public String pay(String orderId, String money, String goodInfo) throws
            AlipayApiException {
        // 根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new
                DefaultAlipayClient(gatewayUrl, appId, merchantPrivateKey,
                "json", charset, alipayPublicKey, signType);
        // 创建支付请求
        AlipayTradePagePayRequest alipayRequest = getAlipayTradePagePayRequest(orderId, money, goodInfo);
        log.info("支付宝配置：gatewayUrl={}, notifyUrl={}, returnUrl={}", gatewayUrl, notifyUrl, returnUrl);
        log.info("支付宝沙箱支付，订单号：{}，请求参数：{}", orderId, JSONUtil.toJsonStr(alipayRequest));
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        // 会收到支付宝的响应，响应的是一个表单页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        log.info("支付宝沙箱支付，订单号：{}，响应结果：{}", orderId, result);
        return result;

    }

    /**
     * 支付请求构建
     *
     * @param orderId  我方订单ID
     * @param money    订单总金额
     * @param goodInfo 商品信息
     * @return
     */
    private AlipayTradePagePayRequest getAlipayTradePagePayRequest(String orderId, String money, String goodInfo) {
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);
        JSONObject bizContent = new JSONObject();
        bizContent.set("out_trade_no", orderId);  // 我们自己生成的订单编号
        bizContent.set("total_amount", money); // 订单的总金额
        bizContent.set("subject", goodInfo);   // 支付的名称
        bizContent.set("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContent.set("timeout_express", timeout);
        alipayRequest.setBizContent(bizContent.toString());
        return alipayRequest;
    }

}
