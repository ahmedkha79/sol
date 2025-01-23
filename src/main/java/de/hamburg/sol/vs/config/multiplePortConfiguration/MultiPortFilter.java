package de.hamburg.sol.vs.config.multiplePortConfiguration;

import de.hamburg.sol.vs.config.global.GlobalConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter, um unerlaubte Endpunktzugriffe über spezifische Ports zu verhindern
 */

@Component
public class MultiPortFilter extends OncePerRequestFilter {


    private static final int GALAXY_PORT = GlobalConfig.getGalaxyPort();
    @Value("${server.galaxyPath}")
    private String GALAXY_PATH;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Prüfe den Port und den Pfad
        int localPort = request.getLocalPort();
        String requestUri = request.getRequestURI();

        if (localPort == GALAXY_PORT) {
            // Wenn die Anfrage auf Port 8200 kommt, aber der Pfad nicht der erwartete Pfad ist, blockiere die Anfrage
            if (!requestUri.startsWith(GALAXY_PATH)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("404 - Not Found: The requested endpoint is not allowed on this port.");
                response.getWriter().flush();// 404, wenn der Pfad nicht /vs/v1/star ist
                return;
            }
        } else {
            // Wenn die Anfrage nicht auf Port 8200, sondern auf Port 8131 oder einem anderen Port kommt
            if (requestUri.startsWith(GALAXY_PATH)) {
                response.setStatus(404);  // 404, wenn der Pfad /vs/v1/star auf einem anderen Port als 8200 ist
                return;
            }
        }

        // Weiter zur nächsten Filter-Kette
        filterChain.doFilter(request, response);
    }
}
