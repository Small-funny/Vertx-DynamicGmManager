var flag
var prepage = null;
var pageName = null
var userName = null
var route
window.onload =  function () {
    //再提交后把输入框的内容变成刚刚查找的配置名

    toastr.success("登陆成功")


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

function changeAside(server, page, list, table) {
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
    refreshAjaxPost("/main/configsName", page, JSON.stringify(json), "configsName")
    refreshAjaxPost("/main/userInfo", page, JSON.stringify(json), "userInfo")

    $("input[type='hidden']").each(function () {
        $(this).attr({from: "return"})
    })
    $("#returnContent").html(" ")
}

function changeReturn(urls, fromValue) {
    var json = {};
    $("input[from='" + fromValue + "']").each(function () {
        json[$(this).attr('id')] = $(this).val()
    })
    $("select[from='" + fromValue + "']").each(function () {
        json[$(this).attr('id')] = $(this).val()
    })
    $("textarea[from='" + fromValue + "']").each(function () {
        json[$(this).attr('id')] = $(this).val()
    })
    console.log(json)
    refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "returnContent")
    refreshAjaxPost("/main/userInfo", pageName, JSON.stringify(json), "userInfo")
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

    json['body'] = $("#text").val()
    json['args'] = $("#args").val()
    console.log(JSON.stringify(json))
    refreshAjaxPostAlert(urls, pageName, JSON.stringify(json), "userInfo")

}

