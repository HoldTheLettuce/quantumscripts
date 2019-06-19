package taylor;

public class Config {

	public static final boolean PRODUCTION = false;

	public static final String MASTER_SERVER_HOST = PRODUCTION ? "http://157.230.162.34:3001" : "http://localhost:3001";
	public static final String LOCAL_SERVER_HOST = "http://localhost:3002";
}
