package telco.controllers;

import java.io.IOException;

import javax.ejb.EJB;
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

import telco.services.UserService;
import telco.entities.User;
import telco.exceptions.CredentialsException;
import javax.persistence.NonUniqueResultException;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	@EJB(name = "telco.services/UserService")
	private UserService userService;

	public CheckLogin() {
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
		// obtain and escape params
		String usrn = null;
		String pwd = null;

		// used to know if CheckLogin is called from the Confirmation page
		if (request.getSession().getAttribute("fromConfirmationPage") == null)
			request.getSession().setAttribute("fromConfirmationPage", false);
		boolean fromConfirmationPage = (boolean) request.getSession().getAttribute("fromConfirmationPage");

		try {
			usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
			pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
			if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			}
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing credential value");
			return;
		}

		User user;
		try {
			user = userService.checkCredentials(usrn, pwd);
		} catch (CredentialsException | NonUniqueResultException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not check credentials");
			return;
		}

		String path;
		if (user == null) {
			// if coming from Confirmation Page (or from Login Registration Page) and user
			// login incorrectly reload the Login Registration Page
			if (fromConfirmationPage) {
				path = getServletContext().getContextPath() + "/GoToLogin";
				request.getSession().setAttribute("loginMsg", "Incorrect username or password");
				response.sendRedirect(path);
			} else {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("errorMsg", "Incorrect username or password");
				path = "/index.html";
				templateEngine.process(path, ctx, response.getWriter());
			}
		} else {

			if (!user.getEmployee())
				request.getSession().setAttribute("user", user);

			// if coming from Confirmation Page (or from Login Registration Page) come back
			// to Confirmation Page
			if (fromConfirmationPage && !user.getEmployee()) {
				request.getSession().removeAttribute("loginMsg");
				String[] optionalProductsName = (String[]) request.getSession().getAttribute("optionalProductsName");
				String optionalProductsPath = "";

				if (optionalProductsName != null) {
					for (String s : optionalProductsName) {
						optionalProductsPath = optionalProductsPath + "&optionalproduct=" + s;
					}
				}

				path = getServletContext().getContextPath() + "/GoToConfirmationPage?valperiod="
						+ request.getSession().getAttribute("valPeriod") + optionalProductsPath + "&startdate="
						+ request.getSession().getAttribute("startDate") + "&package="
						+ request.getSession().getAttribute("packageId");

			} else if (user.getEmployee())
				path = getServletContext().getContextPath() + "/GoToEmployeeHomePage";
			else
				path = getServletContext().getContextPath() + "/GoToHomePage";

			request.getSession().setAttribute("fromConfirmationPage", false);

			response.sendRedirect(path);
		}
	}

	public void destroy() {
	}
}