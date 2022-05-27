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

import telco.entities.Alert;
import telco.entities.Order;
import telco.entities.User;
import telco.forTriggers.AmountSalesPerPackage;
import telco.forTriggers.AverageProductsPerPackage;
import telco.forTriggers.BestSellerOptProduct;
import telco.forTriggers.PurchasesPerPackage;
import telco.forTriggers.PurchasesPerPackageAndVp;
import telco.forTriggers.ViewService;
import telco.services.AlertService;
import telco.services.OrderService;
import telco.services.UserService;

@WebServlet("/GoToSalesReportPage")
public class GoToSalesReportPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	
	@EJB(name = "telco.forTriggers/ViewService")
	private ViewService viewService;
	@EJB(name = "telco.services/AlertService")
	private AlertService aService;
	@EJB(name = "telco.services/UserService")
	private UserService uService;
	@EJB(name = "telco.services/OrderService")
	private OrderService oService;
	
	public GoToSalesReportPage() {
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
		
		List<PurchasesPerPackage> ppp = viewService.totalPurchasesPerPackage();
		List<PurchasesPerPackageAndVp> pppvp = viewService.totalPurchasesPerPackageAndVp();
		List<AmountSalesPerPackage> aspp = viewService.totalAmountSalesPerPackage();
		List<AverageProductsPerPackage> appp = viewService.totalAverageProductsPerPackage();
		List<BestSellerOptProduct> bsop = viewService.totalBestSellerOptProduct();
		BestSellerOptProduct best = null;
		List<User> insolventUsers = uService.findInsolvents();
		List<Order> rejectedOrders = oService.findAllRejectedOrders();
		List<Alert> totAlerts = aService.findAllAlerts();
		
		
		String path = "/WEB-INF/SalesReportPage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		if (!bsop.isEmpty())
			best = bsop.get(0);
		
		ctx.setVariable("ppp", ppp);
		ctx.setVariable("pppvp", pppvp);
		ctx.setVariable("aspp", aspp);
		ctx.setVariable("appp", appp);
		ctx.setVariable("best", best);
		ctx.setVariable("insolventUsers", insolventUsers);
		ctx.setVariable("rejectedOrders", rejectedOrders);
		ctx.setVariable("totAlerts", totAlerts);
		
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
	
}
