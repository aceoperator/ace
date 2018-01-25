package com.quikj.ace.common.client.salesfront.recaptcha;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;

/**
 * Created by ahtremblay on 15-09-21.
 */
public class RecaptchaWidget extends Composite {

    public static final String scriptUrl = "https://www.google.com/recaptcha/api.js?onload=comSalesfrontGwtrecaptchaRecaptchaWidgetCallback&render=explicit";
    public static boolean isInjected = false;

    private static String language;

    private Size size = Size.normal;
    private Theme theme = Theme.light;
    private Type type = Type.image;
    private int tabIndex = 0;
    private List<RecaptchaCallback> recaptchaCallbackList = new ArrayList<>();
    private List<RecaptchaExpiredCallback> recaptchaExpiredCallbackList = new ArrayList<>();


    private boolean isLoaded = false;
    private boolean callbackIsRegistered = false;
    private boolean isRendered=false;
    private HTML html = new HTML();
    private static int idCount = 0;
    private String id;
    private String widgetId;
    private String siteKey;

    public RecaptchaWidget(String siteKey){
        initWidget(html);
        this.siteKey = siteKey;
        idCount++;
        id = "com_salesfront_gwtrecaptcha_client_RecaptchaWidget_" + String.valueOf(idCount);
        html.getElement().setId(id);


        if (!callbackIsRegistered){
            registerCallback();
            callbackIsRegistered=true;
        }

        if (!isInjected) {

            String sUrl = scriptUrl;
            if (language !=null)
                sUrl += sUrl + "&hl=" + language;

            ScriptInjector.fromUrl(sUrl).setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
                @Override
                public void onFailure(Exception e) {
                    html.setText("could not load recaptcha");
                }

                @Override
                public void onSuccess(Void aVoid) {
                    if (isAttached())
                        render();
                    isInjected = true;
                }
            }).setRemoveTag(false).inject();
        }

        addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent attachEvent) {
                if (attachEvent.isAttached()) {
                    if (isInjected)
                        render();
                }
            }
        });
    }


    private void render(){

        Timer timer = new Timer() {
            @Override
            public void run() {

                final Timer that = this;
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {

                        if (!checkIsLoaded()) {
                            that.schedule(50);
                            return;
                        }

                        if (isRendered)
                            return;

                        if (isInjected)
                            renderP(id, siteKey, size.name(), theme.name(), type.name(), String.valueOf(tabIndex));
                    }
                });
            }
        };
        timer.schedule(50);
    }

    private native boolean checkIsLoaded() /*-{
        return typeof ($wnd.grecaptcha)!="undefined";
    }-*/;

    private native void renderP(String id, String publicKey, String size, String theme, String type, String tabIndex)/*-{
        var that=this;
        var widget = $wnd.grecaptcha.render(id, {
            'sitekey' : publicKey,
            'size' : size,
            'theme' : theme,
            'type' : type,
            'tabindex' : tabIndex,
            'callback' : function(){
                that.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::fireCallback()();
            },
            'expired-callback' : function(){
                that.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::fireExpiredCallback()();
            }
        });
        this.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::isRendered=true;
        this.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::widgetId=widget;
    }-*/;


    public void reset(){
        resetP(widgetId);
    }

    private native void resetP(String id)/*-{
        $wnd.grecaptcha.reset(id);
    }-*/;

    public String getResponse(){
        return getResponseP(widgetId);
    }

    private native String getResponseP(String widgetId)/*-{
        return $wnd.grecaptcha.getResponse(widgetId);
    }-*/;

    private native void registerCallback() /*-{
        var that=this;
        $wnd.comSalesfrontGwtrecaptchaRecaptchaWidgetCallback = new function(){
            that.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::isLoaded=true;
            that.@com.quikj.ace.common.client.salesfront.recaptcha.RecaptchaWidget::render()();
        };
    }-*/;


    public static void setLanguage(String language) {
        RecaptchaWidget.language = language;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public void registerRecaptchaCallback(RecaptchaCallback recaptchaCallback){
        recaptchaCallbackList.add(recaptchaCallback);
    }

    public void registerRecaptchaExpiredCallback(RecaptchaExpiredCallback recaptchaExpiredCallback){
        recaptchaExpiredCallbackList.add(recaptchaExpiredCallback);
    }

    private void fireCallback(){
        String response = getResponse();
        for(RecaptchaCallback recaptchaCallback : recaptchaCallbackList)
            recaptchaCallback.onSubmit(response);
    }

    private void fireExpiredCallback(){
        for(RecaptchaExpiredCallback recaptchaExpiredCallback : recaptchaExpiredCallbackList)
            recaptchaExpiredCallback.onExpired();
    }


	public static String getLanguage() {
		return language;
	}
}