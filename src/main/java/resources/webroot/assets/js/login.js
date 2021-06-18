
function btn() {

    document.getElementById("sign in").innerHTML =
        "<span class=\"spinner-border spinner-border-sm\" role=\"status\" aria-hidden=\"true\"></span>\n" +
        "            <span class=\"sr-only\">Loading...</span>"

    $.ajax({
        type: "GET",
        datatype: "text",
        url: "/login/verifyCode",
        success: function (data) {
//            if (($('#verifyCode').val() !== "")) {
//                if ($('#verifyCode').val().toLowerCase() === data.toLowerCase()) {
                    getPublicKey();
//                } else if ($('#verifyCode').val().toLowerCase() !== data.toLowerCase()) {
//                    toastr.error("验证码错误")
//                    document.getElementById("sign in").innerHTML = "Sign in"
//                }
//            } else {
//                document.getElementById("sign in").innerHTML = "Sign in"
//            }
//            changeUrl()
        }
    })

}

function getPublicKey() {
    $.ajax({
        type: "GET",
        datatype: "text",
        url: "/login/publicKey",
        success: function (data) {
            // alert(data);
            let encrypt = new JSEncrypt();
            encrypt.setPublicKey(data);

            let usernameEncrypted = encrypt.encrypt($('#username').val())
            let passwordEncrypted = encrypt.encrypt($('#password').val())

            createToken(usernameEncrypted, passwordEncrypted)
        }, error: function () {
            document.getElementById("sign in").innerHTML = "Sign in"
        }
    })
}

function createToken(usernameEncrypted, passwordEncrypted) {
    $.ajax({
        type: "POST",
        dataType: "text",
        url: "/login/createToken",
        data: JSON.stringify({
            'username': usernameEncrypted,
            'password': passwordEncrypted
        }), success: function (data, result, xhr) {
            if (xhr.status === 200) {
                console.log(usernameEncrypted)
                console.log(passwordEncrypted)
                document.cookie = "Token=" + data + ";Path=/"
                if (document.getElementById("remember").checked === true) {
                    localStorage.setItem("username", $("#username").val());
                    localStorage.setItem("password", $("#password").val());
                    console.log("username")
                    console.log(usernameEncrypted)
                    console.log(passwordEncrypted)
                } else {
                    $("#password")
                    localStorage.setItem("username", "");
                    localStorage.setItem("password", "");
                    console.log(usernameEncrypted)
                    console.log(passwordEncrypted)
                }
                window.location.href = "/main/home"
            }
        }, error: function () {
            document.getElementById("sign in").innerHTML = "Sign in"
            toastr.error("账号或密码错误")
        }
    })
}

//function changeUrl() {
//    document.getElementById('verifyCodePic').src = "/login/verifyCodePic" + "?time=" + Date.now();
//}

window.onload = function () {

    history.pushState(null, null, "/login");

    if (localStorage.getItem("username") !== "" && localStorage.getItem("password") !== "") {
        $("#username").val(localStorage.getItem("username"));
        $("#password").val(localStorage.getItem("password"));
        document.getElementById("remember").checked = true;
    } else {
        $("#username").val("");
        $("#password").val("");
        document.getElementById("remember").checked = false;
    }

    $.ajax({
        type: "GET",
        url: "/login/authenticity",
        success: function () {
            window.location.href = "/main/home"
        }
    })
    toastr.info("Welcome to fotoable")
}