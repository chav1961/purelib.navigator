package chav1961.purelibnavigator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class Test {

	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		try(final Connection	conn = DriverManager.getConnection("jdbc:edb://localhost:5444/edb?connectTimeout=0","enterprisedb","sasa21");
			final CallableStatement ps = conn.prepareCall(" { call RDX_Entity.setUserPropBool(?, ? , ?, ?, ?, ?, ?) }")) {
			ps.setString(1, "S");
			ps.setString(2, "S");
			ps.setString(3, "S");
			ps.setString(4, "S");
			ps.setNull(5, Types.BIGINT);
			ps.setInt(6, 1);
			ps.setString(7, "S");
			ps.execute();
		}
	}

}


