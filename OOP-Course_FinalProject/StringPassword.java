public class StringPassword extends Password{
	private String string;
	private String getPassword() {
		return string;
	}
	@Override
	public boolean equals(Object o) {
		if(o==this)return true;
		if(!(o instanceof StringPassword))return false;
		StringPassword str=(StringPassword)o;
		return string.equals(str.getPassword());
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}
	public StringPassword(String s) {
		this.string=s;
	}
}
