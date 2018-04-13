<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>找回密码</title>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <meta name="Keywords" content="网站关键词">
    <meta name="Description" content="网站介绍">
    <link rel="stylesheet" href="./css/base.css">
    <link rel="stylesheet" href="./css/iconfont.css">
    <link rel="stylesheet" href="./css/reg.css">
</head>
<body>
<div id="ajax-hook"></div>
<div class="wrap">
    <div class="wpn">
        <div class="form-data find_password">
            <h4>找回密码</h4>
            <p class="p-input pos">
                <label for="pc_tel">请输入密码</label>
                <input type="text" id="pc_tel">
                <span class="tel-warn pc_tel-err hide"><em>&nbsp</em><i class="icon-warn"></i></span>
            </p>
            <p class="p-input pos pc-very">
                <label for="veri-code">请输入帐号</label>
                <input type="number" id="veri-code">
                <a href="javascript:;" class="send"></a>
                <span class="time hide"><em>120</em>s</span>
                <span class="tel-warn error hide"><em></em><i class="icon-warn"></i></span>
            </p>
            <p class="p-input pos code pc-code">
                <label for="veri"></label>
                <input type="text" id="veri">
                <img src="">
                <span class="tel-warn img-err hide"><em></em><i class="icon-warn"></i></span>
            </p>
            <button class="lang-btn next">下一步</button>
            <p class="right">Powered by ZiQin© 2018</p>
        </div>
    </div>
</div>
<script src="./js/jquery.js"></script>
<script src="./js/agree.js"></script>
<script src="./js/reset.js"></script>
</body>
</html>