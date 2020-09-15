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

/**
 * 管理员相关操作接口
 */
@Slf4j
public class ManagerResources {

    public void registerResources(Router router) {
        router.route("/manager").handler(this::GmSystemServer);
    }

    /**
     * 数据库操作
     *
     * @param routingContext
     */
    private void GmSystemServer(RoutingContext routingContext) {

        HashMap<String, String> data = JSON.parseObject(routingContext.getBodyAsJson().getString("arguments"), HashMap.class);

        log.info("Manager receive args：" + data);
        
        String operation = data.get("operation");

        switch (operation) {
            case "deleteAuth":
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
            case "addAuth":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String server = data.get("server");
                    String auth = data.get("auth");
                    ManagerDatabaseHelper.addAuth(username, server, auth);
                    future.complete("Add succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", asyncResult.result().toString());
                });
                break;
            case "deleteUser":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    ManagerDatabaseHelper.deleteUser(username);
                    future.complete("Delete succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Delete failed!", "str", asyncResult.result().toString());
                });
                break;
            case "addUser":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String password = data.get("password");
                    List<String> userInfo = Arrays.asList(username, password, "token", "1");
                    ManagerDatabaseHelper.addUser(userInfo);
                    future.complete("Add succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", asyncResult.result().toString());
                });
                break;
            case "updateUserStatus":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    ManagerDatabaseHelper.updateUserStatus(username);
                    future.complete("Update succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Update failed!", "str", asyncResult.result().toString());
                });
                break;
            case "updateUserInfo":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String password = data.get("password");
                    ManagerDatabaseHelper.updateUserInfo(username, password);
                    future.complete("Update succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Update failed!", "str", asyncResult.result().toString());
                });
                break;
            case "selectAuthList":
                routingContext.vertx().executeBlocking(future -> {
                    String username = data.get("username");
                    String server = data.get("server");
                    HashMap<String, Object> resultHashMap = new HashMap<>();
                    List<String> list = ManagerDatabaseHelper.selectAuthList(username, "list", server);
                    List<String> listBtn = ManagerDatabaseHelper.selectAuthList(username, "btn", server);
                    List<String> colName = Arrays.asList("list","btn");
                    List<List<String>> body = Arrays.asList(list, listBtn);
                    resultHashMap.put("colName", colName);
                    resultHashMap.put("tableBody", body);
                    future.complete(resultHashMap);
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
    private void executeResult(RoutingContext routingContext, AsyncResult<Object> asyncResult, String info, String type, Object resultData) {
        if (asyncResult.failed()) {
            routingContext.fail(asyncResult.cause());
            log.info(info);
            return;
        }
        if ("table".equals(type)) {
			routingContext.response().end(XmlMapping.createReturnString("table", JSON.toJSONString(resultData), false, null));
        } else if ("str".equals(type)) {
            //routingContext.response().end(resultData.toString());
            routingContext.response().end(XmlMapping.createReturnString("str", JSON.toJSONString(resultData), false, null));
        }
    }

}
