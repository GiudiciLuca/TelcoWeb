package telco.controllers;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import telco.entities.Product;
import telco.entities.Service;
import telco.entities.ValPeriod;
import telco.services.ProductService;
import telco.services.ServiceService;
import telco.services.ValPeriodService;

@WebServlet("/GoToEmployeeHomePage")
public class GoToEmployeeHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	@EJB(name = "telco.services/ProductService")
	private ProductService pService;
	@EJB(name = "telco.services/ServiceService")
	private ServiceService sService;
	@EJB(name = "telco.services/ValPeriodService")
	private ValPeriodService vService;

	public GoToEmployeeHomePage() {
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

		if (request.getSession().getAttribute("user") != null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User not allowed");
			return;
		}

		List<Product> products = null;
		products = pService.findAllProducts();

		List<Service> services = null;
		services = sService.findAllServices();

		List<ValPeriod> valPeriods = null;
		valPeriods = vService.findAllValPeriods();

		String path = "/WEB-INF/EmployeeHomePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		if (request.getSession().getAttribute("packageMsg") != null) {
			String packageMsg = (String) request.getSession().getAttribute("packageMsg");
			ctx.setVariable("packageMsg", packageMsg);
		}

		if (request.getSession().getAttribute("productMsg") != null) {
			String productMsg = (String) request.getSession().getAttribute("productMsg");
			ctx.setVariable("productMsg", productMsg);
		}

		request.getSession().setAttribute("packageMsg", null);
		request.getSession().setAttribute("productMsg", null);

		ctx.setVariable("products", products);
		ctx.setVariable("services", services);
		ctx.setVariable("valperiods", valPeriods);
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}
