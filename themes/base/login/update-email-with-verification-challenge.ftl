<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${updateEmailWithVerificationChallengeHeader}
    <#elseif section = "header">
        ${updateEmailWithVerificationChallengeHeader}
    <#elseif section = "form">
        <div id="kc-totp-login-form" class="${properties.kcFormClass!}">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <p for="totp" class="${properties.kcLabelClass!}">
                        ${updateEmailWithVerificationChallengeBody}
                    </p>
                </div>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>