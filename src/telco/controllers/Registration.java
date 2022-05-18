package telco.controllers;

import java.io.IOException;

import javax.ejb.EJB;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import telco.exceptions.CredentialsException;
import telco.services.UserService;

@WebServlet("/Registration")
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	@EJB(name = "telco.services/UserService")
	private UserService usrService;

	public Registration() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String usrn = null;
		String email = null;
		String pwd = null;
		try {
			usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			if (usrn == null || email == null || pwd == null || usrn.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}
		} catch (Exception e) {
			// for debugging only e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}
		String error;
		try {
			// query db to register the user
			error = usrService.registration(usrn, email, pwd);
		} catch (CredentialsException | NonUniqueResultException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not check credentials");
			return;
		}

		// TODO: to check
		String path;
		if (request.getSession().getAttribute("fromConfirmationPage") == null)
			request.getSession().setAttribute("fromConfirmationPage", false);

		boolean fromConfirmationPage = (boolean) request.getSession().getAttribute("fromConfirmationPage");
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		//

		if (error.equals("OK")) {
			// PREVIOUS:
			/*
			 * String path; ServletContext servletContext = getServletContext(); final
			 * WebContext ctx = new WebContext(request, response, servletContext,
			 * request.getLocale()); ctx.setVariable("registrationMsg",
			 * "Registration successful"); path = "/index.html";
			 * templateEngine.process(path, ctx, response.getWriter());
			 */
			if (fromConfirmationPage) {
				path = getServletContext().getContextPath() + "/GoToLogin";
				request.getSession().setAttribute("registration", "Registration successful");
				response.sendRedirect(path);
			} else {
				ctx.setVariable("registrationMsg", "Registration successful");
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
		} else {
			// PREVIOUS:
			/*
			 * String path; ServletContext servletContext = getServletContext(); final
			 * WebContext ctx = new WebContext(request, response, servletContext,
			 * request.getLocale()); ctx.setVariable("registrationMsg", error); path =
			 * "/index.html"; templateEngine.process(path, ctx, response.getWriter());
			 */
			if (fromConfirmationPage) {
				path = getServletContext().getContextPath() + "/GoToLogin";
				request.getSession().setAttribute("registration", error);
				response.sendRedirect(path);
			} else {
				ctx.setVariable("registrationMsg", error);
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
		}
	}

	public void destroy() {
	}
}
