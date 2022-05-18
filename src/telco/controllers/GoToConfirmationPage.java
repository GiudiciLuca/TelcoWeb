package telco.controllers;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
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

import telco.entities.Package;
import telco.entities.Product;
import telco.services.PackageService;
import telco.services.ProductService;

@WebServlet("/GoToConfirmationPage")
public class GoToConfirmationPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "telco.services/PackageService")
	private PackageService packageService;
	@EJB(name = "telco.services/ProductService")
	private ProductService productService;

	public GoToConfirmationPage() {
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
	
	//TODO: to check
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Integer valPeriod = Integer.parseInt(request.getParameter("valperiod"));
		Date startDate = Date.valueOf(request.getParameter("startdate"));
		Integer packageId = Integer.parseInt(request.getParameter("package"));
		String[] optionalProductsParam = request.getParameterValues("optionalproduct");
		
		Package pack = packageService.findById(packageId);
		
		List<Product> optionalProducts = new ArrayList<Product>();
		for(String s : optionalProductsParam) {
			optionalProducts.add(productService.findByName(s));
		}
		
		String path = "/WEB-INF/ConfirmationPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		ctx.setVariable("valperiod", valPeriod);
		ctx.setVariable("startdate", startDate);
		ctx.setVariable("package", pack);
		ctx.setVariable("optionalproducts", optionalProducts);
		
		//TODO: check with the requirements if it is the correct value of total price
		Integer totalPrice = pack.getMonthlyFee()*valPeriod;
		if(!optionalProducts.isEmpty()) {
			for(Product o : optionalProducts) {
				totalPrice = totalPrice + o.getMonthlyFee()*valPeriod;
			}
		}
		ctx.setVariable("totalprice", totalPrice);
		
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}