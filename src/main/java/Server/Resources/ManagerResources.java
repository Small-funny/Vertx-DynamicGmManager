package Server.Resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Server.Automation.XmlMapping;
import Server.DatabaseHelper.ManagerDatabaseHelper;
import Server.DatabaseHelper.VerifyDatabaseHelper;

import com.alibaba.fastjson.JSON;
import io.vertx.core.AsyncResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

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
        HashMap<String, String> data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);
        String operation = data.get("operation");

        log.info("Manager receive args：" + data);

        switch (operation) {
            case OPERATION_DELETE_AUTH:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String server = data.get("server");
                    String auth = data.get("auth");
                    ManagerDatabaseHelper.deleteAuth(username, server, auth);
                    future.complete("Delete succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Delete failed!", "str", "");
                });
                break;
            case OPERATION_ADD_AUTH:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String server = data.get("server");
                    String auth = data.get("auth");
                    String type = data.get("type");
                    ManagerDatabaseHelper.addAuth(username, server, auth, type);
                    future.complete("Add succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", asyncResult.result().toString());
                });
                break;
            case OPERATION_DELETE_USER:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    ManagerDatabaseHelper.deleteUser(username);
                    future.complete("Delete succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Delete failed!", "str", asyncResult.result().toString());
                });
                break;
            case OPERATION_ADD_USER:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String password = data.get("password");
                    String response = "Add failed!";
                    if (!VerifyDatabaseHelper.isExisted(username)) {
                        List<String> userInfo = Arrays.asList(username, password, "token", "1");
                        ManagerDatabaseHelper.addUser(userInfo);
                        response = "Add succeed!";
                    }
                    future.complete(response);
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", asyncResult.result().toString());
                });
                break;
            case OPERATION_UPDATE_USERINFO:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String password = data.get("password");
                    ManagerDatabaseHelper.updateUserInfo(username, password);
                    future.complete("Update succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Update failed!", "str", asyncResult.result().toString());
                });
                break;
            case OPERATION_SELECT_AUTHLIST:
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String server = data.get("server");
                    HashMap<String, String> resultHashMap = ManagerDatabaseHelper.selectAuthTable(username, server);
                    String hashStr = JSON.toJSONString(resultHashMap);
                    future.complete(hashStr);
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Select failed!", "table", asyncResult.result().toString());
                });
                break;
            default:
                break;
        }
    }

    /**
     * 阻塞结果处理
     *
     * @param routingContext
     * @param asyncResult
     * @param info
     * @param type
     * @param resultData
     */
    private void executeResult(RoutingContext routingContext, AsyncResult<Object> asyncResult, String info, String type, String resultData) {
        if (asyncResult.failed()) {
            routingContext.fail(asyncResult.cause());
            log.info(info);
            return;
        }
        if ("table".equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("table", resultData, false, null));
        } else if ("str".equals(type)) {
            routingContext.response().end(XmlMapping.createReturnString("str", JSON.toJSONString(resultData), false, null));
        }
    }

}
