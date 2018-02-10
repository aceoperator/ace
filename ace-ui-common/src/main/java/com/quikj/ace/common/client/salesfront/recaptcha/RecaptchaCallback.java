package com.quikj.ace.common.client.salesfront.recaptcha;

/**
 * Created by ahtremblay on 15-09-22.
 */
public interface RecaptchaCallback {
    void onSubmit(String response);
}