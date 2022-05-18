package telco.controllers;

import java.io.IOException;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import telco.services.PackageService;

@WebServlet("/CreatePackage")
public class CreatePackage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@EJB(name = "telco.services/PackageService")
	private PackageService pService;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String name = request.getParameter("packagename");
		Integer valPeriod = Integer.parseInt(request.getParameter("valperiod"));
		String services[] = request.getParameterValues("service");
		String products[] = request.getParameterValues("optionalproduct");
		
		if (name == null | name.isEmpty() | valPeriod == null | valPeriod == 0 | services == null
				| services.length == 0) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Package parameters");
			return;
		}

		pService.createPackage(name, valPeriod, services, products);
		String path = getServletContext().getContextPath() + "/GoToEmployeeHomePage";
		response.sendRedirect(path);
	}
}
