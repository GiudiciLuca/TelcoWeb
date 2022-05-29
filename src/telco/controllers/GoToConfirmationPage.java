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
import telco.entities.User;
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
		Integer packageId;
		String[] optionalProductsName = null;

		if (request.getParameter("orderId") != null) {
			
			User sessionUser = (User) request.getSession().getAttribute("user");
			if (sessionUser == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee not allowed");
				return;
			}

			Integer orderId = null;
			try {
				orderId = Integer.parseInt(request.getParameter("orderId"));
			} catch (NumberFormatException e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request on order parameter");
				return;
			}

			Order o = orderService.findById(orderId);

			if (o == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect order parameter");
				return;
			}

			if (o.getUser().getId() != sessionUser.getId() | o.isValid()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect order parameter");
				return;
			}

			request.getSession().setAttribute("rejectedOrder", o);

			optionalProducts = o.getProducts();
			valPeriodId = o.getValPeriod().getId();
			validityPeriod = o.getValPeriod();
			startDate = o.getStartDate();
			pack = o.getPackage();
		} else {
			// Check on bad request
			if (request.getParameter("valperiod") == null | request.getParameter("startdate") == null
					| request.getParameter("package") == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request on order parameters");
				return;
			}

			try {
				valPeriodId = Integer.parseInt(request.getParameter("valperiod"));
				startDate = Date.valueOf(request.getParameter("startdate"));
				packageId = Integer.parseInt(request.getParameter("package"));
				optionalProductsName = request.getParameterValues("optionalproduct");
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request on order parameters");
				return;
			}

			pack = packageService.findById(packageId);
			validityPeriod = valPeriodService.findById(valPeriodId);

			if (pack == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Incorrect package associated to order parameters");
				return;
			}

			// Used to validate data from the request valperiod parameter
			boolean checkOnVp = false;
			for (ValPeriod v : pack.getValPeriods()) {
				if (v.getId() == valPeriodId) {
					checkOnVp = true;
					break;
				}
			}
			if (!checkOnVp) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Incorrect validity period associated to order parameters");
				return;
			}

			request.getSession().setAttribute("valPeriod", valPeriodId);
			request.getSession().setAttribute("startDate", startDate);
			request.getSession().setAttribute("packageId", packageId);
			request.getSession().setAttribute("optionalProductsName", optionalProductsName);

			// Used to validate data from the request products parameter
			Product prod;
			List<Product> packageProducts = pack.getProducts();
			boolean checkOnP = false;
			if (optionalProductsName != null) {
				for (String s : optionalProductsName) {
					Integer optionalProduct = null;
					try {
						optionalProduct = Integer.parseInt(s);
					} catch (Exception e) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"Bad request on products associated to order parameters");
						return;
					}
					for (Product p : packageProducts) {
						if (p.getId() == optionalProduct) {
							checkOnP = true;
							break;
						}
					}
					if (!checkOnP) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"Incorrect products associated to order parameters");
						return;
					}
					prod = productService.findById(optionalProduct);
					optionalProducts.add(prod);
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

		Integer totalPrice = validityPeriod.getMonthlyfee() * validityPeriod.getMonths();
		if (!optionalProducts.isEmpty()) {
			for (Product o : optionalProducts) {
				totalPrice = totalPrice + o.getMonthlyFee() * validityPeriod.getMonths();
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