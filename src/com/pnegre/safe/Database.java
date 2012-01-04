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
	Secret[] getSecrets();
	void newSecret(Secret s);
}


class DatabaseImp implements Database
{
	private SimpleCrypt sc = null;
	
	
	public void init(String password)
	{
		sc = null;
	}
	
	public Secret[] getSecrets()
	{
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
