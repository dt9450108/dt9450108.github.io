public class FileInfo {
	private String name;
	private String size;
	private String type;
	private String lastTime;
	private String auth;
	private String owner;
	private String group;

	public FileInfo() {

	}

	public FileInfo(String name, String size, String type, String lastTime) {
		this.name = name;
		this.size = size;
		this.type = type;
		this.lastTime = lastTime;
		this.auth = "";
		this.owner = "";
		this.group = "";
	}

	public FileInfo(String name, String size, String type, String lastTime, String auth, String owner, String group) {
		this.name = name;
		this.size = size;
		this.type = type;
		this.lastTime = lastTime;
		this.auth = auth;
		this.owner = owner;
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public String getSize() {
		return size;
	}

	public String getType() {
		return type;
	}

	public String getLastTime() {
		return lastTime;
	}

	public String getAuth() {
		return auth;
	}

	public String getOwner() {
		return owner;
	}

	public String getGroup() {
		return group;
	}

	public String getOwnerGroup() {
		return owner + " " + group;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}
