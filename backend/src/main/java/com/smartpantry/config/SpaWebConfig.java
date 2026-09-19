package com.smartpantry.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards any deep-link browser navigation (e.g. refreshing on /pantry)
 * to index.html so React Router can take over client-side routing.
 * Excludes /api, /v3/api-docs and /swagger-ui paths, and any request that
 * already targets a real static file (contains a dot, e.g. main.js).
 */
@Controller
public class SpaWebConfig {

    @RequestMapping(value = {
            "/",
            "/{path:^(?!api|v3|swagger-ui|actuator)[^\\.]*}",
            "/{path:^(?!api|v3|swagger-ui|actuator)[^\\.]*}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
