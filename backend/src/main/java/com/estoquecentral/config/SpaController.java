package com.estoquecentral.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SpaController - Handles Angular SPA routing
 *
 * <p>This controller forwards all non-API requests to index.html,
 * allowing Angular Router to handle client-side routing.
 *
 * <p>Example:
 * - /login → forward to /index.html (Angular handles routing)
 * - /produtos → forward to /index.html (Angular handles routing)
 * - /api/produtos → NOT forwarded (handled by REST controllers)
 */
@Controller
public class SpaController {

    /**
     * Forwards all non-API requests to Angular's index.html.
     *
     * <p>This allows Angular Router to handle client-side routing
     * without getting 404 errors from the backend.
     *
     * @return forward to index.html
     */
    @RequestMapping(value = {
            "/",
            "/login",
            "/produtos",
            "/produtos/**",
            "/categorias",
            "/categorias/**",
            "/estoque/**",
            "/clientes",
            "/clientes/**",
            "/pdv",
            "/integracoes",
            "/integracoes/**",
            "/dashboard"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
