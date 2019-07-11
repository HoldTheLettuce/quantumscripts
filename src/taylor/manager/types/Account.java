package taylor.manager.types;

public class Account {
	
	private String id, username, password;
	
	private boolean isMember;
	
	public Account(String id, String username, String password, boolean isMember) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.isMember = isMember;
	}

	public String getId() {
		return id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean isMember() {
		return isMember;
	}
}
