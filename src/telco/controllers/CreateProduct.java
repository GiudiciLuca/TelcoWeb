package telco.controllers;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import telco.services.ProductService;

@WebServlet("/CreateProduct")
public class CreateProduct extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@EJB(name = "telco.services/ProductService")
	private ProductService pService;

	public CreateProduct() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("productname");
		Integer monthlyFee = Integer.parseInt(request.getParameter("monthlyfee"));
		if (name == null | name.isEmpty() | monthlyFee == null | monthlyFee == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Product parameters");
			return;
		}
		pService.createProduct(name, monthlyFee);
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GoToEmployeeHomePage";
		response.sendRedirect(path);
	}
}
