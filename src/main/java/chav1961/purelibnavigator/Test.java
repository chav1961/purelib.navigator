package chav1961.purelibnavigator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Test {

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		try(final Connection	conn = DriverManager.getConnection("jdbc:edb://localhost:5444/edb?connectTimeout=0","enterprisedb","sasa21");
			final CallableStatement ps = conn.prepareCall("{ call RDX_ACS_UTILS.compileRightsForUser(?) }")) {
			ps.setString(1, "msinana");
			ps.executeUpdate();
		}
	}

}
