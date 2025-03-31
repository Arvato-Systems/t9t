<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8"/>
        <style>
        </style>
    </head>
 
    <body>
        <p>
        Your account data has just been changed. Type of change: 
        <#if d.changeType == "EMAIL_ADDRESS_CHANGE">
            Email address.
        <#elseif  d.changeType == "PASSWORD_CHANGE">
           Regular change of password.
        <#elseif  d.changeType == "PASSWORD_RESET">
            Password reset (by administrator).
        <#elseif  d.changeType == "PASSWORD_FORGOTTEN">
            Password reset (self service).
        </#if>
        </p>
        If that was you: <br /> 
        You don't need to do anything else.
        <br /> <br />    
        If that wasn't you: <br />
        Check your account for unauthorized access.    
    </body>
</html>
