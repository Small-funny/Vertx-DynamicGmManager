package Server.Resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.ManagerDatabaseHelper;

import com.alibaba.fastjson.JSON;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import static Server.Automation.PageUtil.*;
import static Server.DatabaseHelper.ManagerDatabaseHelper.*;

/**
 * 管理员相关操作接口
 */
@Slf4j
public class ManagerResources {
    // 删除权限
    private static final String OPERATION_DELETE_AUTH = "deleteAuth";
    // 增加权限
    private static final String OPERATION_ADD_AUTH = "addAuth";
    // 删除用户
    private static final String OPERATION_DELETE_USER = "deleteUser";
    // 增加用户
    private static final String OPERATION_ADD_USER = "addUser";
    // 更新用户信息
    private static final String OPERATION_UPDATE_USERINFO = "updateUserInfo";
    // 查询权限列表
    private static final String OPERATION_SELECT_AUTHLIST = "selectAuthList";
    private HashMap<String, String> data;

    public void registerResources(Router router) {
        router.route("/manager").handler(this::GmSystemServer);
    }

    /**
     * 数据库操作
     *
     * @param routingContext
     */
    @SuppressWarnings("unchecked")
    private void GmSystemServer(RoutingContext routingContext) {
        data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);
        String operation = data.get("operation");
        String username = data.get("username");
        String server = data.get("route").split("/")[1];
        System.out.println("server:"+server);
        String page = data.get("route").split("/")[0];
        log.info("Manager receive args：" + data);

        switch (operation) {
            case OPERATION_DELETE_AUTH:
                String deleteAuth = zhAuth2en(data.get("auth"));
                String type = data.get("type");
                returnResult(routingContext, "return", deleteAuth(username, server, deleteAuth, type));
                break;
            case OPERATION_ADD_AUTH:
                String addAuth = zhAuth2en(data.get("auth"));
                returnResult(routingContext, "return", addAuth(username, server, addAuth));
                break;
            case OPERATION_DELETE_USER:
                returnResult(routingContext, "return", deleteUser(username));
                break;
            case OPERATION_ADD_USER:
                String addUserPassword = data.get("password");
                List<String> userInfo = Arrays.asList(username, addUserPassword, "token");
                returnResult(routingContext, "return", addUser(userInfo));
                break;
            case OPERATION_UPDATE_USERINFO:
                String updateUserPassword = data.get("password");
                returnResult(routingContext, "return", updateUserInfo(username, updateUserPassword));
                break;
            case OPERATION_SELECT_AUTHLIST:
                HashMap<String, String> resultHashMap = selectAuthTable(username, server);
                String hashStr = JSON.toJSONString(resultHashMap);
                returnResult(routingContext, "table", hashStr);
                break;
            default:
                break;
        }
    }

    /**
     * 结果处理
     *
     * @param routingContext
     * @param type
     * @param resultData
     */
    private void returnResult(RoutingContext routingContext, String type, String resultData) {
        if ("table".equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("table", resultData, false, data));
        } else if ("str".equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("str", JSON.toJSONString(resultData), false, data));
        } else if ("return".equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("return", resultData, false, null));
        }
    }
}