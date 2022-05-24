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
import telco.entities.ValPeriod;
import telco.entities.Package;
import telco.services.PackageService;

@WebServlet("/GoToBuyServicePage")
public class GoToBuyServicePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;

	@EJB(name = "telco.services/PackageService")
	private PackageService packageService;

	public GoToBuyServicePage() {
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
		Integer packageId = null;
		Package pack = null;
		List<Product> products = null;
		List<ValPeriod> valPeriods = null;

		if (request.getParameterMap().containsKey("packageId") && request.getParameter("packageId") != ""
				&& !request.getParameter("packageId").isEmpty()) {
			packageId = Integer.parseInt(request.getParameter("packageId"));
		}
		
		if (packageId != null) {
			pack = packageService.findById(packageId);
			products = packageService.findProducts(packageId);
			valPeriods = packageService.findValPeriods(packageId);
		}

		String path = "/WEB-INF/BuyServicePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		if (packageId != null) {
			ctx.setVariable("products", products);
			ctx.setVariable("valperiods", valPeriods);
			ctx.setVariable("package", pack);
		}
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
