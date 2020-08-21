package Server.Resources;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * 管理员相关操作路由
 */
public class ManagerResources {

    public void registerResources(Router router) {
        router.route("/manager/searchManagerInfo").handler(this::searchManagerInfo);
    }

    private void searchManagerInfo(RoutingContext routingContext) {

    }

}
