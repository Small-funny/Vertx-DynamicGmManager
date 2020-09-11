package Server.Resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Server.DatabaseHelper.ManagerDatabaseHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.http.HttpServerRequest;
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
     * @param routingContext
     */
    private void GmSystemServer(RoutingContext routingContext) {
        log.info("Manager receive args：" + routingContext.getBodyAsString());

        HttpServerRequest request = routingContext.request();
        String route = request.getFormAttribute("route");
        String operation = request.getFormAttribute("operation");

        switch (operation) {
            case "allManagerInfo" :
                routingContext.vertx().executeBlocking(future -> {
                    HashMap<String, String> tableData;
                    tableData = ManagerDatabaseHelper.allManagerInfo();
                    future.complete(tableData);
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Select failed!", "table", route, asyncResult.result().toString());
                });
                break;
            case "deleteAuth" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    String server = request.getFormAttribute("server");
                    String auth = request.getFormAttribute("auth");
                    ManagerDatabaseHelper.deleteAuth(username, server, auth);
                    future.complete("Delete succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Delete failed!", "str", route, "");
                });
                break;
            case "addAuth" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    String server = request.getFormAttribute("server");
                    String auth = request.getFormAttribute("auth");
                    ManagerDatabaseHelper.addAuth(username, server, auth);
                    future.complete("Add succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", route, asyncResult.result().toString());
                });
                break;
            case "deleteUser" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    ManagerDatabaseHelper.deleteUser(username);
                    future.complete("Delete succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Delete failed!", "str", route, asyncResult.result().toString());
                });
                break;
            case "addUser" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    String password = request.getFormAttribute("password");
                    List<String> userInfo = Arrays.asList(username, password, "token", "1");
                    ManagerDatabaseHelper.addUser(userInfo);
                    future.complete("Add succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Add failed!", "str", route, asyncResult.result().toString());
                });
                break;
            case "updateUserStatus" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    ManagerDatabaseHelper.updateUserStatus(username);
                    future.complete("Update succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Update failed!", "str", route, asyncResult.result().toString());
                });
                break;
            case "updateUserInfo" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    String password = request.getFormAttribute("password");
                    ManagerDatabaseHelper.updateUserInfo(username, password);
                    future.complete("Update succeed!");
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Update failed!", "str", route, asyncResult.result().toString());
                });
                break;
            case "selectAuthList" :
                routingContext.vertx().executeBlocking(future -> {
                    String username = request.getFormAttribute("username");
                    String type = request.getFormAttribute("type");
                    String server = request.getFormAttribute("server");
                    System.out.println(username+"  "+type+"   "+server);
                    List<String> authList = ManagerDatabaseHelper.selectAuthList(username, type, server);
                    future.complete(authList);
                }, false, asyncResult -> {
                    executeResult(routingContext, asyncResult, "Select failed!", "str", route, asyncResult.result().toString());
                });
                break;
            default :
                break;
        }
    }

    /**
     * 阻塞结果处理
     * @param routingContext
     * @param asyncResult
     * @param info
     * @param type
     * @param route
     * @param resultData
     */
    private void executeResult(RoutingContext routingContext, AsyncResult<Object> asyncResult, String info, String type, String route, String resultData) {
        if (asyncResult.failed()) {
            routingContext.fail(asyncResult.cause());
            log.info(info);
            return;
        }
        routingContext.put("type", type).put("data", resultData).put("route", route).reroute("/main" + route);
    }
    
}
