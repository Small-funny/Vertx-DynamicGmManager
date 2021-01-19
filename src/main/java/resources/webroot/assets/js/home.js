var flag
var prepage = null;
var pageName = null
var operatorName
var route
var ip = null
window.onload = function () {
    //再提交后把输入框的内容变成刚刚查找的配置名

    operatorName = $("#operatorName")
    toastr.success("登陆成功")
    ip = returnCitySN["cip"]
}

function logout() {
    document.cookie = "Token=token;Path=/"
    window.location.href = "/login";
}


function submitForm() {
    var server = $("#selServer").val();
    window.location.href = "/main/" + server + "/0"
}


function refreshAjaxGet(server, page, section) {
    console.log("ajaxget")
    $.ajax({
        type: "GET",
        dataType: "text",
        url: "/subMain" + "/" + server + "/" + page,

        data: JSON.stringify({
            "URLS": server + page
        }), success: function (data) {
            $("#" + section).html(data)
        }, error: function () {
            toastr.warning("操作失败")
        }
    })
}

function refreshAjaxPost(url, page, arguments, section) {
    $.ajax({
        type: "POST",
        dataType: "text",
        url: url,
        data: JSON.stringify({
            'page': page,
            'arguments': arguments
        }), success: function (data) {
            $("#" + section).html(data)
        }, error: function () {
            toastr.warning("操作失败")
        }
    })
}

function refreshAjaxPostAlert(url, page, arguments, section) {
    $.ajax({
        type: "POST",
        dataType: "text",
        url: url,
        // contentType: false,
        // processData: false,

        data: JSON.stringify({
            'page': page,
            'arguments': arguments
        }), success: function (data) {
            console.log(section)
            $("#" + section).html(data)
        }, error: function () {
            toastr.warning("操作失败")
        }
    })
}

function changeAside(server, page, list, table, subList) {
    pageName = page
    //侧边栏添加选中状态和展开状态
    $("#" + prepage).removeClass("active")
    $("#" + page).addClass("active")
    prepage = page;
    refreshAjaxGet(server, page, "pageContent")
    var json = {}
    history.pushState(null, null, "/main" + "/" + server + "/" + page);
    route = "/" + server + "/" + page
    json["route"] = route
    json["list"] = list
    json["table"] = table
    json["subList"] = subList
    refreshAjaxPost("/main/configsName", page, JSON.stringify(json), "configsName")
    refreshAjaxPost("/main/userInfo", page, JSON.stringify(json), "userInfo")
    refreshAjaxPost("/main/subList", page, JSON.stringify(json), "subList")
    $("input[type='hidden']").each(function () {
        $(this).attr({from: "return"})
    })
    $("#returnContent").html(" ")
}

function changeReturn(urls, fromValue, object, special) {

    if (special !== "null") {
        if (!confirm(special))
            return true;
    }
    {
        let hasSend = false
        var obj = $(object)
        console.log(obj.parent().parent().parent())
        let json = {};
        let subJson = [];
        json["operatorName"] = operatorName.text()
        console.log(ip)
        json["ip"] = ip
        obj.parent().parent().parent().children().each(function (index) {
            if ($(this).attr("type") === "hidden") {
                json[$(this).attr("id")] = $(this).attr("value")
            } else if ($(this).attr("class") === "row form-group") {
                $(this).children().each(function (index) {
                    if (index === 1) {
                        $(this).children().each(function (index) {
                            var idNamename = $(this).attr("id")
                            //json[$(this).attr('id')] = $(this).val();
                            if ($(this).attr("type") === "file") {
                                var formData = new FormData();
                                formData.append("file",$(this).get(0).files[0]);
                                $.ajax({
                                    url:'http://localhost:8000/upload',
                                    dataType:'json',
                                    type:'POST',
                                    async: false,
                                    data: formData,
                                    processData : false, // 使数据不做处理
                                    contentType : false, // 不要设置Content-Type请求头
                                    success: function(data){
                                        alert(data);
                                        if (data.status == 'ok') {
                                            alert('上传成功！');
                                        }

                                    },
                                    error:function(response){
                                        alert(response);
                                        console.log(response);
                                    }
                                });
                            } else {
                                json[$(this).attr("id")] = $(this).val();
                            }
                            json[$(this).attr("id")] = $(this).val();
                        })
                    }
                })
            } else {
                $(this).children().each(function (index) {
                    let subsubjson = []
                    $(this).children().each(function (index) {
                        if (index === $(this).parent().children().length - 1) {
                            return true
                        }
                        $(this).children().each(function (index) {
                            if (index === 1) {
                                $(this).children().each(function (index) {
                                    console.log(index)
                                    subsubjson.push($(this).val())
                                })
                            }
                        })
                    })
                    subJson.push(subsubjson)
                })
                json["accessory"] = JSON.stringify(subJson)
            }
        })

        console.log(json)
        console.log(hasSend)
        if (!hasSend) {
            refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "returnContent")
            refreshAjaxPost("/main/userInfo", pageName, JSON.stringify(json), "userInfo")
        }
    }
}

function updateAuth(urls, operation) {
    var list = [];
    $("input[checked='checked']").each(function () {
        list.push($(this).attr('value'))
        console.log($(this))
    })
    console.log(list)
    var json = {};
    json["authList"] = list
    json["route"] = route
    json["operation"] = operation
    json["username"] = $("#username").val()
    json["serverAuth"] = $("#serverAuth").val()
    json["authType"] = $("#authType").val()
    if (operation === "updateAuth") {
        refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "userInfo")
    } else if (operation === "deleteUsers") {
        refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "returnContent")
        refreshAjaxPost("/main/userInfo", pageName, JSON.stringify(json), "userInfo")
    }
}

function updateCheckbox(chechbox) {
    console.log(chechbox.attr("checked"))
    if (chechbox.attr("checked") === "checked") {
        chechbox.removeAttr("checked")
    } else {
        chechbox.attr("checked", true)
    }
    console.log(chechbox.attr("checked"))
}

function userDelete(column, operation) {
    var subjson = {}
    subjson["operation"] = operation
    subjson["username"] = column.parent().parent().find("td").get(0).innerHTML
    subjson["route"] = route
    refreshAjaxPostAlert("/manager", pageName, JSON.stringify(subjson), "returnContent")
    refreshAjaxPost("/main/userInfo", pageName, JSON.stringify(subjson), "userInfo")
}

function authDelete(column, operation) {
    var subjson = {}
    subjson["operation"] = operation
    subjson["username"] = $("#username").val().toString();
    console.log($("#username").val())
    subjson["auth"] = column.parent().parent().find("td").get(0).innerHTML
    subjson["type"] = column.parent().parent().find("td").get(1).innerHTML
    subjson["route"] = route
    refreshAjaxPostAlert("/manager", pageName, JSON.stringify(subjson), "returnContent")
    refreshAjaxPost("/main/userInfo", pageName, null, "userInfo")
}

function updateReturn(urls) {
    var json = {}
    $("input[from='return']").each(function () {
        json[$(this).attr('name')] = $(this).val()
    })

    json["operatorName"] = operatorName.text()
    json['body'] = $("#text").val()
    json['args'] = $("#args").val()
    json["ip"] = ip
    $("#subPassword").val(" ")
    refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "userInfo")
}

function dlbclick(argDivName, argsName) {
    document.getElementById(argDivName).value = argsName
    document.getElementById('returnContent').innerHTML = ' '

}

function isJson(divName) {

    str = document.getElementById(divName).value
    if (typeof str == 'string') {
        try {
            var obj = JSON.parse(str);
            if (typeof obj == 'object' && obj) {
                alert("true")
                return true;
            } else {
                alert("false")
                return false;
            }

        } catch (e) {
            console.log('error：' + str + '!!!' + e);
            alert("false")
            return false;
        }
    }
    console.log('It is not a string!')
}

function copyDiv() {
    $('#accessory').clone().appendTo($('#accessory').parent());
}

function deleteDiv(object) {
    var obj = $(object)
    obj.parent().parent().parent().remove();
}

function uploadFile(url, page, arguments, section) {

    //1.需要先利用FormData内置对象
    let formDateObj = new FormData();
    // //2.添加普通键值对
    // formDateObj.append('username',$('#d1').val());
    // formDateObj.append('password',$('#d2').val());
    //3.添加文件对象
    formDateObj.append('myfile', $('#d3')[0].files[0])
    //4.将对象基于ajax发送给后端
    $.ajax({
        url: url,
        type: 'post',
        data: formDateObj,  //直接将对象放在data后面

        //ajax发送文件必须要指定两个参数
        contentType: false,  //不要使用任何编码，django后端能够自动识别formdata对象
        processData: false,  //告诉浏览器不要对你的数据进行任何处理

        success: function (args) {

        }
    })


}