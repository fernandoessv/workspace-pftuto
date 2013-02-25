package ar.com.magm.web.primefaces;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import ar.com.magm.model.Venta;

public class VentasBean implements Serializable {

	private static final long serialVersionUID = -6690574219803425728L;

	private String[] meses = new String[] { "Enero", "Febrero", "Marzo",
			"Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
			"Octubre", "Noviembre", "Diciembre" };

	private String sql = "SELECT year(fecha) as anio, month(fecha) as mes, zona, cliente, sum(importe*cantidad) as ventas FROM dw_ventasfact v INNER JOIN clientes c ON v.idCliente=c.idCliente INNER JOIN zonas z ON z.idZona=c.idZona WHERE cliente like ? GROUP BY zona, cliente, anio, mes ORDER BY anio,mes,zona,cliente";
	private List<Venta> ventas;
	private List<Venta> ventasFiltradas;
	private List<String> zonas;
	

	public VentasBean() {
		//processList(null);
	}

	public SelectItem[] getMesesOptions() {
		SelectItem[] r = new SelectItem[13];
		r[0] = new SelectItem("", "Todos");
		for (int t = 0; t < meses.length; t++)
			r[t + 1] = new SelectItem(meses[t], meses[t]);
		return r;
	}

	public List<Venta> getVentas() {
		if(ventas==null){
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
					.getExternalContext().getSession(false);
			LoginBean loginBean = (LoginBean) session.getAttribute(
					"loginBean");
			String parametroNombre=loginBean.getNombre();
			if(parametroNombre.equals("admin")){
				parametroNombre="%";
			}
			processList(new String[]{parametroNombre});
		}
		return ventas;
	}

	public List<Venta> getVentasFiltradas() {
		return ventasFiltradas;
	}

	public SelectItem[] getZonasOptions() {
		SelectItem[] r = new SelectItem[zonas.size() + 1];
		r[0] = new SelectItem("", "Todas");
		for (int t = 0; t < zonas.size(); t++)
			r[t + 1] = new SelectItem(zonas.get(t), zonas.get(t));
		return r;
	}

	private void processList(Object args[]) {
		ventas = new ArrayList<Venta>();
		zonas = new ArrayList<String>();
		ServletContext sc = (ServletContext) FacesContext.getCurrentInstance()
				.getExternalContext().getContext();
		Connection cn = (Connection) sc.getAttribute("datasource");
		try {
			PreparedStatement pst = cn.prepareStatement(sql);
			if (args != null) {
				for (int t = 0; t < args.length; t++) {
					pst.setObject(t + 1, args[t]);
				}
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				String zona = rs.getString("zona");
				Venta venta = new Venta(zona, rs.getString("cliente"),
						rs.getInt("anio"), rs.getInt("mes"),
						meses[rs.getInt("mes") - 1], rs.getDouble("ventas"));
				ventas.add(venta);
				if (!zonas.contains(zona))
					zonas.add(zona);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void setVentasFiltradas(List<Venta> ventasFiltradas) {
		this.ventasFiltradas = ventasFiltradas;
	}

}
