package org.zenframework.z8.server.runtime;

public interface IClass<TYPE extends IObject> extends IObject {
	public final static int Constructor = -1;
	public final static int Constructor1 = 0;
	public final static int Constructor2 = 1;

	public Class<TYPE> getJavaClass();
	public void setJavaClass(Class<?> cls);

	public int stage();

	public boolean hasInstance();
	public boolean instanceOf(Class<?> cls);

	public TYPE newInstance();

	public TYPE get();
	public TYPE get(int stage);


//	public String getAttribute(String key);
//	public void setAttribute(String key, String value);
//	public boolean hasAttribute(String key);

//	public String classId();

//	public String displayName();

//	public String description();

//	public String index();
}
