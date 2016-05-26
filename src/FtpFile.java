public class FtpFile {
	private boolean directory;
	private String auth;
	private String owner;
	private String group;
	private String size;
	private String lastTime;
	private String name;
	private String parent;
	private String type;

	public FtpFile() {
		this.directory = false;
		this.auth = "";
		this.owner = "";
		this.group = "";
		this.size = "";
		this.lastTime = "";
		this.name = "..";
		this.parent = "";
	}

	public FtpFile(String name, String size, String type, String lastTime, String auth, String ownerGroup) {
		this.directory = true;
		this.name = name;
		this.size = size;
		this.type = type;
		this.lastTime = lastTime;
		this.auth = auth;
		this.owner = " ";
		this.group = "";
		this.parent = "";
	}

	public boolean isDirectory() {
		return this.directory;
	}

	public boolean isFile() {
		return !this.directory;
	}

	public String getAbsolutePath() {
		return parent + name;
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

	public String getSize() {
		return size;
	}

	public String getLastTime() {
		return lastTime;
	}

	public String getName() {
		return name;
	}

	public String getParent() {
		return parent;
	}

	public String getType() {
		return type;
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

	public void setSize(String size) {
		this.size = size;
	}

	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("%s%s, %s, %s, %s/%s, %s, %s", parent, name, size, auth, owner, group, lastTime, (directory ? "dir" : "file"));
	}
}
