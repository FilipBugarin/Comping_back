package hr.demo.demoProject.services;

import hr.demo.demoProject.config.exception.DemoProjectException;
import hr.demo.demoProject.constants.ProjectErrorMessagesConstants;
import hr.demo.demoProject.domain.ProjectAuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
public abstract class AbstractService {

    public ProjectAuthUser getProjectAuthUser() {
        try {
            return (ProjectAuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (ClassCastException cce) {
            throw new DemoProjectException(ProjectErrorMessagesConstants.UNAUTHORIZED_ACCESS);
        }
    }

    /*
    This code gets the current HttpServletRequest using RequestContextHolder and then checks several common
    headers that may contain the client IP address. If none of these headers contain a valid IP address,
    it falls back to using the getRemoteAddr() method to get the IP address of the client.
    Note that this code assumes that your Spring application is behind a reverse proxy,
    which is a common setup in production environments. If your application is not behind a reverse proxy,
    you can remove the X-Forwarded-For header check.
     */
    public String getClientIpAddress(){
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest httpRequest = attributes.getRequest();

            String clientIp = httpRequest.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getHeader("Proxy-Client-IP");
            }
            if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getHeader("WL-Proxy-Client-IP");
            }
            if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getHeader("HTTP_CLIENT_IP");
            }
            if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
                clientIp = httpRequest.getRemoteAddr();
            }
            return clientIp;
        } catch (Exception e){
            log.error(e.getMessage());
        }
        return "";
    }

    protected HttpServletRequest getRequest() {
         return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
    }

}
