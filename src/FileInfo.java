public class FileInfo {
	private String name;
	private String size;
	private String type;
	private String lastTime;

	public FileInfo() {

	}

	public FileInfo(String name, String size, String type, String lastTime) {
		this.name = name;
		this.size = size;
		this.type = type;
		this.lastTime = lastTime;
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
}
