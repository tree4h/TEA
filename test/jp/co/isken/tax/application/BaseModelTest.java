package jp.co.isken.tax.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
//import org.junit.Before;
import org.junit.BeforeClass;

import com.avaje.ebean.Ebean;

import play.test.FakeApplication;
import play.test.Helpers;

public class BaseModelTest {
	 public static FakeApplication app;
	 public static String createDdl = "";
	 public static String dropDdl = "";
	    
	 @BeforeClass
	 public static void startApp() throws IOException {
		 Map<String, String> settings = new HashMap<String, String>();
		 settings.put("db.default.driver", "com.mysql.jdbc.Driver");
		 settings.put("db.default.user", "tax");
		 settings.put("db.default.password", "tax");
		 settings.put("db.default.url", "jdbc:mysql://localhost/tax");
		 //app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
		 app = Helpers.fakeApplication(settings);
		 Helpers.start(app);
		 // Reading the evolution file
		 String evolutionContent = FileUtils.readFileToString(app.getWrappedApplication().getFile("conf/evolutions/default/1.sql"));
		 // Splitting the String to get Create & Drop DDL
		 String[] splittedEvolutionContent = evolutionContent.split("# --- !Ups");
		 String[] upsDowns = splittedEvolutionContent[1].split("# --- !Downs");
		 createDdl = upsDowns[0];
		 dropDdl = upsDowns[1];
		 //System.out.println(createDdl);
		 //System.out.println(dropDdl);
	 }
	 @AfterClass
	 public static void stopApp() {
		 Helpers.stop(app);
	 }
	 //TODO dropDdlがうまく動かない
	 //@Before
	 public void createCleanDb() {
		 Ebean.execute(Ebean.createCallableSql(dropDdl));
		 Ebean.execute(Ebean.createCallableSql(createDdl));
	 }
}
