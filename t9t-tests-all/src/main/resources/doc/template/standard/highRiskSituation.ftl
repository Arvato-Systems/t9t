<#--

    Copyright (c) 2012 - 2023 Arvato Systems GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
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