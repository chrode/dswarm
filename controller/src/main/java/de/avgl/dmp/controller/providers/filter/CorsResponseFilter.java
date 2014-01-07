package de.avgl.dmp.controller.providers.filter;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.X_POWERED_BY;

/**
 * A filter for providing the CORS headers of a HTTP response.
 * 
 * @author phorn
 *
 */
@Priority(Priorities.HEADER_DECORATOR)
public class CorsResponseFilter implements ContainerResponseFilter {

	/**
	 * {@inheritDoc}<br/>
	 * Creates the CORS headers of a HTTP response.<br>
	 * note: [@tgaengler] note every resource provides all HTTP methods ...
	 */
	@Override
	public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
		final MultivaluedMap<String,Object> headers = responseContext.getHeaders();

		headers.add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		headers.add(ACCESS_CONTROL_ALLOW_METHODS, "GET, OPTIONS, HEAD, PUT, POST, DELETE, PATCH");
		headers.add(ACCESS_CONTROL_ALLOW_HEADERS, "accept, origin, x-requested-with, content-type");

		headers.add(X_POWERED_BY, "DMP/2000");
	}
}
