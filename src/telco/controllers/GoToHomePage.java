package telco.controllers;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;

import telco.entities.Order;
import telco.entities.Package;
import telco.entities.SAS;
import telco.entities.User;
import telco.services.OrderService;
import telco.services.PackageService;
import telco.services.SasService;

import java.util.List;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 * Servlet implementation class GoToHomePage
 */
@WebServlet("/GoToHomePage")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	@EJB(name = "telco.services/PackageService")
	private PackageService packageService;

	@EJB(name = "telco.services/OrderService")
	private OrderService orderService;
	
	@EJB(name = "telco.services/SasService")
	private SasService sasService;

	public GoToHomePage() {
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<Package> packages = null;
		packages = packageService.findAllPackages();
		
		String path = "/WEB-INF/HomePage.html";

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("packages", packages);

		// There is the need to check if the user is null since an user can enter the application without logging in
		User user = (User) request.getSession().getAttribute("user");
		if (user != null) {
			List<Order> rejectedOrders = null;
			rejectedOrders = orderService.findRejectedOrdersByUserId(user.getId());
			ctx.setVariable("rejectedOrders", rejectedOrders);
						
			// For demo only we decided to show the service activation schedule
			List<SAS> sas = null;
			sas = sasService.findAllByUser(user);
			ctx.setVariable("sas", sas);
		}

		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
