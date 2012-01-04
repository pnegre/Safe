package com.pnegre.safe;


class Secret
{
	String name;
	String username;
	String password;
	
	Secret(String nm, String us, String pw)
	{
		name = nm;
		username = us;
		password = pw;
	}
	
	public String toString()
	{
		return name;
	}
}




interface Database
{
	void init(String password);
	boolean Ready();
	Secret[] getSecrets();
	void newSecret(Secret s);
}


class DatabaseImp implements Database
{
	private SimpleCrypt sc = null;
	boolean ready = false;
	
	public boolean Ready()
	{
		return ready;
	}
	
	public void init(String password)
	{
		try
		{
			sc = new SimpleCrypt(password);
			ready = true;
		} 
		catch (Exception e) { }
	}
	
	public Secret[] getSecrets()
	{
		if (ready == false) return null;
		
		Secret[] ss = { 
			new Secret("meneame.net","us1","pw1"), 
			new Secret("slashdot.org","abc","dfg"),
			new Secret("Barrapunto.org","paba","mego")
		};
		return ss;
	}
	
	public void newSecret(Secret s)
	{
	}
}
