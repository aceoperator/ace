package com.quikj.ace.web.client.salesfront.recaptcha;

/**
 * Created by ahtremblay on 15-09-22.
 */
public interface RecaptchaCallback {
    void onSubmit(String response);
}