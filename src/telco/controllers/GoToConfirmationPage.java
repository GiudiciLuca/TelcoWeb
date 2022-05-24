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

import telco.entities.Order;
import telco.entities.Package;
import telco.entities.Product;
import telco.entities.ValPeriod;
import telco.services.OrderService;
import telco.services.PackageService;
import telco.services.ProductService;
import telco.services.ValPeriodService;

@WebServlet("/GoToConfirmationPage")
public class GoToConfirmationPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "telco.services/PackageService")
	private PackageService packageService;
	@EJB(name = "telco.services/ProductService")
	private ProductService productService;
	@EJB(name = "telco.services/ValPeriodService")
	private ValPeriodService valPeriodService;
	@EJB(name = "telco.services/OrderService")
	private OrderService orderService;

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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Integer valPeriodId;
		ValPeriod validityPeriod;
		Date startDate;
		Package pack;
		List<Product> optionalProducts = new ArrayList<Product>();
		
		if(request.getParameter("orderId") != null) {
			Integer orderId = Integer.parseInt(request.getParameter("orderId"));
			Order o = orderService.findById(orderId);
			
			//
			request.getSession().setAttribute("rejectedOrder", o);
			
			optionalProducts = o.getProducts();
			valPeriodId = o.getValPeriod().getId();
			validityPeriod = o.getValPeriod();
			startDate = o.getStartDate();
			pack = o.getPackage();
		} else {
			valPeriodId = Integer.parseInt(request.getParameter("valperiod"));
			startDate = Date.valueOf(request.getParameter("startdate"));
			Integer packageId = Integer.parseInt(request.getParameter("package"));
			String[] optionalProductsName = request.getParameterValues("optionalproduct");
			request.getSession().setAttribute("valPeriod", valPeriodId);
			request.getSession().setAttribute("startDate", startDate);
			request.getSession().setAttribute("packageId", packageId);
			request.getSession().setAttribute("optionalProductsName", optionalProductsName);
			
			pack = packageService.findById(packageId);
			validityPeriod = valPeriodService.findById(valPeriodId);
			
			if(optionalProductsName != null) {
				for(String s : optionalProductsName) {
					Integer optionalProduct = Integer.parseInt(s);
					optionalProducts.add(productService.findById(optionalProduct));
				}
			}
		}
		
		String path = "/WEB-INF/ConfirmationPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		ctx.setVariable("valperiod", valPeriodId);
		ctx.setVariable("startdate", startDate);
		ctx.setVariable("package", pack);
		ctx.setVariable("optionalproducts", optionalProducts);
		
		Integer totalPrice = validityPeriod.getMonthlyfee()*validityPeriod.getMonths();
		if(!optionalProducts.isEmpty()) {
			for(Product o : optionalProducts) {
				totalPrice = totalPrice + o.getMonthlyFee()*validityPeriod.getMonths();
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