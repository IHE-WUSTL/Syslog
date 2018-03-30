package gov.nist.syslog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import gov.nist.syslog.util.Query;
import gov.nist.syslog.util.Util;

/**
 * program used at one point to build user tables for Syslog Message Browser
 * from a csv file of gazelle vendors.
 * 
 * @author rmoult01
 * 
 */
public class Builder {

	public static void main(String[] args) throws Exception {

		Path runDir = Paths.get("/home/rmoult01/atna/syslog/run/SyslogCollector");
		Path csvPfn = runDir.resolve("data.csv");
		String line;
		String last = "";
		Integer id = 0;

		Util.initialize(runDir.toString(), "Builder", null);

		try (BufferedReader in = new BufferedReader(new FileReader(csvPfn.toString()))) {

			while ((line = in.readLine()) != null) {
				String[] tokens = line.split(",");
				String co = tokens[0];
				String name = tokens[1];
				if (co.equalsIgnoreCase(last)) {
					id++;
				} else {
					last = co;
					id = 1;
				}
				String uid = co + id.toString();
				String q = new Query(
						"INSERT INTO users VALUES(DEFAULT, ${companyId}, '${userId}', "
								+ "'${password}', '${userName}', '', '', '0');")
						.set("companyId", 1).set("userId", uid)
						.set("password", DigestUtils.md5Hex("IHE"))
						.set("userName", name).getQuery();
				System.out.println(q);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
