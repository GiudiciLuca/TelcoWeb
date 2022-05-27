package telco.controllers;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import telco.entities.Order;
import telco.entities.Package;
import telco.entities.Product;
import telco.entities.User;
import telco.entities.ValPeriod;
import telco.services.AlertService;
import telco.services.OrderService;
import telco.services.PackageService;
import telco.services.ProductService;
import telco.services.SasService;
import telco.services.UserService;
import telco.services.ValPeriodService;

@WebServlet("/HandleOrder")
public class HandleOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@EJB(name = "telco.services/OrderService")
	private OrderService oService;
	@EJB(name = "telco.services/PackageService")
	private PackageService packageService;
	@EJB(name = "telco.services/ValPeriodService")
	private ValPeriodService vService;
	@EJB(name = "telco.services/ProductService")
	private ProductService productService;
	@EJB(name = "telco.services/SasService")
	private SasService sasService;
	@EJB(name = "telco.services/AlertService")
	private AlertService aService;
	@EJB(name = "telco.services/UserService")
	private UserService uService;

	public HandleOrder() {

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Integer packageId = Integer.parseInt(request.getParameter("packageid"));
		Integer totalPrice = Integer.parseInt(request.getParameter("totalprice"));
		Integer valPeriodId = Integer.parseInt(request.getParameter("valperiod"));
		String[] productsName = request.getParameterValues("products");
		Date startDate = Date.valueOf(request.getParameter("startdate"));
		String typeOfPayment = request.getParameter("typeofpayment");
		User user = (User) request.getSession().getAttribute("user");

		Order rejectedOrder = null;
		if (request.getSession().getAttribute("rejectedOrder") != null)
			rejectedOrder = (Order) request.getSession().getAttribute("rejectedOrder");

		// TODO need to add all the check for the parameter
		if (packageId == null | totalPrice == null | valPeriodId == null | startDate == null | typeOfPayment == null
				| user == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid order parameters");
			return;
		}

		Package pack = packageService.findById(packageId);
		ValPeriod valPeriod = vService.findById(valPeriodId);

		List<Product> products = new ArrayList<Product>();
		if (productsName != null) {
			for (String s : productsName) {
				Integer product = Integer.parseInt(s);
				products.add(productService.findById(product));
			}
		}

		if (rejectedOrder == null) {
			if (typeOfPayment.equals("Correct payment")) {
				Order o = oService.createOrder(user, pack, valPeriod, totalPrice, startDate, true, products);
				Date deactivationDate = Date.valueOf(startDate.toLocalDate().plusMonths(valPeriod.getMonths()));
				sasService.createSas(user, deactivationDate, o);
			} else {
				oService.createOrder(user, pack, valPeriod, totalPrice, startDate, false, products);
			}
		} else {
			if (typeOfPayment.equals("Correct payment")) {

				Order validOrder = oService.validOrder(rejectedOrder);
				Date deactivationDate = Date.valueOf(startDate.toLocalDate().plusMonths(valPeriod.getMonths()));
				sasService.createSas(user, deactivationDate, validOrder);
			} else {
				oService.invalidOrder(rejectedOrder);
			}
		}

		// To initialize the rejected order
		request.getSession().setAttribute("rejectedOrder", null);

		// Handle Order and Alert
		List<Order> allUserRejectedOrders = oService.findRejectedOrdersByUser(user);
		uService.handleInsolvent(user, allUserRejectedOrders);
		int failedPayments = 0;
		int totalAmount = 0;
		for (Order o : allUserRejectedOrders) {
			failedPayments = failedPayments + o.getFailedPayments();
			totalAmount = totalAmount + o.getTotalValue();
		}
		if (failedPayments >= 3) {
			aService.handleAlert(user, totalAmount,typeOfPayment);
		} else
			aService.deleteAlert(user);

		String path = getServletContext().getContextPath() + "/GoToHomePage";
		response.sendRedirect(path);
	}
}
